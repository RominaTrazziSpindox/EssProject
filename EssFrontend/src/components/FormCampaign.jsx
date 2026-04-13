import { useEffect, useState } from 'react';
import CampaignContent from './CampaignContent';
import AttendeeContent from './AttendeeContent';
import Button from './Button';
import { syncCampaigns } from '../api/crmApi';
import { validateCampaignForm } from '../helpers/HelperFunctions';
import { TEST_DELAY_MS, delay, buildTestPayload } from '../helpers/HelperTestPayload';
import { readRateLimitHeaders } from '../helpers/HelperRateLimit';
import { buildLoadingFeedback, buildValidationFeedback, buildSuccessFeedback, buildBadRequestFeedback, buildUnauthorizedFeedback, buildRateLimitedFeedback, buildNetworkFeedback, buildUnexpectedFeedback, buildTestSuccessFeedback, buildTestBadRequestFeedback, buildTestUnauthorizedFeedback, buildTestGenericFailureFeedback } from '../helpers/HelperFeedback';

function FormCampaign({ formData, setFormData, createInitialForm, createEmptyAttendee, buildCampaignPayload, rateLimitInfo, setRateLimitInfo }) {
 
  /* UI STATE */

  // Stores the selected attendee by rowId
  const [selectedRowId, setSelectedRowId] = useState('');

  // Tracks whether the form submission is currently in progress. While true, form controls are disabled to prevent duplicate actions 
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Stores client-side validation errors
  const [validationErrors, setValidationErrors] = useState({ campaignId: '', attendees: {} });

  // Stores the feedback message shown to the user after validation or submission
  const [feedback, setFeedback] = useState({ type: '', message: '' });

  // Stores the countdown in seconds until the rate limit resets, based on the Retry-After header or X-Rate-Limit-Reset timestamp
  const [secondsLeft, setSecondsLeft] = useState(0);


  /* HANDLERS FUNCTIONS */

  // Updates campaign fields (campaignId, subCampaignId)
  const handleChangeCampaign = (event) => {
    const { name, value } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: value,
    }));

    // Clear the campaign field error as soon as the user edits it
    setValidationErrors((prev) => ({
      ...prev,
      [name]: '',
    }));
  };

  // Updates a specific attendee field identified by its rowId
  const handleChangeAttendee = (rowId, field, value) => {
    setFormData((current) => ({
      ...current,
      attendees: current.attendees.map((attendee) =>
        attendee.rowId === rowId ? { ...attendee, [field]: value } : attendee
      ),
    }));

    // Clear the edited attendee field error immediately after user input
    setValidationErrors((prev) => ({
      ...prev,
      attendees: {
        ...prev.attendees,
        [rowId]: {
          ...prev.attendees[rowId],
          [field]: '',
        },
      },
    }));
  };

  /* ATTENDEE ACTIONS */

  // Appends a new empty attendee to the current form
  const addAttendee = () => {
    const newAttendee = createEmptyAttendee();

    setFormData((current) => ({
      ...current,
      attendees: [...current.attendees, newAttendee],
    }));
  };

  // Removes the selected attendee. If it is the last remaining attendee, the row is reset instead of removing the array entirely
  const removeAttendee = (rowIdToRemove) => {
    if (!rowIdToRemove) return;

    setFormData((current) => {
      if (current.attendees.length === 1) {
        return {
          ...current,
          attendees: [createEmptyAttendee()],
        };
      }

      return {
        ...current,
        attendees: current.attendees.filter(
          (attendee) => attendee.rowId !== rowIdToRemove
        ),
      };
    });

    setValidationErrors((prev) => {
      const updatedAttendeeErrors = { ...prev.attendees };
      delete updatedAttendeeErrors[rowIdToRemove];

      return {
        ...prev,
        attendees: updatedAttendeeErrors,
      };
    });

    setSelectedRowId('');
  };

  /* FORM ACTIONS */

  // Resets the form to its initial state and clears the form fields
  const clearForm = () => {

    setFormData(createInitialForm());

    setSelectedRowId('');

    setValidationErrors({
      campaignId: '',
      attendees: {},
    });

    setFeedback({
      type: '',
      message: '',
    });

    setSecondsLeft(0);
  };

  // Updates the shared rate limit state after a successful request
  const handleRequestSuccess = (result, feedbackBuilder) => {
    const { limit, remaining } = readRateLimitHeaders(result.headers, 5);
    const requestCount = remaining !== null ? limit - remaining : 0;

    setRateLimitInfo((current) => ({
      ...current,
      requestCount,
      limit,
      lastStatus: result.status,
      retryAfter: null,
      isRateLimited: false,
      apiKeyStatus: 'Valid',
      resetAt: null,
    }));

    setFeedback(feedbackBuilder(result.status));
  };

  // Reads rate limit headers on error responses, updates the shared state, and returns how the caller should proceed.
  const handleRequestError = (error, options = {}) => {
    const { isTest = false, index = null } = options;

    // Read rate limit headers even on error responses, when available
    const { limit, remaining, retryAfter } = readRateLimitHeaders(error.headers, rateLimitInfo?.limit ?? 5);
     
    const requestCount = remaining !== null ? limit - remaining : rateLimitInfo?.requestCount ?? 0;

    // Map known error codes to give user-friendly messages
    switch (error.code) {

      case 'BAD_REQUEST':
        setRateLimitInfo((current) => ({
          ...current,
          requestCount,
          limit,
          lastStatus: 400,
          retryAfter: null,
          isRateLimited: false,
        }));

        setFeedback(isTest ? buildTestBadRequestFeedback(index) : buildBadRequestFeedback());
        return isTest ? 'continue' : 'handled';

      case 'UNAUTHORIZED':
        setRateLimitInfo((current) => ({
          ...current,
          lastStatus: 401,
          retryAfter: null,
          isRateLimited: false,
        }));

        setFeedback(isTest ? buildTestUnauthorizedFeedback(index) : buildUnauthorizedFeedback());
        return 'break';

      case 'RATE_LIMITED':
        setRateLimitInfo((current) => ({
          ...current,
          requestCount,
          limit,
          lastStatus: 429,
          retryAfter,
          isRateLimited: true,
          apiKeyStatus: 'Valid',
          resetAt: null,
        }));

        setFeedback(buildRateLimitedFeedback(retryAfter));
        return 'break';

      case 'NETWORK_ERROR':
        setFeedback(buildNetworkFeedback());
        return 'break';

      default:
        setFeedback(isTest ? buildTestGenericFailureFeedback(index) : buildUnexpectedFeedback());
        return 'break';
    }
  };

  // Validates the form, builds the payload, and sends it to the Producer API
  const handleSubmit = async (event) => {
    
    event.preventDefault();

    // Run frontend validation before sending the request
    const validation = validateCampaignForm(formData);

    // If validation fails, show errors and prevent submission
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setFeedback(buildValidationFeedback());
      return;
    }

    // Clear old errors before starting a valid submission
    setValidationErrors({
      campaignId: '',
      attendees: {},
    });

    setIsSubmitting(true);

    setFeedback(buildLoadingFeedback());

    try {

      // Build the final payload expected by the backend
      const campaignPayload = buildCampaignPayload(formData);

      // API expects an array of campaigns
      const result = await syncCampaigns([campaignPayload]);

      handleRequestSuccess(result, buildSuccessFeedback);

      // Reset the form after a successful submission
      setFormData(createInitialForm());
      setSelectedRowId('');

    } catch (error) {
      
      handleRequestError(error);

    } finally {
      setIsSubmitting(false);
    }
  };

  // Sends six valid requests in sequence to demonstrate the rate limit directly from the UI
  const handleRateLimitTest = async () => {
   
    setIsSubmitting(true);

    try {
      for (let i = 1; i <= 6; i += 1) {
        
        // Reads the test paylod
        const testPayload = buildTestPayload(i);

        try {

          const result = await syncCampaigns(testPayload);

          console.log('TEST SUCCESS', i, result.status);

          handleRequestSuccess(result, (status) => buildTestSuccessFeedback(i, status));

          // Pause before the next request so the UI can show each step.
          if (i < 6) {
            await delay(TEST_DELAY_MS);
          }
          continue;

        } catch (error) {

          console.log('TEST ERROR', i, error.code, error.status, error.message);

          const action = handleRequestError(error, { isTest: true, index: i });

          if (action === 'continue') {
            if (i < 6) {
              await delay(TEST_DELAY_MS);
            }

            continue;
          }

          break;
        }
      }

    } finally {
      setIsSubmitting(false);
    }
  };

  /* COUNTDOWN */
  
  // Reset the countdown when the banner is not in warning state
  useEffect(() => {
  
  if (feedback.type !== 'warning' || !rateLimitInfo?.retryAfter) {
    setSecondsLeft(0);
    return;
  }

  // Initialize the countdown with the number of seconds returned by the backend
  setSecondsLeft(rateLimitInfo.retryAfter);

  }, [feedback.type, rateLimitInfo?.retryAfter]);


  // Stop when the banner is not in warning state or the countdown is not active
  useEffect(() => {
   
    if (feedback.type !== 'warning' || secondsLeft <= 0) {
      return;
    }

    const timeoutId = setTimeout(() => {
      if (secondsLeft <= 1) {
        
        setSecondsLeft(0);

        setFeedback({
          type: '',
          message: '',
        });

        setRateLimitInfo((current) => ({
          ...current,
          requestCount: 0,
          retryAfter: null,
          isRateLimited: false,
          resetAt: null,
        }));

        return;
      }

      setSecondsLeft((current) => current - 1);
    }, 1000);

    return () => clearTimeout(timeoutId);
  }, [feedback.type, secondsLeft, setRateLimitInfo]);
    

  /* RENDER */

  return (
    <section>
      <form onSubmit={handleSubmit}>
        <h2>Campaign Submission Form</h2>
        <hr />
        <h3>Create a new campaign</h3>

        <CampaignContent formData={formData} onChange={handleChangeCampaign} errors={validationErrors} isSubmitting={isSubmitting}/>

        <h3>Add attendees</h3>

        <fieldset className="new_attendee">
          {formData.attendees.map((attendee, index) => (
            <div key={attendee.rowId} className={`attendee_wrapper ${selectedRowId === attendee.rowId ? 'selected' : ''}`} onClick={() => setSelectedRowId(attendee.rowId)}>
              <p>Attendee #{index + 1}</p>

              <AttendeeContent attendee={attendee} onFieldChange={handleChangeAttendee} isSubmitting={isSubmitting} errors={validationErrors.attendees[attendee.rowId]} />
            </div>
          ))}
        </fieldset>

        <div className="action">
          <Button text="Add Attendee" variant="primary" onClick={addAttendee} disabled={isSubmitting} />

          <div className="remove_attendee_group">
            <label htmlFor="attendeeToRemove">Choose attendee to remove</label>

            <select id="attendeeToRemove" value={selectedRowId} onChange={(e) => setSelectedRowId(e.target.value)} disabled={isSubmitting}>
              <option value="">Select an attendee</option>

              {formData.attendees.map((attendee, index) => (
                <option key={attendee.rowId} value={attendee.rowId}>
                  Attendee #{index + 1} - {attendee.firstName || 'No name'} {attendee.lastName || ''}
                </option>
              ))}
            </select>

            <Button text="Remove Attendee" variant="danger" onClick={() => removeAttendee(selectedRowId)} disabled={isSubmitting || !selectedRowId} />
          </div>
        </div>

        <hr id="hr_end" />

        {feedback.message && (
          <div className={`form_user_feedback ${feedback.type}`}>
            <p>{feedback.message}</p>

            {feedback.type === 'warning' && secondsLeft > 0 && (
              <p className="rate_limit_countdown">Retry available in {secondsLeft}s</p>
            )}
          </div>
        )}

        <div className="action">
          <Button type="submit" variant="primary" text={isSubmitting ? 'Sending...' : 'Submit Campaign'} disabled={isSubmitting} />
          <Button text="Clear Form" variant="danger" onClick={clearForm} disabled={isSubmitting} />
          <Button text="Send 6 Test Requests" variant="primary" onClick={handleRateLimitTest} disabled={isSubmitting} />
        </div>
      </form>
    </section>
  );
}

export default FormCampaign;
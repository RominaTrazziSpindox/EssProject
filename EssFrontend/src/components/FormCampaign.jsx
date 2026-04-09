import { useState } from 'react';

import CampaignContent from './CampaignContent';
import AttendeeContent from './AttendeeContent';
import Button from './Button';

import { validateCampaignForm } from '../helpers/HelperFunctions';
import { syncCampaigns } from '../api/crmApi';

function FormCampaign({formData, setFormData, createInitialForm, createEmptyAttendee, buildCampaignPayload}) {

  /* UI STATE */

  // Stores the selected attendee by rowId
  const [selectedRowId, setSelectedRowId] = useState(null);

  /* Tracks whether the form submission is currently in progress
   While true, form controls are disabled to prevent duplicate actions */
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Stores client-side validation errors
  const [validationErrors, setValidationErrors] = useState({
    campaignId: '',
    attendees: {},
  });

  // Stores the feedback message shown to the user after validation or submission
  const [feedback, setFeedback] = useState({
    type: '',
    message: '',
  });


  /* HANDLERS FUNCTIONS */

  // Updates campaign fields (campaignId, subCampaignId)
  const handleChangeCampaign = (event) => {
    const { name, value } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: value,
    }));

    // Clear the campaign field error as soon as the user edits it.
    setValidationErrors((prev) => ({
      ...prev,
      [name]: '',
    }));
  };

  // Updates a specific attendee field identified by its rowId.
  const handleChangeAttendee = (rowId, field, value) => {
    setFormData((current) => ({
      ...current,
      attendees: current.attendees.map((attendee) =>
        attendee.rowId === rowId
          ? { ...attendee, [field]: value }
          : attendee
      ),
    }));

    // Clear the edited attendee field error immediately after user input.
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

  /* Removes the selected attendee.
  If it is the last remaining attendee, the row is reset instead of removing the array entirely. */
  const removeAttendee = () => {
    if (!selectedRowId) return;

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
          (attendee) => attendee.rowId !== selectedRowId
        ),
      };
    });

    // Remove validation errors associated with the deleted attendee.
    setValidationErrors((prev) => {
      const updatedAttendeeErrors = { ...prev.attendees };
      delete updatedAttendeeErrors[selectedRowId];

      return {
        ...prev,
        attendees: updatedAttendeeErrors,
      };
    });

    setSelectedRowId(null);
  };



  /* FORM ACTIONS */

  // Resets the form to its initial state and clears all UI feedback.
  const clearForm = () => {
    setFormData(createInitialForm());
    setSelectedRowId(null);

    setValidationErrors({
      campaignId: '',
      attendees: {},
    });

    setFeedback({
      type: '',
      message: '',
    });
  };

  // Validates the form, builds the payload, and sends it to the Producer API.
  const handleSubmit = async (event) => {
    
    event.preventDefault();

    // Run frontend validation before sending the request.
    const validation = validateCampaignForm(formData);

    // If validation fails, show errors and prevent submission
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setFeedback({
        type: 'error',
        message: 'Validation failed. Please check the fields.',
      });
      return;
    }

    // Clear old errors before starting a valid submission.
    setValidationErrors({
      campaignId: '',
      attendees: {},
    });

    setIsSubmitting(true);
    
    setFeedback({
      type: 'loading',
      message: 'Sending request to Producer...',
    });

    try {
      // Build the final payload expected by the backend
      const campaignPayload = buildCampaignPayload(formData);

      // The API expects an array of campaigns
      await syncCampaigns([campaignPayload]);

      setFeedback({
        type: 'success',
        message: 'Request accepted (202). Campaign queued successfully.',
      });

      // Reset the form after a successful submission.
      setFormData(createInitialForm());
      setSelectedRowId(null);

    } catch (error) {

      // Map known error codes to user-friendly messages
      switch (error.code) {
        case 'BAD_REQUEST':
          setFeedback({
            type: 'error',
            message: 'Bad request (400). Check required fields.',
          });
          break;

        case 'UNAUTHORIZED':
          setFeedback({
            type: 'error',
            message: 'Unauthorized (401). Check X-API-KEY.',
          });
          break;

        case 'NETWORK_ERROR':
          setFeedback({
            type: 'error',
            message: 'Network error or timeout.',
          });
          break;

        default:
          setFeedback({
            type: 'error',
            message: 'Unexpected error while sending the request.',
          });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section>
      <form onSubmit={handleSubmit}>
        <h2>Campaign Submission Form</h2>
        <hr />

        <h3>Create a new campaign</h3>

        <CampaignContent formData={formData} onChange={handleChangeCampaign} errors={validationErrors} isSubmitting={isSubmitting}/>
          
        <h3>Add attendees</h3>

        <fieldset className="new_attendee">
          
          {formData.attendees.map((attendee) => (
            <div key={attendee.rowId} className={`attendee-wrapper ${selectedRowId === attendee.rowId ? 'selected' : ''}`}
            onClick={() => setSelectedRowId(attendee.rowId)}>  
                <AttendeeContent attendee={attendee} onFieldChange={handleChangeAttendee} isSubmitting={isSubmitting}
                errors={validationErrors.attendees[attendee.rowId]}/>
            </div>
          ))}
        </fieldset>

        <div className="action">
          <Button text="Add Attendee" variant="primary" onClick={addAttendee} disabled={isSubmitting}/>

          <Button text="Remove Attendee" variant="danger" onClick={removeAttendee} disabled={isSubmitting || !selectedRowId}/>
         
        </div>

        <hr />

        {feedback.message && (
          <div className={`form-feedback ${feedback.type}`}>
            {feedback.message}
          </div>
        )}

        <div className="action">
          <Button type="submit" variant="primary" text={isSubmitting ? 'Sending...' : 'Submit Campaign'} disabled={isSubmitting}/>
           
          <Button text="Clear Form" variant="danger" onClick={clearForm} disabled={isSubmitting}/>
        </div>
      </form>
    </section>
  );
}

export default FormCampaign;
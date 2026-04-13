import {  useState, useMemo } from 'react';

import FormCampaign from './FormCampaign.jsx';
import PayloadPreview from './PayloadPreview.jsx';

import { toRequiredText, toNullable } from '../helpers/HelperFunctions';

function CampaignPage() {

  // Creates a new empty attendee 
  const createEmptyAttendee = () => ({
    rowId: crypto.randomUUID(),
    cn: '',
    firstName: '',
    lastName: '',
    birthDate: '',
    partnerId: '',
    isCompanion: false,
    qrCode: '',
  });

  // Creates the initial form state with one empty attendee.
  const createInitialForm = () => ({
    campaignId: '',
    subCampaignId: '',
    attendees: [createEmptyAttendee()],
  });

  // Main form state shared with FormCampaign.
  const [formData, setFormData] = useState(createInitialForm);

  // Shared rate limit state used by both FormCampaign and RateLimit.
  const [rateLimitInfo, setRateLimitInfo] = useState({
    requestCount: 0,
    limit: 5,
    lastStatus: null,
    retryAfter: null,
    isRateLimited: false,
    apiKeyStatus: 'Unknown',
    resetAt: null,
  });

  // Builds the final payload expected by the backend.
  const buildCampaignPayload = (form) => ({
    campaignId: toRequiredText(form.campaignId),
    subCampaignId: toNullable(form.subCampaignId),
    attendees: form.attendees.map(({ rowId, ...attendee }) => ({
      ...attendee,
      cn: toNullable(attendee.cn),
      firstName: toRequiredText(attendee.firstName),
      lastName: toRequiredText(attendee.lastName),
      birthDate: toNullable(attendee.birthDate),
      partnerId: toRequiredText(attendee.partnerId),
      qrCode: toRequiredText(attendee.qrCode),
    })),
  });

  // Memoizes the preview payload so it is recalculated only when formData changes.
  const previewPayload = useMemo(() => {
    return [buildCampaignPayload(formData)];
  }, [formData]);

  return (
    <>
      <FormCampaign formData={formData} setFormData={setFormData} createInitialForm={createInitialForm} createEmptyAttendee={createEmptyAttendee}
      buildCampaignPayload={buildCampaignPayload} rateLimitInfo={rateLimitInfo} setRateLimitInfo={setRateLimitInfo}/>
      
      <PayloadPreview data={previewPayload} rateLimitInfo={rateLimitInfo}/>
 
    </>
  );
}

export default CampaignPage;
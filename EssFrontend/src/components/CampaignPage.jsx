import { useMemo, useState } from 'react';

import FormCampaign from './FormCampaign.jsx';
import PayloadPreview from './PayloadPreview.jsx';

import { toRequiredText, toNullable } from '../helpers/HelperFunctions';

function CampaignPage() {
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

  const createInitialForm = () => ({
    campaignId: '',
    subCampaignId: '',
    attendees: [createEmptyAttendee()],
  });

  const [formData, setFormData] = useState(createInitialForm);

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

  const previewPayload = useMemo(() => {
    return [buildCampaignPayload(formData)];
  }, [formData]);

  return (
    <>
      <FormCampaign formData={formData} setFormData={setFormData} createInitialForm={createInitialForm} createEmptyAttendee={createEmptyAttendee} 
      buildCampaignPayload={buildCampaignPayload}/>
        
      <PayloadPreview data={previewPayload} />
    </>
  );
}

export default CampaignPage;
export const TEST_DELAY_MS = 5000;

export function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function buildTestPayload(index) {
  const uniqueId = `${Date.now()}-${index}`;

  return [
    {
      campaignId: `C-RL-${uniqueId}`,
      subCampaignId: `SC-RL-${index}`,
      attendees: [
        {
          cn: `CN-${uniqueId}`,
          firstName: 'Test',
          lastName: `User${index}`,
          birthDate: '1990-01-01',
          partnerId: `PARTNER-${uniqueId}`,
          isCompanion: false,
          qrCode: `QR-RL-${uniqueId}`,
        },
      ],
    },
  ];
}
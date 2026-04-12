/* HELPERS FUNCTIONS */

// Trim spaces from specific values
export const toRequiredText = (value) => value.trim();


// Converts empty strings into null for nullable backend fields
export const toNullable = (value) => {
   
    if (value == null) return null;
    
    const trimmedValue = String(value).trim();
    
    return trimmedValue === '' ? null : trimmedValue;
};


/**
 * Validates the entire campaign form data
 * @param {Object} form - The current formData state
 * @returns {Object} - An object containing isValid (boolean) and errors (object)
 */
export const validateCampaignForm = (form) => {
    const errors = {
        campaignId: '',
        attendees: {},
    };
    let isValid = true;

    // Form campaign validation
    if (!form.campaignId.trim()) {
        errors.campaignId = 'Campaign ID is required.';
        isValid = false;
    }

    // Attendees validation
    form.attendees.forEach((attendee) => {
        const attendeeErrors = {};

        // Iterate through all keys of the attendee object
        Object.keys(attendee).forEach((field) => {
            const value = attendee[field];

            switch (field) {
                case 'firstName':
                case 'lastName':
                case 'partnerId':
                case 'qrCode':
                    if (typeof value === 'string' && !value.trim()) {
                        attendeeErrors[field] = `${field.charAt(0).toUpperCase() + field.slice(1)} is required.`;
                    }
                    break;

                case 'cn':
                case 'birthDate':
                    if (!attendee.isCompanion && (typeof value === 'string' && !value.trim())) {
                        const label = field === 'cn' ? 'CN Code' : 'Birth date';
                        attendeeErrors[field] = `${label} is required for non-companion attendees.`;
                    }
                    break;

                default:
                    break;
            }
        });

        // Check if current attendee has errors (this must be inside the forEach)
        if (Object.keys(attendeeErrors).length > 0) {
            errors.attendees[attendee.rowId] = attendeeErrors;
            isValid = false;
        }
    });

    return { isValid, errors };
};
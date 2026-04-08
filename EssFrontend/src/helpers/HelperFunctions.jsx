/* HELPERS FUNCTIONS */

// Trim spaces from specific values
export const toRequiredText = (value) => value.trim();

// Converts empty strings into null for nullable backend fields
export const toNullable = (value) => {
    const trimmedValue = value.trim();
    return trimmedValue === '' ? null : trimmedValue;
};
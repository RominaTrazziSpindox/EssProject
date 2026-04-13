// Base URL of the Producer endpoint
const API_URL = import.meta.env.VITE_CRM_SYNC_URL;

// API key required by the backend
const API_KEY = import.meta.env.VITE_CRM_API_KEY;

// Timeout for API requests in milliseconds (default to 8000 ms if not set)
const TIMEOUT_MS = parseInt(import.meta.env.VITE_CRM_TIMEOUT_MS, 10) || 8000;

// Sends the campaign payload to the Producer API (= an array of campaigns)
export async function syncCampaigns(campaignPayload) {

  // Stop execution if required environment variables are missing.
  if (!API_URL || !API_KEY) {
    const configError = new Error('Missing VITE_CRM_SYNC_URL or VITE_CRM_API_KEY in environment variables.');
    configError.code = 'CONFIG_ERROR';
    throw configError;
  }

  let response;

  try {

    // HTTP POST request with timeout
    response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-API-KEY': API_KEY,
      },
      body: JSON.stringify(campaignPayload),
      signal: AbortSignal.timeout(TIMEOUT_MS),
    });

  } catch (error) {

    // Handle network errors and request timeouts
    const networkError = new Error('Network error or request timeout.');
    networkError.status = 0;
    networkError.code = 'NETWORK_ERROR';
    networkError.cause = error;
    throw networkError;
  }

  // Return useful data when the request is successful
  if (response.ok) {
    return {
      status: response.status,
      headers: response.headers,
      message: 'Request accepted.',
    };
  }

  // Read the error body if available
  const errorText = await response.text();

  // Create a custom error object with status, code and headers
  const httpError = new Error(errorText || getDefaultErrorMessage(response.status));
  httpError.status = response.status;
  httpError.code = getErrorCode(response.status);
  httpError.headers = response.headers;

  throw httpError;
}

// Common status HTTP errors
function getErrorCode(status) {
  switch (status) {
    case 400:
      return 'BAD_REQUEST';
    case 401:
      return 'UNAUTHORIZED';
    case 429:
      return 'TOO_MANY_REQUESTS';
    default:
      return 'HTTP_ERROR';
  }
}

// Default error messages for common HTTP status codes
function getDefaultErrorMessage(status) {
  switch (status) {
    case 400:
      return 'Bad request. Check required fields and payload format.';
    case 401:
      return 'Unauthorized. Check X-API-KEY.';
    case 429:
      return 'Too many requests. Please wait before trying again.';
    default:
      return 'Unexpected error while sending the request.';
  }
}
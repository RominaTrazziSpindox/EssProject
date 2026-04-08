// Base URL of the Producer endpoint
const API_URL = import.meta.env.VITE_CRM_SYNC_URL;

// API key required by the backend
const API_KEY = import.meta.env.VITE_CRM_API_KEY;


// Sends the campaign payload to the Producer API (array of campaigns).
export async function syncCampaigns(campaignPayload) {
    
    // Stop execution if required environment variables are missing
    if (!API_URL || !API_KEY) {
        throw new Error('Missing VITE_CRM_SYNC_URL or VITE_CRM_API_KEY in environment variables.');
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
            body: JSON.stringify([campaignPayload]),
            signal: AbortSignal.timeout(8000),
        });

    } catch (error) {

        // Handle network errors and timeouts
        const networkError = new Error('Network error or request timeout.');
        networkError.status = 0;
        networkError.cause = error;
        throw networkError;
    }
    
    // If the response is successful, return the HTTP status
    if (response.ok) {
        return response.status;
    }

    // If the request fails, try to read the response body.
    const errorText = await response.text();
    
    // Create a custom error object and attach the HTTP status code
    const httpError = new Error(errorText || 'CRM sync request failed.');
    httpError.status = response.status;
    throw httpError;
}

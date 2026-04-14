/* ERRORS FOR HANDLE SUBMIT */


// Request is being sent to the Producer (Blue)
export function buildLoadingFeedback() {
  return {
    type: 'loading',
    message: 'Sending request to Producer...',
  };
}

// Fields not valid or missing (Red)
export function buildValidationFeedback() {
  return {
    type: 'error',
    message: 'Validation failed. Please check the fields.',
  };
}

// Malformed JSON request (Red)
export function buildBadRequestFeedback() {
  return {
    type: 'error',
    message: 'Bad request (400). Check required fields or payload format.',
  };
}

// Wrong X-API-KEY (Red)
export function buildUnauthorizedFeedback() {
  return {
    type: 'error',
    message: 'Unauthorized (401). Check X-API-KEY.',
  };
}

// Network error (e.g. IngestionAPI down) or request timeout (Red)
export function buildNetworkFeedback() {
  return {
    type: 'error',
    message: 'Network error or request timeout.',
  };
}

// Unexpected error (Red)
export function buildUnexpectedFeedback() {
  return {
    type: 'error',
    message: 'Unexpected error while sending the request.',
  };
}

// Too many requests (Orange)
export function buildRateLimitedFeedback() {
  return {
    type: 'warning',
    message: `Too many requests (429). Plaease wait before sending more requests.`,
  };
}

// Successful request (Green)
export function buildSuccessFeedback(status) {
  return {
    type: 'success',
    message: `Request accepted (${status}). Campaign queued successfully.`,
  };
}


/* ERRORS FOR TEST REQUESTS */

export function buildTestSuccessFeedback(index, status) {
  return {
    type: 'success',
    message: `Test request ${index} accepted (${status}).`,
  };
}

export function buildTestBadRequestFeedback(index) {
  return {
    type: 'error',
    message: `Test request ${index} failed with 400.`,
  };
}

export function buildTestUnauthorizedFeedback(index) {
  return {
    type: 'error',
    message: `Test request ${index} failed with 401.`,
  };
}

export function buildTestGenericFailureFeedback(index) {
  return {
    type: 'error',
    message: `Test request ${index} failed.`,
  };
}
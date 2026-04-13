export function readRateLimitHeaders(headers, fallbackLimit = 5) {
  
  const limitHeader = headers?.get?.('X-Rate-Limit-Limit');
  const remainingHeader = headers?.get?.('X-Rate-Limit-Remaining');
  const retryAfterHeader = headers?.get?.('Retry-After');

  const parsedLimit = Number(limitHeader);
  const parsedRemaining = Number(remainingHeader);
  const parsedRetryAfter = Number(retryAfterHeader);

  return {
    limit: Number.isFinite(parsedLimit) ? parsedLimit : fallbackLimit,
    remaining: Number.isFinite(parsedRemaining) ? parsedRemaining : null,
    retryAfter: Number.isFinite(parsedRetryAfter) ? parsedRetryAfter : null,
  };
}
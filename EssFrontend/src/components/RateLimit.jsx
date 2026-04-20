import React, { useState, useEffect } from 'react';

RateLimit.propTypes = {
    requestCount: PropTypes.number,
    limit: PropTypes.number,
    retryAfter: PropTypes.oneOfType([
        PropTypes.number,
        PropTypes.string,
        PropTypes.instanceOf(Date),
    ]),
    isRateLimited: PropTypes.bool,
    resetAt: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.instanceOf(Date),
        PropTypes.number,
    ]),
};

function RateLimit({requestCount = 0, limit = 5, retryAfter = null, isRateLimited = false,  resetAt = null,}) {

    const safeValue = Math.min(requestCount, limit);
    const [secondsLeft, setSecondsLeft] = useState(0);

    useEffect(() => {

        // Do after the component is rendered if "resetAt" is changed

        // If "resetAt" is false set the secondLeft to zero and do nothing
        if (!resetAt) {
            setSecondsLeft(0);
        return;
        }

        // Countdown function to count how many seconds left to restart the bucket (different from zero)
        const updateCountdown = () => {
            const remainingSeconds = Math.max( 0, Math.ceil((resetAt - Date.now()) / 1000));               
            setSecondsLeft(remainingSeconds);
        };

        updateCountdown();

        // Repeat the countdown every seconds
        const intervalId = setInterval(updateCountdown, 1000);

        // Delete the timer when the countdown is finished (UseEffect return a clearInterval function)
        return () => clearInterval(intervalId);
    }, [resetAt]);

    return (

        <section>

            <div className="rate_limit">

                <h2>Rate Limiting and Status</h2>
                <hr />
                <h3>Check</h3>

                <p id="request_number">
                    Request: <span> {requestCount} / {limit} per minute</span>
                </p>

                <input type="range" min="0" max={limit} step="1" value={safeValue} readOnly/>

            </div>
            
        </section>
    )

}


export default RateLimit
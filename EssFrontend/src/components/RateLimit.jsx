import React, { useState, useEffect } from 'react';

function RateLimit({requestCount = 0, limit = 5, retryAfter = null, isRateLimited = false,  resetAt = null,}) {

    const safeValue = Math.min(requestCount, limit);

    const [secondsLeft, setSecondsLeft] = useState(0);

    useEffect(() => {
        if (!resetAt) {
            setSecondsLeft(0);
        return;
        }

        const updateCountdown = () => {

            const remainingSeconds = Math.max( 0, Math.ceil((resetAt - Date.now()) / 1000));               
            setSecondsLeft(remainingSeconds);
        };

        updateCountdown();

        const intervalId = setInterval(updateCountdown, 1000);

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
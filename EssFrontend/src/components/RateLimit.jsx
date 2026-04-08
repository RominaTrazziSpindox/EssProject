function RateLimit() {


    return (

        <section>

            <div className="rate_limit">

                <h2>Rate Limiting and Status</h2>
                <hr></hr>
                <h3>Check</h3>

                <p id="request_number">Request: 
                    <span> number / 5 per minute</span>
                </p>
                
                <input type="range" min="0" max="5" step="1"></input>
                <p>X-API-KEY Status: round status</p>
            </div>
            
        </section>
    )

}


export default RateLimit
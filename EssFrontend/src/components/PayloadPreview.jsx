import JsonText from './JsonText'; 
import RateLimit from './RateLimit.jsx';

PayloadPreview.propTypes = {
    data: PropTypes.oneOfType([
        PropTypes.object,
        PropTypes.array,
        PropTypes.string,
    ]).isRequired,
    rateLimitInfo: PropTypes.shape({
        requestCount: PropTypes.number,
        limit: PropTypes.number,
        retryAfter: PropTypes.oneOfType([
            PropTypes.number,
            PropTypes.string,
            PropTypes.instanceOf(Date),
        ]),
        isRateLimited: PropTypes.bool,
        resetAt: PropTypes.oneOfType([
            PropTypes.number,
            PropTypes.string,
            PropTypes.instanceOf(Date),
        ]),
    }),
};

PayloadPreview.defaultProps = {
    rateLimitInfo: null,
};


function PayloadPreview({ data, rateLimitInfo  }) {
  return (
    <div>
      <section>
        <div className="json_payload">
          <h2>Payload preview</h2>
          <hr />        
          <h3>Review payload data</h3> 

          <div className="json_code">
            <JsonText data={data} />
          </div> 
        </div>
      </section>
      
      <RateLimit requestCount={rateLimitInfo.requestCount} limit={rateLimitInfo.limit} retryAfter={rateLimitInfo.retryAfter} 
      isRateLimited={rateLimitInfo.isRateLimited} resetAt={rateLimitInfo.resetAt}/>

    </div>  
  );
}

export default PayloadPreview;
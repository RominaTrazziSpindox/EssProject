import JsonText from './JsonText'; 
import RateLimit from './RateLimit.jsx';

function PayloadPreview({ data }) {
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
      
      <RateLimit />
    </div>  
  );
}

export default PayloadPreview;
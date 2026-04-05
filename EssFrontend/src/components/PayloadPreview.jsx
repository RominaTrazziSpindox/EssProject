import JsonText from './JsonText'; 

function PayloadPreview() {

  return (
    <section className="panel">

      <div className="json_payload">
      
        <h2>Payload preview</h2>
        <hr></hr>        
        <h3>Review payload data</h3> 


        <div className="json_code">
          <JsonText/>
        </div> 
      
      </div>
  
    </section>
  )
}

export default PayloadPreview
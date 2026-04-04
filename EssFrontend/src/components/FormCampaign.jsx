import Toggle from './Toggle';
import Button from './Button';

function FormCampaign() {

    function addAttendee() {
        console.log('addAttendee');
    }

    function changeAttendee() {
        console.log('changeAttendee');
    }

    function deleteAttendee() {
        console.log('deleteAttendee');
    }

    function submitCampaign() {
        console.log('submitCampaign');
    }

    function clearForm() {
        console.log('clearForm');
    }

    return (

        <section>

            <form> 
                
                <h2>Campaign Submission Form</h2>
                <hr></hr>
                <h3>Create a new campaign</h3>
                
                <fieldset className="new_campaign">
                    <label htmlFor="campaign_name">Campaign name</label>
                    <input type="text" id="campaign_name" placeholder="Es. BUCCAMP0001"></input>

                    <label htmlFor="campaign_id">Sub-campaign ID</label>
                    <input type="text" id="campaign_id" placeholder="Es. Christmas Party"></input>
                </fieldset>

                <h3>Add attendees</h3>

                <fieldset className="new_attendee">

                    <div className="fields_column">
                        
                        <div className="fields">
                            <label htmlFor="first_name">First name</label>
                            <input id="first_name" type="text" placeholder="Es. Mario" />
                        </div>

                        <div className="fields">
                            <label htmlFor="partner_id">Partner Id</label>
                            <input id="partner_id" type="text" placeholder="Es. Maria Rossi" />
                        </div>
                        
                        <div className="fields">
                            <label htmlFor="cn_code">CN Code</label>
                            <input id="cn_code" type="text" placeholder="Es. 1234" />
                        </div>

                    </div>


                    <div className="fields_column">

                        <div className="fields">
                            <label htmlFor="last_name">Last name</label>
                            <input id="last_name" type="text" placeholder="Es. Rossi" />
                        </div> 
                        
                        <div className="fields">
                            <label htmlFor="is_companion">IsCompanion</label>
                            <input id="is_companion" role="switch" type="checkbox" />
                        </div> 
                        
                        <div className="fields">
                            <label htmlFor="qr_code">Qr Code</label>
                            <input id="qr_code" type="text" placeholder="Es. xxxxxx" />
                        </div>

                    </div>


                    <div className="fields_column">
                        
                        <div className="fields">
                            <label htmlFor="birth_date">Birth date</label>
                            <input id="birth_date" type="date" />
                        </div>

                    </div>

                
                    <Button text="+ Add Attendee" variant="primary" onClick={addAttendee} />  
                    <Button text="Change Attendee" variant="secondary" onClick={changeAttendee} />
                    <Button text="Delete Attendee" variant="danger" onClick={deleteAttendee} />
                
            
                </fieldset>
             
                <div className="fields_result">

                    <Button text="Submit Campaign" variant="primary" onClick={submitCampaign} />
                    <Button text="Clear Form" variant="danger" onClick={clearForm} />
                
                </div>
                

                <hr></hr> 

            </form>
            
        </section>
        
    )
}

export default FormCampaign
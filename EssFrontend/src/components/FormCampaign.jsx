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

                    <div className="field">
                    
                        <label htmlFor="first_name">First name</label>
                        <input type="text" id="first_name" placeholder="Es. Mario"></input>
                        
                        <label htmlFor="partner_id">Partner Id</label>
                        <input type="text" id="partner_id" placeholder="Es. Maria Rossi"></input>
                        
                        <label htmlFor="cn_code">CN Code</label>
                        <input type="text" id="cn_code" placeholder="Es. 1234"></input>

                    </div>

                    <div className="field">

                       <label htmlFor="last_name">Last name</label>
                        <input type="text" id="last_name" placeholder="Es. Rossi"></input>

                        <Toggle />

                        <label htmlFor="qr_code">Qr Code</label>
                        <input type="text" id="qr_code" placeholder="Es. xxxxxx"></input>

                    </div>

                    <div className="fields">

                        <label htmlFor="birth_date">Birth date</label>
                        <input type="date" id="birth_date"></input>
                      
                    </div>

                    <div className="fields">

                       <Button text="+ Add Attendee" variant="primary" onClick={addAttendee} />
                       <Button text="Change Attendee" variant="secondary" onClick={changeAttendee} />
                       <Button text="Delete Attendee" variant="danger" onClick={deleteAttendee} />

                    </div>

                </fieldset>

                <hr></hr> 

                <div className="fields_result">

                    <Button text="Submit Campaign" variant="primary" onClick={submitCampaign} />
                    <Button text="Clear Form" variant="danger" onClick={clearForm} />
                
                </div>

            </form>
            
        </section>
        
    )
}

export default FormCampaign
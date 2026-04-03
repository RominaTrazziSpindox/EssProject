function FormCampaign() {



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
                    <label htmlFor="campaign_name">CN Code</label>
                    <input type="text" id="cn_code" placeholder="Es. 1234"></input>

                    <label htmlFor="birth_date">Birth date</label>
                    <input type="date" id="birth_date"></input>

                    <label htmlFor="first_name">First name</label>
                    <input type="text" id="first_name" placeholder="Es. Mario"></input>

                    <label htmlFor="last_name">Last name</label>
                    <input type="text" id="first_name" placeholder="Es. Rossi"></input>

                    <label htmlFor="partner_id">Partner Id</label>
                    <input type="text" id="partner_id" placeholder="Es. Maria Rossi"></input>

                    <label className="toggle_switch" htmlFor="is_companion">Companion
                        <input type="checkbox" id="is_companion" role="switch"></input>
                        <span className="slider"></span>
                    </label>
    

                </fieldset>

                <hr></hr> 
                <button>Submit campaign</button>
                <button>Clear form</button>

            </form>
            
           

        </section>
        
    )
}

export default FormCampaign
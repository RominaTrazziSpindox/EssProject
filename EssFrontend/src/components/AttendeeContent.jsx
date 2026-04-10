import Input from './Input';
import Toggle from './Toggle';

function AttendeeContent({ attendee, onFieldChange, isSubmitting, errors }) {
    
    // Helper function to handle input changes 
    const handleInputChange = (field, value) => {
        onFieldChange(attendee.rowId, field, value);
    };

    return (
        
        <div className="new_attendee_row">

            <div className="fields_column">
                <div className="fields">
                    <Input label="First name"  id={`first_name-${attendee.rowId}`} 
                    placeholder="Es. Mario" value={attendee.firstName}     
                    onChange={(event) => handleInputChange('firstName', event.target.value)}     
                    disabled={isSubmitting}  error={errors?.firstName}/>   
                </div>             
                        
                <div className="fields">       
                    <Input label="Partner ID" id={`partner_id-${attendee.rowId}`} placeholder="Es. 1002002"  
                    value={attendee.partnerId} onChange={(event) => handleInputChange('partnerId', event.target.value)}
                    disabled={isSubmitting} error={errors?.partnerId} />
                </div>

                <div className="fields">
                    <Input label="CN Code" id={`cn_code-${attendee.rowId}`} placeholder="Es. 1234" value={attendee.cn}
                    onChange={(event) => handleInputChange('cn', event.target.value)} disabled={isSubmitting}    
                    error={errors?.cn} />  
                </div>          
                
            </div>

            <div className="fields_column">
                <div className="fields">
                    <Input label="Last name" id={`last_name-${attendee.rowId}`} placeholder="Es. Rossi"
                    value={attendee.lastName}  onChange={(event) => handleInputChange('lastName', event.target.value)}  
                    disabled={isSubmitting} error={errors?.lastName} />  
                </div>         

                <div className="fields">
                    <Toggle id={`is_companion-${attendee.rowId}`} checked={attendee.isCompanion}
                    onChange={(event) => handleInputChange('isCompanion', event.target.checked)}    
                    disabled={isSubmitting} />    
                </div>

                <div className="fields">
                    <Input label="Qr Code" id={`qr_code-${attendee.rowId}`} placeholder="Es. xxxxxx" 
                    value={attendee.qrCode} onChange={(event) => handleInputChange('qrCode', event.target.value)}   
                    disabled={isSubmitting} error={errors?.qrCode} />
                </div>
            </div>

            <div className="fields_column">
                <div className="fields">
                    <Input type="date" label="Birth date" id={`birth_date-${attendee.rowId}`} value={attendee.birthDate}
                    onChange={(event) => handleInputChange('birthDate', event.target.value)} disabled={isSubmitting}   
                    error={errors?.birthDate} />   
                </div>
            </div>
        </div>
    );
}

export default AttendeeContent;

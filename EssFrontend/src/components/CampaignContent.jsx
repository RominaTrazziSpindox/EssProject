import PropTypes from 'prop-types';

import Input from "./Input";

CampaignContent.propTypes = {
    formData: PropTypes.object.isRequired,
    onChange: PropTypes.func.isRequired,
    errors: PropTypes.object,
    isSubmitting: PropTypes.bool.isRequired,
};

CampaignContent.defaultProps = {
    errors: {},
};

function CampaignContent({ formData, onChange, errors, isSubmitting }) {
    return (
        <fieldset className="new_campaign">
            <Input label="Campaign ID"  id="campaign_id" name="campaignId" placeholder="Es. C-00088102" 
            value={formData.campaignId} onChange={onChange} disabled={isSubmitting} error={errors?.campaignId}/>      
               
            <Input label="Sub-campaign ID" id="sub_campaign_id" name="subCampaignId" placeholder="Es. SC-0091"      
            value={formData.subCampaignId} onChange={onChange} disabled={isSubmitting}/>  
        
        </fieldset>        
    ); 
}

export default CampaignContent;
  

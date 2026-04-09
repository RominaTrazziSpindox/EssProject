import Input from "./Input";

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
  

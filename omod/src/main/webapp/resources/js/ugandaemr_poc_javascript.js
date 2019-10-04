
function enable_fields(group){
    var disable = false;
    var group = jq("#"+group);
    group.find("input").attr("disabled", disable);
    group.find('select').attr("disabled", disable);

    if (!disable) {
        /* remove the fade on the fields */
        group.find("input").fadeTo(250, 1);
        group.find("select").fadeTo(250, 1);
    }
}
function enable_disable(triggerId, enabledDisabledId, conditionValue) {
    if (getValue(triggerId + '.value') == conditionValue){

        enableContainer('#' + enabledDisabledId);
    }
    else {
        if(getValue(enabledDisabledId+'.value')!=''){
            setValue(enabledDisabledId+'.value', '');
        }
        disableContainer('#' + enabledDisabledId);
    }

}
function disable_fields(groupId){
    var disable = true;
    var group = jq("#"+groupId);
    group.find("input").attr("disabled", disable);
    group.find('select').attr("disabled", disable);

    if (disable) {
        /* fade out the fields that are disabled */
        group.find("input").fadeTo(250, 0.25);
        group.find("select").fadeTo(250, 0.25);
    } else {
        /* remove the fade on the fields */
        group.find("input").fadeTo(250, 1);
        group.find("select").fadeTo(250, 1);
    }
}
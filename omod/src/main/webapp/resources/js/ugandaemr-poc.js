
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

function enable_disable_fm(selected_option) {

    var class_name = jq(selected_option).attr("class");

    var length = class_name.length;

    var class_id = parseInt(class_name.substring(length - 1, length));

    var row_id = class_id;

    var disable = true;
    var disable1 = true;

    var row_1 = '[class^="FamilyMember"][class*="Children1"]';
    var row_2 = '[class^="FamilyMember"][class*="Children2"]';
    var row_3 = '[class^="FamilyMember"][class*="Children3"]';
    var row_4 = '[class^="FamilyMember"][class*="Children4"]';
    var row_5 = '[class^="FamilyMember"][class*="Children5"]';

    var xx = row_1;
    var selected_value = jq(selected_option).find(":selected").text();


    if (selected_value == "P") {
        disable = false;
    }
    else if (selected_value == "Y") {
        disable1 = false;
    }

    switch (row_id) {
        case 1:
            xx = row_1;
            break;
        case 2:
            xx = row_2;
            break;
        case 3:
            xx = row_3;
            break;
        case 4:
            xx = row_4;
            break;
        case 5:
            xx = row_5;
            break;
    }


    jq(xx).each(function () {
        var group = jq(this);
        if (class_name.indexOf('Children') == -1) {
            /* group.find('select').prop("selectedIndex", 0);*/
            group.find('select').attr("disabled", disable);
            if (disable) {
                /* fade out the fields that are disabled*/
                group.find("select").fadeTo(250, 0.25);
            } else {
                /* remove the fade on the fields*/
                group.find("select").fadeTo(250, 1);
            }
        }
        if (class_name.indexOf('GrandChildren') == -1) {
            /* group.find("input").attr("value", "");*/
            group.find("input").attr("disabled", disable1);

            if (disable1) {
                /* fade out the fields that are disabled*/
                group.find("input").fadeTo(250, 0.25);
            } else {
                /* remove the fade on the fields*/
                group.find("input").fadeTo(250, 1);
            }
        }

    });

}

function enable_disable(field, class_name_prefix, conditions, input_type) {

    var class_name = jq(field).attr("class");

    var length = class_name.length;

    var class_id = parseInt(class_name.substring(length - 1, length));
    var childClass = "Child" + class_id;

    var disable = true;
    var requires = true;

    var row = '[class^="' + class_name_prefix + '"][class*="' + childClass + '"]';

    var selected_value = null;

    if (input_type == "select") {
        selected_value = jq(field).find(":selected").text().trim().toLowerCase();
    }
    else if (input_type == "hidden") {
        selected_value = jq(field).find("input[type=hidden]").val().trim().toLowerCase();
    }


    if (eval(conditions)) {
        disable = false;
    }


    jq(row).each(function () {
        var group = jq(this);

        if (class_name.indexOf('Child') == -1) {
            /*group.find("input").attr("value", ""); */
            /* group.find('select').prop("selectedIndex", 0);*/
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
    });

    jq(".checkboxGroup").each(function () {
        var group = jq(this);
        var uncheckAll = function () {
            group.find("input[type$='checkbox']").attr("checked", false);
            group.find("input[type$='checkbox']").change();
        }

        var uncheckRadioAndAll = function () {
            group.find("#checkboxAll,#checkboxRadio").find("input[type$='checkbox']").attr("checked", false);
            group.find("#checkboxAll,#checkboxRadio").find("input[type$='checkbox']").change();
        }


        group.find("#checkboxAll").find("input").click(
            function () {
                var flip;
                var checked = jq(this).siblings(":checkbox:first").attr("checked");
                if (jq(this).attr("name") == jq(this).parents("#checkboxAll:first").find(":checkbox:first").attr("name")) {
                    checked = jq(this).attr("checked");
                    flip = checked;
                } else {
                    flip = !checked;
                }
                if (jq.attr("type") == "text") if (flip == false) flip = !filp;
                /* this is so the user doesn't go to check thecheckbox, then uncheck it when they hit the input.uncheckAll();*/
                jq(this).parents("#checkboxAll:first").find(":checkbox").attr("checked", flip);
                jq(this).parents("#checkboxAll:first").find(":checkbox").change();
            }
        );


        group.find("#checkboxRadio").find("input[type$='checkbox']").click(function () {
            uncheckAll();
            jq(this).siblings("input[type$='checkbox']").attr("checked", false);
            jq(this).attr("checked", true);
            jq(this).change();
        });

        group.find("#checkboxCheckbox").click(
            function () {
                uncheckRadioAndAll();
            }
        );
    });
}

/* Since MUAC Codes varry from age to age, this function will check the specific age group
for the client and return their respective MUAC Code using the MUAC Score provided.
The MUAC Score is entered in the provided textbox which has an id="muac-code", this function
picks up this value and compares it against a list of available MUAC Codes hidden in the select box
which as an id ="muac-score". The MUAC Codes are categorized in age groups, this functions checks the
right code based on the client age.*/

function getMUACCodeFromMUACScoreByAge(age) {

    jq("#muac-score").find("input[type$='text']").keyup(function() {

        var muacScore = jq(this).val();

        if(jq("#muac-score input").val()=="" || jq("#muac-score input").val()==0) {
            jq("#muac-code").find("select").val('').attr('selected', 'selected')
            return false;
        }

        if(age < 5) {

            if(muacScore < 11.5) {
                jq("#muac-code").find("select").val(99028).attr('selected', 'selected')
            }

            if(muacScore >= 11.5 && muacScore < 12.5) {
                jq("#muac-code").find("select").val(99029).attr('selected', 'selected')
            }

            if(muacScore >=12.5) {
                jq("#muac-code").find("select").val(99027).attr('selected', 'selected')
            }
        }

        if(age >= 5 && age < 10) {

            if(muacScore < 13.5) {
                jq("#muac-code").find("select").val(99028).attr('selected', 'selected')
            }

            if(muacScore >=13.5 && muacScore < 14.5) {
                jq("#muac-code").find("select").val(99029).attr('selected', 'selected')
            }

            if(muacScore >=14.5) {
                jq("#muac-code").find("select").val(99027).attr('selected', 'selected')
            }

        }

        if(age >=10 && age < 18) {

            if(muacScore < 16.5) {
                jq("#muac-code").find("select").val(99028).attr('selected', 'selected')
            }

            if(muacScore >=16.5 && muacScore < 19) {
                jq("#muac-code").find("select").val(99029).attr('selected', 'selected')
            }

            if(muacScore >=19) {
                jq("#muac-code").find("select").val(99027).attr('selected', 'selected')
            }

        }

        if(age >=18) {

            if(muacScore < 19) {
                jq("#muac-code").find("select").val(99028).attr('selected', 'selected')
            }

            if(muacScore >=19 && muacScore < 22) {
                jq("#muac-code").find("select").val(99029).attr('selected', 'selected')
            }

            if(muacScore >=22) {
                jq("#muac-code").find("select").val(99027).attr('selected', 'selected')
            }
        }

        jq("#muac-code").change(function () {
            jq("#muac-score").find("input[type$='text']").val('');
        });
    });
}

//Disable encounter date field.
function disableEncounterDate() {
    $encounterDateField = jq('#encounterDate').find('input[type="text"]');
    $encounterDateField.addClass('disabled');
    $encounterDateField.attr('disabled', 'disabled');
}
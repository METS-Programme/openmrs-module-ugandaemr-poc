<script>
    jq(function () {
        jq('#date').datepicker("option", "dateFormat", "dd/mm/yy");
    });

    var editPrescriptionDialog,
        editPrescriptionForm,
        editPrescriptionParameterOpts = {editPrescriptionParameterOptions: ko.observableArray([])},
        printPrescriptionParameterOpts = {printPrescriptionParameterOptions: ko.observableArray([])};

    jq(function () {
        ko.applyBindings(editPrescriptionParameterOpts, jq("#edit-prescription-form")[0]);

        editPrescriptionDialog = emr.setupConfirmationDialog({
            dialogOpts: {
                overlayClose: false,
                close: true
            },
            selector: '#edit-prescription-form',
            actions: {
                confirm: function () {
                    saveEditResult();
                    editPrescriptionDialog.close();
                },
                cancel: function () {
                    editPrescriptionDialog.close();
                }
            }
        });

        editPrescriptionForm = jq("#edit-prescription-form").find("form").on("submit", function (event) {
            event.preventDefault();
            saveEditResult();
        });
    });

    function showEditPrescriptionForm(encounter, position) {
        getEditPrescriptionTempLate(pharmacyData, encounter, position);
        editPrescriptionForm.find("#edit-prescription-id").val(encounter);

    }

    function getDrugOrderData(pharmacyQueueList, encounterId, position) {
        var orderedTestsRows = [];
        jq.each(pharmacyQueueList.patientPharmacyQueueList[position].orderMapper, function (index, element) {
            if (element.encounterId === encounterId) {
                orderedTestsRows.push(element);
            }
        });
        return orderedTestsRows;
    }

    function getEditPrescriptionTempLate(pharmacyData, encounterId, position) {
        var editPrescriptionParameterOptions = getDrugOrderData(pharmacyData, encounterId, position);
        jq.each(editPrescriptionParameterOptions, function (index, editPrescriptionParameterOption) {
            editPrescriptionParameterOpts.editPrescriptionParameterOptions.push(editPrescriptionParameterOption);
        });

        editPrescriptionDialog.show();
    }

    function saveEditResult() {
        var dataString = editPrescriptionForm.serialize();
        jq.ajax({
            type: "POST",
            url: '${ui.actionLink("ugandaemrpoc", "pharmacyQueueList", "dispense")}',
            data: dataString,
            dataType: "json",
            success: function (data) {
                if (data.status === "success") {
                    jq().toastmessage('showSuccessToast', data.message);
                    editPrescriptionDialog.close();
                    window.location.reload();
                } else if (data.referredOutPrescriptions !== "") {
                    var dataToPrint= JSON.parse(data.referredOutPrescriptions);
                    printPrescription(dataToPrint);
                }
            }
        });
    }

    function Result() {
        self = this;
        self.items = ko.observableArray([]);
    }

    function printPrescription(dataToPrint) {
        var divToPrint = document.getElementById("printSection");
        var newWin = window.open('', 'Print-Window');

        jq.each(dataToPrint, function (index, dataToPrint) {
          var rowToAppendToTable="<tr><td width='30%' style='text-align: center;'>"+dataToPrint.conceptName+"</td><td width='30%' style='text-align: center;'>"+dataToPrint.quantity+"</td><td width='30%' style='text-align: center;'>"+dataToPrint.duration+" "+dataToPrint.durationUnits+"</td></tr>";
            jq("#containerToAppendRefferedOutPrescriptions").append(rowToAppendToTable);
        });
        newWin.document.open();
        newWin.document.write('<html><body onload="window.print()">' + divToPrint.innerHTML + '</body></html>');
        newWin.document.close();
        setTimeout(function () {
            newWin.close();
            editPrescriptionDialog.close();
            window.location.reload();
        }, 10);
    }

    var prescription = new Result();
</script>
<style>
form .lablex {
    width: 93%;
}

form .lablex span {
    float: left;
    margin: 1px 0px;
}

form .lablex2 span {
    min-width: 50%;
    float: left;
    margin: 1px 0px;
}

form .lablex3 span {
    min-width: 30%;
    float: left;
    margin: 1px 0px;
}

form .lablex4 span {
    min-width: 25%;
    float: left;
    margin: 1px 0px;
}

form .lablex5 span {
    min-width: 20%;
    float: left;
    margin: 1px 0px;
}

form .lablex3 {
}

form .lablex3 span {
    min-width: 70%;
    float: left;
    margin: 1px 0px;
}

form input {
    min-width: 47%;
}

.div-table {
    display: table;
    width: 100%;
    background-color: #fff;
}

.box {
    display: flex;
    flex-wrap: wrap;
}

.box > * {
    flex: 1 1 160px;
}

.div-row {
    display: table-row;
    width: 100%;
}

.div-col1 {
    display: table-cell;
    margin-left: auto;
    margin-right: auto;
    width: 100%;
}

.div-col2 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 50%;
}

.div-col3 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 33%;
}

.div-col4 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 25%;
}

.div-col5 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 20%;
}

.div-col6 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 16%;
}

.dialog {
    width: 70%;
}

.dialog .dialog-content {
    padding: 0px 19px 0 19px;
}

.dialog .dialog-header {
    background: #151414;
}

.prescription-checkbox {
    width: 20px;
}

.print-only {
    display: none;
}
</style>

<div id="edit-prescription-form" title="Prescription" class="modal fade bd-order-modal-lg" style="">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h3 style="color: #FFFFFF">${ui.message("Dispense Medication order for")}</h3>
            </div>

            <div class="modal-body">
                <form>
                    <input type="hidden" name="wrap.encounterId" id="edit-prescription-id"/>
                    <table>
                        <thead>
                        <th>Drug Name</th>
                        <th>Prescribed Quantity</th>
                        <th>Prescription Period</th>
                        <th>Dispense Quantity</th>
                        <th>Dispense Period</th>
                        <th>Refer Out</th>

                        </thead>
                        <tbody class="container" data-bind="foreach: editPrescriptionParameterOptions">
                        <tr>
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].concept' }, value: encounter?encounter+'.'+concept:concept">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].conceptName' }, value: encounter?encounter+'.'+conceptName:conceptName">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].dose' }, value: encounter?encounter+'.'+dose:dose">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].doseUnits' }, value: encounter?encounter+'.'+doseUnits:doseUnits">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].frequency' }, value: encounter?encounter+'.'+frequency:frequency">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].quantityUnits' }, value: encounter?encounter+'.'+quantityUnits:quantityUnits">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].numRefills' }, value: encounter?encounter+'.'+numRefills:numRefills">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].dosingInstructions' }, value: encounter?encounter+'.'+dosingInstructions:dosingInstructions">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].durationUnits' }, value: encounter?encounter+'.'+durationUnits:durationUnits">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].route' }, value: encounter?encounter+'.'+route:route">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].drugNonCoded' }, value: encounter?encounter+'.'+drugNonCoded:drugNonCoded">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].drugNonCoded' }, value: encounter?encounter+'.'+drugNonCoded:drugNonCoded">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].orderNumber' }, value: encounter?encounter+'.'+orderNumber:orderNumber">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].orderId' }, value: encounter?encounter+'.'+orderId:orderId">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].patient' }, value: encounter?encounter+'.'+patient:patient">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].patientId' }, value: encounter?encounter+'.'+patientId:patientId">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].orderType' }, value: encounter?encounter+'.'+orderType:orderType">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].instructions' }, value: encounter?encounter+'.'+instructions:instructions">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].dateActivated' }, value: encounter?encounter+'.'+dateActivated:dateActivated">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].autoExpireDate' }, value: encounter?encounter+'.'+autoExpireDate:autoExpireDate">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].encounter' }, value: encounter?encounter+'.'+encounter:encounter">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].orderer' }, value: encounter?encounter+'.'+orderer:orderer">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].dateStopped' }, value: encounter?encounter+'.'+dateStopped:dateStopped">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].orderReason' }, value: encounter?encounter+'.'+orderReason:orderReason">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].accessionNumber' }, value: encounter?encounter+'.'+accessionNumber:accessionNumber">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].urgency' }, value: encounter?encounter+'.'+urgency:urgency">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].commentToFulfiller' }, value: encounter?encounter+'.'+commentToFulfiller:commentToFulfiller">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].careSetting' }, value: encounter?encounter+'.'+careSetting:careSetting">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].scheduledDate' }, value: encounter?encounter+'.'+scheduledDate:scheduledDate">
                            <input type="hidden"
                                   data-bind="attr: { 'name' : 'wrap.drugOrderMappers[' + \$index() + '].status' }, value: encounter?encounter+'.'+status:status">
                            <!--Other Input Types-->

                            <td data-bind="">
                                <div id="data">
                                    <span data-bind="if:drug && drug.toUpperCase() === 'WRITE COMMENT'">
                                        <label data-bind="text: drug + ' (' + container+')'"></label>
                                    </span>

                                    <span data-bind="if:drug && drug.toUpperCase() !== 'WRITE COMMENT'">
                                        <label data-bind="text: drug"></label>
                                    </span>
                                </div>
                            </td>
                            <td data-bind="">
                                <div id="data">
                                    <span data-bind="if:drug && drug.toUpperCase() === 'WRITE COMMENT'">
                                        <label data-bind="text: quantity + ' (' + container+')'"></label>
                                    </span>

                                    <span data-bind="if:drug && drug.toUpperCase() !== 'WRITE COMMENT'">
                                        <label data-bind="text: quantity"></label>
                                    </span>
                                </div>
                            </td>
                            <td data-bind="">
                                <div id="data">
                                    <span data-bind="if:drug && drug.toUpperCase() === 'WRITE COMMENT'">
                                        <label data-bind="text: dose + ' (' + container+')'"></label>
                                    </span>

                                    <span data-bind="if:drug && drug.toUpperCase() !== 'WRITE COMMENT'">
                                        <label data-bind="text: dose"></label>
                                    </span>
                                </div>
                            </td>
                            <td data-bind="">
                                <div id="data">
                                    <input class="prescription-text"
                                           data-bind="attr : { 'type' : 'number', 'name' : 'wrap.drugOrderMappers[' + \$index() + '].quantity', value : '' }">
                                </div>
                            </td>

                            <td data-bind="">
                                <div id="data">
                                    <input class="prescription-text"
                                           data-bind="attr : { 'type' : 'number', 'name' : 'wrap.drugOrderMappers[' + \$index() + '].duration', value : '' }">
                                </div>
                            </td>
                            <td data-bind="">
                                <div id="data">
                                    <input class="prescription-checkbox"
                                           data-bind="attr : { 'type' : 'checkbox', 'name' : 'wrap.drugOrderMappers[' + \$index() + '].orderReasonNonCoded', value : 'REFERREDOUT' }">
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </form>
            </div>

            <div class="modal-footer">
                <button class="cancel" data-dismiss="modal"
                        id="">${ui.message("patientqueueing.close.label")}</button>
                <span class="button confirm right">Dispense</span>
            </div>
        </div>
    </div>
</div>

<div id="printSection" class="print-only">
    <center>
        <div style="width: 60%">
            <div><img width="150px" src="${ui.resourceLink("aijar", "images/moh_logo_large.png")}"/></div>

            <div><h3>PRESCRIPTION FROM : HEALTH CENTER NAME</h3></div>
            <hr style="border: 1px solid red;"/>

            <div id="prescription_receipt" align="left">
                <table>
                    <thead>
                    <th width="30%" style="text-align: center;">Drug Name</th>
                    <th width="30%" style="text-align: center;">Prescribed Quantity</th>
                    <th width="30%" style="text-align: center;">Prescription Period</th>

                    </thead>
                    <tbody id="containerToAppendRefferedOutPrescriptions">

                    </tbody>
                </table>
            </div>
        </div>
    </center>
</div>






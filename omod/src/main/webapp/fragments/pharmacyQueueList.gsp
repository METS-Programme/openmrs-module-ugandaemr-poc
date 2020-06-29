<% if (clinicianLocation?.contains(currentLocation?.uuid)) {

    ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
    ui.includeJavascript("patientqueueing", "patientqueue.js")
    ui.includeJavascript("aijar", "js/aijar.js")
%>

<style>
button, input {
    line-height: normal;
    width: 92px;
}
.fade:not(.show) {
     opacity: 1;
}
</style>
<script>
    jq(document).ready(function () {
        jq("#tabs").tabs();
    })
    if (jQuery) {
        jq(document).ready(function () {
            jq(document).on('sessionLocationChanged', function () {
                window.location.reload();
            });
            var pharmacyData = null;
            jq("#clinician-list").hide();
            getPharmacyQueue();
            jq("#patient-pharmacy-search").change(function () {
                if (jq("#patient-pharmacy-search").val().length >= 3) {
                    getPharmacyQueue();
                }
            });
        });
    }

    jq("form").submit(function (event) {
        alert("Handler for .submit() called.");
    });

    function getPharmacyQueue() {
        jq("#pharmacy-pending-list-table").html("");
        jq.get('${ ui.actionLink("getPharmacyQueueList") }', {
            pharmacySearchFilter: jq("#patient-pharmacy-search").val().trim().toLowerCase()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("patientPharmacyQueueList=", "\"patientPharmacyQueueList\":").trim());
                pharmacyData = responseData;
                displaypharmacyData(responseData);
            } else if (!response) {
                jq("#pharmacy-pending-list-table").append(${ ui.message("coreapps.none ") });
            }
        });
    }

    function displaypharmacyData(response) {
        jq("#pharmacy-pending-list-table").val("");
        var prescriptions = "";
        var drugRefill = "";
        var completed = "";
        var prescriptionCount = 0;
        var drugRefillCount = 0;
        var completedCount = 0;
        prescriptions = "<table><thead><tr><th>Visit No.</th><th>Names</th><th>Age</th><th>ORDER FROM</th><th>WAITING TIME</th><th>ACTION</th></tr></thead><tbody>";
        drugRefill = "<table><thead><tr><th>Visit No.</th><th>Names</th><th>Age</th><th>FROM</th><th>WAITING TIME</th><th>ACTION</th></tr></thead><tbody>";
        completed = "<table><thead><tr><th>Visit No.</th><th>Names</th><th>Age</th><th>ACTION</th></tr></thead><tbody>";

       var dataToDisplay=[];

        if(response.patientPharmacyQueueList.length>0){
            dataToDisplay=response.patientPharmacyQueueList.sort(function (a, b) {
                return a.patientQueueId - b.patientQueueId;
            });
        }

        jq.each(dataToDisplay, function (index, element) {
                var patientQueueListElement = element;


                var ordersNo = noOfDrugPrescriptions(element);

                var waitingTime = getWaitingTime(patientQueueListElement.dateCreated, patientQueueListElement.dateChanged);

                var visitNumber = "";
                if (patientQueueListElement.visitNumber != null) {
                    visitNumber = patientQueueListElement.visitNumber.substring(15);
                }

                if (ordersNo > 0) {
                    prescriptions += "<tr>";
                    prescriptions += "<td>" + visitNumber + "</td>";
                    prescriptions += "<td>" + patientQueueListElement.patientNames + "</td>";
                    prescriptions += "<td>" + patientQueueListElement.age + "</td>";
                    prescriptions += "<td>" + patientQueueListElement.providerNames + " - " + patientQueueListElement.locationFrom + "</td>";
                    prescriptions += "<td>" + waitingTime + "</td>";
                    prescriptions += "<td><a title=\"Dispense Medication\" onclick='showEditPrescriptionForm(" + patientQueueListElement.encounterId + "," + patientQueueListElement.patientQueueId + "," + index + ")'>Dispense Medication <i class=\"icon-list-ul small\"></i></a> <span style=\"color: red;\">" + ordersNo + "</span></td>";
                    prescriptions += "</tr>";
                    prescriptionCount += 1;
                } else if (ordersNo <= 0 && patientQueueListElement.status !== "COMPLETED") {
                    var newDispensingFormURL = "";

                    if (patientQueueListElement.visitId !== null) {
                        newDispensingFormURL = "/" + OPENMRS_CONTEXT_PATH + "/htmlformentryui/htmlform/enterHtmlFormWithStandardUi.page?patientId=" + patientQueueListElement.patientId + "&visitId=" + patientQueueListElement.visitId + "&formUuid=340fe8d8-4984-11ea-b77f-2e728ce88125&returnUrl=" + "/" + OPENMRS_CONTEXT_PATH + "/patientqueueing/providerDashboard.page";
                    }
                    var action = "<i style=\"font-size: 25px;\" class=\"icon-edit edit-action\" title=\"Dispense Medication\" onclick=\" location.href = '" + newDispensingFormURL + "'\"></i>";
                    drugRefill += "<tr>";
                    drugRefill += "<td>" + visitNumber + "</td>";
                    drugRefill += "<td>" + patientQueueListElement.patientNames + "</td>";
                    drugRefill += "<td>" + patientQueueListElement.age + "</td>";
                    drugRefill += "<td>" + patientQueueListElement.locationFrom + "</td>";
                    drugRefill += "<td>" + waitingTime + "</td>";
                    drugRefill += "<td>" + action + "</td>";
                    drugRefill += "</tr>";
                    drugRefillCount += 1;
                } else if (patientQueueListElement.encounterId != null && patientQueueListElement.status === "COMPLETED") {
                    var editDispensingFormURL = "/" + OPENMRS_CONTEXT_PATH + "/htmlformentryui/htmlform/editHtmlFormWithStandardUi.page?patientId=" + patientQueueListElement.patientId + "&formUuid=340fe8d8-4984-11ea-b77f-2e728ce88125&encounterId=" + patientQueueListElement.encounterId + "&visitId=" + patientQueueListElement.visitId + "&returnUrl=" + "/" + OPENMRS_CONTEXT_PATH + "/patientqueueing/providerDashboard.page";
                    var action = "<i style=\"font-size: 25px;\" class=\"icon-edit edit-action\" title=\"Edit Medication Dispensed\" onclick=\" location.href = '" + editDispensingFormURL + "'\"></i>";
                    completed += "<tr>";
                    completed += "<td>" + visitNumber + "</td>";
                    completed += "<td>" + patientQueueListElement.patientNames + "</td>";
                    completed += "<td>" + patientQueueListElement.age + "</td>";
                    completed += "<td>" + action + "</td>";
                    completed += "</tr>";
                    completedCount += 1;
                }
            }
        );

        prescriptions += "</tbody></table>";
        jq("#pharmacy-pending-list-table").append(prescriptions);
        jq("#pharmacy-pending-number").html("");
        jq("#pharmacy-pending-number").append("   " + prescriptionCount);

        jq("#pharmacy-drugrefill-list-table").append(drugRefill);
        jq("#pharmacy-drugrefill-number").html("");
        jq("#pharmacy-drugrefill-number").append("   " + drugRefillCount);

        jq("#pharmacy-completed-list-table").append(completed);
        jq("#pharmacy-completed-number").html("");
        jq("#pharmacy-completed-number").append("   " + completedCount);
    }

    function noOfDrugPrescriptions(drugList) {
        var orderCount = 0;
        jq.each(drugList.orderMapper, function (index, element) {
            if (element.accessionNumber === null && element.status === "active") {
                orderCount += 1;
            }
        });
        return orderCount;
    }

    function displayPharmacyOrderData(pharmacyQueueList, removeProccesedOrders) {
        var header = "<form id='" + pharmacyQueueList.patientId + "'><table><thead></thead><tbody>";
        var footer = "</tbody></table><input type='button' class='confirm' name='dispense' value='Dispense'><input type='hidden' id='" + pharmacyQueueList.encounterId + "'/></form>";
        var orderedTestsRows = "";
        jq.each(pharmacyQueueList.orderMapper, function (index, element) {
            if (removeProccesedOrders !== false && element.accessionNumber === null && element.status === "active") {
                orderedTestsRows += "<tr>";
                orderedTestsRows += "<td>" + element.drug + "</td>";
                orderedTestsRows += "<td>" + element.quantity + " " + element.quantityUnits + "</td>";
                orderedTestsRows += "<td><input type='text' id='" + pharmacyQueueList.encounterId + "-" + element.drug + "' class='drug-quantity' placeholder='Quantity'/></td>";
                orderedTestsRows += "</tr>";
            }
        });
        if (orderedTestsRows !== "") {
            return header + orderedTestsRows + footer;
        } else {
            return null;
        }
    }

</script>

<div class="card">
    <div class="card-header">
        <div class="">
            <div class="row">
                <div class="col-3">
                    <div>
                        <h2 style="color: maroon">${currentLocation.name} - ${ui.message("Queue")}</i></h2>
                    </div>

                    <div>
                        <h2>${currentProvider?.personName?.fullName}</h2>
                    </div>

                    <div class="vertical"></div>
                </div>

                <div class="col-8">
                    <form method="get" id="patient-search-form" onsubmit="return false">
                        <input type="text" id="patient-pharmacy-search"
                               placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                               autocomplete="off" class="provider-dashboard-patient-search"/>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="card-body">
        <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
            <li class="nav-item">
                <a class="nav-item nav-link active" id="home-tab" data-toggle="tab" href="#pharmacy-pending" role="tab"
                   aria-controls="pharmacy-pending-tab" aria-selected="true">Prescriptions List <span style="color:red"
                                                                                                      id="pharmacy-pending-number">0</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" id="profile-tab" data-toggle="tab" href="#pharmacy-drugrefill" role="tab"
                   aria-controls="pharmacy-drugrefill-tab" aria-selected="false">Non Prescription List<span
                        style="color:red"
                        id="pharmacy-drugrefill-number">0</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" id="contact-tab" data-toggle="tab" href="#pharmacy-completed" role="tab"
                   aria-controls="pharmacy-completed-number-tab" aria-selected="false">Completed <span
                        style="color:red" id="pharmacy-completed-number">0</span></a>
            </li>
        </ul>

        <div class="tab-content" id="myTabContent">
            <div class="tab-pane fade show active" id="pharmacy-pending" role="tabpanel"
                 aria-pharmacyelledby="pharmacy-pending-tab">
                <div class="info-body">
                    <div id="pharmacy-pending-list-table">
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="pharmacy-drugrefill" role="tabpanel"
                 aria-pharmacyelledby="pharmacy-drugrefill-tab">
                <div class="info-body">
                    <div id="pharmacy-drugrefill-list-table">
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="pharmacy-completed" role="tabpanel"
                 aria-pharmacyelledby="pharmacy-completed-number-tab">
                <div class="info-body">
                    <div id="pharmacy-completed-list-table">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
${ui.includeFragment("ugandaemrpoc", "pharmacy/dispensingForm",[healthCenterName:healthCenterName])}
<% } %>





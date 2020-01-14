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
        var content = "";
        var pharmacyStillInQueue = 0;
        var pharmacyDispensed = 0;
        var pharmacyReferred = 0;
        content = "<table><thead><tr><th>Visit No.</th><th>Names</th><th>Age</th><th>ORDER FROM</th><th>WAITING TIME</th><th>ACTION</th></tr></thead><tbody>";
        jq.each(response.patientPharmacyQueueList, function (index, element) {
                var ordersNo = noOfDrugPrescriptions(element);
                if (ordersNo > 0) {
                    var patientQueueListElement = element;
                    var waitingTime = getWaitingTime(patientQueueListElement.dateCreated);
                    content += "<tr>";
                    content += "<td>" + patientQueueListElement.visitNumber + "</td>";
                    content += "<td>" + patientQueueListElement.patientNames + "</td>";
                    content += "<td>" + patientQueueListElement.age + "</td>";
                    content += "<td>" + patientQueueListElement.providerNames + " - " + patientQueueListElement.locationFrom + "</td>";
                    content += "<td>" + waitingTime + "</td>";
                    content += "<td><a title=\"Dispense Medication\" onclick='showEditPrescriptionForm(" + patientQueueListElement.encounterId + ","+patientQueueListElement.patientQueueId+"," + index + ")'>Dispense Medication <i class=\"icon-list-ul small\"></i></a> <span style=\"color: red;\">" + ordersNo + "</span></td>";
                    content += "</tr>";
                    pharmacyStillInQueue += 1;
                }
            }
        );

        content += "</tbody></table>";
        jq("#pharmacy-pending-list-table").append(content);

        jq("#pharmacy-pending-number").html("");
        jq("#pharmacy-pending-number").append("   " + pharmacyStillInQueue);

        jq("#pharmacy-dispensed-number").html("");
        jq("#pharmacy-dispensed-number").append("   " + pharmacyDispensed);

        jq("#pharmacy-referred-out").html("");
        jq("#pharmacy-referred-out").append("   " + pharmacyReferred);
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


    //SUPPORTIVE FUNCTIONS//
    //Get Waiting Time For Patient In Queue
    function getWaitingTime(queueDate) {
        var diff = Math.abs(new Date() - new Date(queueDate));
        var seconds = Math.floor(diff / 1000); //ignore any left over units smaller than a second
        var minutes = Math.floor(seconds / 60);
        var waitingTime = "";
        seconds = seconds % 60;
        var hours = Math.floor(minutes / 60);
        minutes = minutes % 60;

        if (hours > 0 || minutes > 60) {
            waitingTime = "<span style='background-color: red; color: white; width: 100%; text-align: center;'>" + hours + ":" + minutes + ":" + seconds + "</span>";
        } else {
            waitingTime = "<span style='background-color:green; color: white; width: 100%; text-align: center;'>" + hours + ":" + minutes + ":" + seconds + "</span>";
        }
        return waitingTime;
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
                               autocomplete="off"/><i
                            id="patient-search-clear-button" class="small icon-remove-sign"></i>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="card-body">
        <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
            <li class="nav-item">
                <a class="nav-item nav-link active" id="home-tab" data-toggle="tab" href="#pharmacy-pending" role="tab"
                   aria-controls="pharmacy-pending-tab" aria-selected="true">Prescriptions <span style="color:red"
                                                                                                 id="pharmacy-pending-number">0</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" id="profile-tab" data-toggle="tab" href="#pharmacy-dispensed" role="tab"
                   aria-controls="pharmacy-dispensed-tab" aria-selected="false">Dispensed<span style="color:red"
                                                                                               id="pharmacy-dispensed-number">0</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" id="contact-tab" data-toggle="tab" href="#pharmacy-referred-out" role="tab"
                   aria-controls="pharmacy-referred-out-number-tab" aria-selected="false">Referred Out <span
                        style="color:red" id="pharmacy-referred-out">0</span></a>
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

            <div class="tab-pane fade" id="pharmacy-dispensed" role="tabpanel"
                 aria-pharmacyelledby="pharmacy-dispensed-tab">
                <div class="info-body">
                    <div id="pharmacy-dispensed-list-table">
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="pharmacy-refered-out" role="tabpanel"
                 aria-pharmacyelledby="pharmacy-referred-out-number-tab">
                <div class="info-body">
                    <div id="pharmacy-refered-out-list-table">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
${ui.includeFragment("ugandaemrpoc", "pharmacy/dispensingForm")}
<% } %>





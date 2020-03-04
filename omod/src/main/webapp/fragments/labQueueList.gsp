<% if (clinicianLocation?.contains(currentLocation?.uuid)) { %>
<%
        ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
        ui.includeJavascript("patientqueueing", "patientqueue.js")
        ui.includeJavascript("aijar", "js/aijar.js")
%>
<style>
.div-table {
    display: table;
    width: 100%;
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
    width: 550px;
}

.card-body {
    -ms-flex: 1 1 auto;
    flex: 7 1 auto;
    padding: 1.0rem;
    background-color: #eee;
}

.my-tab .tab-pane {
    border: solid 1px blue;
}

.vertical {
    border-left: 1px solid #c7c5c5;
    height: 79px;
    position: absolute;
    left: 99%;
    top: 11%;
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
            jq("#clinician-list").hide();
            getPatientLabQueue();
            getOrders();
            getResults();
            setSpecimenSource();
            jq("#patient-lab-search").change(function () {
                if (jq("#patient-lab-search").val().length >= 3) {
                    getPatientLabQueue();
                }
            });

            jq("#reference-lab-container").addClass('hidden');

            jq("#refer_test").change(function () {
                if (jq("#refer_test").is(":checked")) {
                    jq("#reference-lab-container").removeClass('hidden');
                } else {
                    jq("#reference-lab-container").addClass('hidden');
                }
            });

            jq("#submit-schedule").click(function () {
                jq.get('${ ui.actionLink("scheduleTest") }', {
                    orderNumber: jq("#order_id").val().trim().toLowerCase(),
                    sampleId: jq("#sample_id").val().trim().toLowerCase(),
                    referTest: jq("#refer_test").val().trim().toLowerCase(),
                    referenceLab: jq("#reference_lab").val().trim().toLowerCase(),
                    specimenSourceId: jq("#specimen_source_id").val().trim().toLowerCase()
                }, function (response) {
                    if (!response) {
                        ${ ui.message("coreapps.none ") }
                    }
                });
            });

            jq('#add-order-to-lab-worklist-dialog').on('show.bs.modal', function (event) {
                var button = jq(event.relatedTarget);
                var orderNumber = button.data('order-number');
                var modal = jq(this)
                modal.find("#order_id").val(orderNumber);
                modal.find("#sample_id").val("");
                modal.find("#sample_generator").html("");
                modal.find("#sample_generator").append("<a onclick=\"generateSampleId('" + orderNumber + "')\"><i class=\" icon-barcode\">Generate Sample Id</i></a>");
                modal.find("#reference_lab").prop('selectedIndex', 0);
                modal.find("#specimen_source_id").prop('selectedIndex', 0);
                modal.find("#refer_test input[type=checkbox]").prop('checked', false);
            });

        });
    }

    jq("form").submit(function (event) {
        alert("Handler for .submit() called.");
    });

    //GENERATION OF LISTS IN INTERFACE SUCH AS WORKLIST
    // Get Patients In Lab Queue
    function getPatientLabQueue() {
        jq("#pending-queue-lab-table").html("");
        jq.get('${ ui.actionLink("getPatientQueueList") }', {
            labSearchFilter: jq("#patient-lab-search").val().trim().toLowerCase()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("patientLabQueueList=", "\"patientLabQueueList\":").trim());
                displayLabData(responseData);
            } else if (!response) {
                jq("#pending-queue-lab-table").append(${ ui.message("coreapps.none ") });
            }
        });
    }

    // Gets Orders of List of WorkList and Refered Tests
    function getOrders() {
        jq.get('${ ui.actionLink("getOrders") }', {
            date: (new Date()).toString()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("ordersList=", "\"ordersList\":").trim());
                displayLabOrder(responseData)
            }
        });
    }

    // Gets Orders with results for The List of results
    function getResults() {
        jq.get('${ ui.actionLink("getOrderWithResult") }', {
            date: (new Date()).toString()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("ordersList=", "\"ordersList\":").trim());
                displayLabResult(responseData)
            }
        });
    }

    function displayLabData(response) {
        var content = "";
        var pendingCounter=0;
        content = "<table><thead><tr><th>Q ID</th><th>NAMES</th><th>AGE</th><th>ORDER FROM</th><th>WAITING TIME</th><th>TEST(S) ORDERED</th></tr></thead><tbody>";


        var dataToDisplay=[];

        if(response.patientLabQueueList.length>0){
            dataToDisplay=response.patientLabQueueList.sort(function (a, b) {
                return a.patientQueueId - b.patientQueueId;
            });
        }

        jq.each(dataToDisplay, function (index, element) {
                var orders = displayLabOrderData(element, true);
                if (orders !== null) {
                    var patientQueueListElement = element;
                    var waitingTime = getWaitingTime(patientQueueListElement.dateCreated);
                    content += "<tr>";
                    content += "<td>" + patientQueueListElement.patientQueueId + "</td>";
                    content += "<td>" + patientQueueListElement.patientNames + "</td>";
                    content += "<td>" + patientQueueListElement.age + "</td>";
                    content += "<td>" + patientQueueListElement.providerNames + " - " + patientQueueListElement.locationFrom + "</td>";
                    content += "<td>" + waitingTime + "</td>";
                    content += "<td><a class=\"icon-list-alt\" data-toggle=\"collapse\" href=\"#collapse-tab\" role=\"button\" aria-expanded=\"false\" aria-controls=\"collapseExample\"> <span style=\"color: red;\">TestNo</span> Tests Unproccessed</a>".replace("#collapse-tab", "#collapse-tab" + patientQueueListElement.patientQueueId).replace("TestNo", noOfTests(element));
                    content += "<div class=\"collapse\" id=\"collapse-tab" + patientQueueListElement.patientQueueId + "\"><div class=\"card card-body\">" + orders + "</div></div>";
                    content += "</td>";
                    content += "</tr>";

                    pendingCounter+=1;
                }
            }
        );
        content += "</tbody></table>";
        jq("#pending-queue-lab-table").append(content);
        jq("#pending-queue-lab-number").html("");
        jq("#pending-queue-lab-number").append("   " + pendingCounter);
    }

    function displayLabOrderData(labQueueList, removeProccesedOrders) {
        var header = "<table><thead></thead><tbody>";
        var footer = "</tbody></table>";
        var orderedTestsRows = "";
        var urlToPatientDashBoard = '${ui.pageLink("coreapps","clinicianfacing/patient",[patientId: "patientIdElement"])}'.replace("patientIdElement", labQueueList.patientId);
        jq.each(labQueueList.orderMapper, function (index, element) {
            if (removeProccesedOrders !== false && element.accessionNumber === null && element.status === "active") {
                var urlTransferPatientToAnotherQueue = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderNumber);
                orderedTestsRows += "<tr>";
                orderedTestsRows += "<td>" + element.conceptName + "</td>";
                orderedTestsRows += "<td>";
                orderedTestsRows += "<a  data-toggle=\"modal\" data-target=\"#add-order-to-lab-worklist-dialog\" data-order-number=\"orderNumber\" data-order-id=\"orderId\"><i style=\"font-size: 25px;\" class=\"icon-share\" title=\"Check In\"></i></a>".replace("orderNumber", element.orderNumber).replace("orderId", element.orderId);
                orderedTestsRows += "</td>";
                orderedTestsRows += "</tr>";
            }
        });



        if (orderedTestsRows !== "") {
            return header + orderedTestsRows + footer;
        } else {
            return null;
        }
    }

    function noOfTests(labQueueList) {
        var orderCount = 0;
        jq.each(labQueueList.orderMapper, function (index, element) {
            if (element.accessionNumber === null && element.status === "active") {
                orderCount += 1;
            }
        });
        return orderCount;
    }

    function displayLabOrder(labQueueList) {
        var referedTests = "";
        var workListTests = "";

        var tableHeader = "<table><thead><tr><th>SAMPLE ID</th><th>PATIENT NAME</th><th>TEST</th><th>STATUS</th><th>ACTION</th></tr></thead><tbody>";

        var tableFooter = "</tbody></table>";
        var refferedCounter = 0;
        var worklistCounter = 0;
        jq.each(labQueueList.ordersList, function (index, element) {
            var orderedTestsRows = "";
            var instructions = element.instructions;
            var actionIron = "";
            var actionURL = "";
            if (instructions != null && instructions.toLowerCase().indexOf("refer to") >= 0) {
                actionIron = "icon-tags edit-action";
                actionURL = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderId);
            } else {
                actionIron = "icon-tags edit-action";
                actionURL = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderId);
            }
            orderedTestsRows += "<tr>";
            orderedTestsRows += "<td>" + element.accessionNumber + "</td>";
            orderedTestsRows += "<td>" + element.patient + "</td>";
            orderedTestsRows += "<td>" + element.conceptName + "</td>";
            orderedTestsRows += "<td>" + element.status + "</td>";
            orderedTestsRows += "<td>";
            orderedTestsRows += "<a title=\"Edit Result\" onclick='showEditResultForm(" + element.orderId + ")'><i class=\"icon-list-ul small\"></i></a>";
            orderedTestsRows += "<i class=\" + actionIron + \" title=\"Transfer To Another Provider\" onclick='urlTransferPatientToAnotherQueue'></i>".replace("urlTransferPatientToAnotherQueue", actionURL);
            orderedTestsRows += "</td>";
            orderedTestsRows += "</tr>";
            if (element.status !== "has results") {
                if (instructions != null && instructions.toLowerCase().indexOf("refer to") >= 0) {
                    referedTests += orderedTestsRows;
                    refferedCounter += 1;
                } else {
                    workListTests += orderedTestsRows;
                    worklistCounter += 1;
                }
            }
        });

        jq("#lab-work-list-table").html("");
        jq("#referred-tests-list-table").html("");

        if (workListTests.length > 0) {
            jq("#lab-work-list-table").append(tableHeader + workListTests + tableFooter);
        } else {
            jq("#lab-work-list-table").append("No Data");
        }

        if (referedTests.length > 0) {
            jq("#referred-tests-list-table").append(tableHeader + referedTests + tableFooter);
        } else {
            jq("#referred-tests-list-table").append("No Data ");
        }

        jq("#lab-work-list-number").html("");
        jq("#lab-work-list-number").append("   " + worklistCounter);
        jq("#referred-tests-number").html("");
        jq("#referred-tests-number").append("   " + refferedCounter);
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

    //Sets the Specimen Source Options in the Select in the scheduleTestDialogue
    function setSpecimenSource() {
        jq("#error-specimen-source").html("");
        jq("#specimen_source_id").html("");
        var content = "";
        content += "<option value=\"\">" + "${ui.message("Specimen Source")}" + "</option>";
        <% if (specimenSource.size() > 0) {
                      specimenSource.each { %>
        content += "<option value=\"${it.conceptId}\">" + "${it.getName().name}" + "</option>";
        <%} }else {%>
        jq("#error-specimen-source").append("${ui.message("patientqueueing.select.error")}");
        <%}%>
        jq("#specimen_source_id").append(content);
    }

    // Generates Sample ID for the Sample ID Field on the scheduleTestDialogue
    function generateSampleId(orderId) {
        jq.get('${ ui.actionLink("generateSampleID") }', {
            orderId: orderId
        }, function (response) {
            if (response) {
                var responseData = response.replace("{defaultSampleId=\"", "").replace("\"}", "").trim();
                jq("#sample_id").val(responseData);
            }
        });
    }
</script>
${ui.includeFragment("ugandaemrpoc", "lab/displayResultList")}

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
                    <form method="get" id="patient-lab-search-form" onsubmit="return false">
                        <input type="text" id="patient-lab-search" name="patient-lab-search"
                               placeholder="${ui.message ( "coreapps.findPatient.search.placeholder" )}"
                               autocomplete="off" class="provider-dashboard-patient-search"/>

                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="card-body">
        <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
            <li class="nav-item">
                <a class="nav-item nav-link active" id="pending-queue-lab-tab" data-toggle="tab"
                   href="#pending-queue-lab" role="tab"
                   aria-controls="pending-queue-lab-tab" aria-selected="true">TESTS ORDERED <span style="color:red"
                                                                                                  id="pending-queue-lab-number">0</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" id="lab-work-list-tab" data-toggle="tab" href="#lab-work-list" role="tab"
                   aria-controls="lab-work-list-tab" aria-selected="false">WORKLIST <span style="color:red"
                                                                                          id="lab-work-list-number">0</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" id="referred-tests-tab" data-toggle="tab" href="#referred-tests" role="tab"
                   aria-controls="referred-tests-tab" aria-selected="false">REFFERED TESTS <span
                        style="color:red" id="referred-tests-number">0</span></a>
            </li>

            <li class="nav-item">
                <a class="nav-link" id="lab-results-tab" data-toggle="tab" href="#lab-results" role="tab"
                   aria-controls="lab-results-number-tab" aria-selected="false">RESULTS <span
                        style="color:red" id="lab-results-number">0</span></a>
            </li>
        </ul>

        <div class="tab-content" id="myTabContent">
            <div class="tab-pane fade show active" id="pending-queue-lab" role="tabpanel"
                 aria-pharmacyelledby="pending-queue-lab-tab">
                <div class="info-body">
                    <div id="pending-queue-lab-table">
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="lab-work-list" role="tabpanel"
                 aria-pharmacyelledby="lab-work-list-tab">
                <div class="info-body">
                    <div id="lab-work-list-table">
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="referred-tests" role="tabpanel"
                 aria-pharmacyelledby="referred-tests-tab">
                <div class="info-body">
                    <div id="referred-tests-list-table">
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="lab-results" role="tabpanel"
                 aria-pharmacyelledby="lab-results-tab">
                <div class="info-body">
                    <div id="lab-results-list-table">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
${ui.includeFragment ( "ugandaemrpoc", "lab/resultForm" )}
${ui.includeFragment ( "ugandaemrpoc" , "printResults" )}
</div>
${ui.includeFragment ( "ugandaemrpoc", "lab/scheduleTestDialogue" )}
<% } %>





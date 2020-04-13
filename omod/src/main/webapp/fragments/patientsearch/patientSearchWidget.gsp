<%
    config.require("afterSelectedUrl")
    def breadcrumbOverride = config.breadcrumbOverride ?: ""
    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
    ui.includeCss("ugandaemrfingerprint", "patientsearch/patientSearchWidget.css")
    ui.includeCss("ugandaemrfingerprint", "patientsearch/fontcustom_findpatient_fingerprint.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("ugandaemrfingerprint", "patientsearch/patientSearchWidget.js")
    ui.includeJavascript("ugandaemrfingerprint", "patientsearch/patientFingerPrintSearch.js")
    ui.includeJavascript("uicommons", "moment-with-locales.min.js")
    ui.includeJavascript("ugandaemrfingerprint", "patientsearch/sockjs-0.3.4.js")
    ui.includeJavascript("ugandaemrfingerprint", "patientsearch/stomp.js")
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>
<style type="text/css">
img {
    width: 100px;
    height: auto;
}


.vertical {
    border-left: 1px solid #c7c5c5;
    height: 70px;
    position: absolute;
    left: 106%;
    top: 0%;
}

.card-body {
    -ms-flex: 1 1 auto;
    flex: 7 1 auto;
    padding: 1.0rem;
    background-color: #eee;
}

body {
    background: #f9f9f9;
}

#body-wrapper {
    margin-top: 10px;
    padding: 10px;
    background-color: #ffffff00;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
    -o-border-radius: 5px;
    -ms-border-radius: 5px;
    -khtml-border-radius: 5px;
    border-radius: 5px;
}

#patient-search {
    min-width: 95%;
    color: #363463;
    display: block;
    padding: 5px 10px;
    height: 40px;
    margin-top: 15px;
    background-color: #FFF;
    border: 1px solid #dddddd;
}

#images img {
    border-radius: 50px;
    width: 30%
}

</style>
<script type="text/javascript">
    var myVar;
    var stompClient = null;
    var listableAttributeTypes = [];
    <% listingAttributeTypeNames.each { %>
    listableAttributeTypes.push('${ ui.encodeHtml(it) }');
    <% } %>
    var lastViewedPatients = [];

    function handlePatientRowSelection() {
        this.handle = function (row) {
            var uuid = row.uuid;
            location.href = '/' + OPENMRS_CONTEXT_PATH + emr.applyContextModel('${ ui.escapeJs(config.afterSelectedUrl) }', {
                patientId: uuid,
                breadcrumbOverride: '${ ui.escapeJs(breadcrumbOverride) }'
            });
        }
    }

    var handlePatientRowSelection = new handlePatientRowSelection();
    var addPatientToQueueLink = "<a  data-toggle=\"modal\" data-target=\"#add_patient_to_queue_dialog\" data-patientid=\"patientIdPlaceHolder\" data-patientnames=\"patientNamsePlaceHolder\"><i style=\"font-size: 25px;\" data-target=\"#add_patient_to_queue_dialog\" class=\"icon-share\" title=\"Check In\"></i></a>";
    var patientDashboardURL = "<i style=\"font-size: 25px;\" class=\"icon-file-alt\" title=\"Goto Patient Dashboard\" onclick=\" location.href = '/"+OPENMRS_CONTEXT_PATH+"/coreapps/clinicianfacing/patient.page?patientId=patientIdPlaceHolder'\"></i>";
    var editPatientLink = "<i style=\"font-size: 25px;\" class=\"icon-edit\" title=\"Edit Demographics\" onclick=\"location.href = '/"+OPENMRS_CONTEXT_PATH+"/registrationapp/registrationSummary.page?patientId=patientIdPlaceHolder&sectionId=demographics&appId=aijar.registrationapp.registerPatient&returnUrl=/"+OPENMRS_CONTEXT_PATH+"/ugandaemrpoc/findpatient/findPatient.page?app=ugandaemrpoc.findPatient'\"></i>";
    var patientSearchWidget = null;
    jq(function () {
        var widgetConfig = {
            initialPatients: lastViewedPatients,
            doInitialSearch: ${ doInitialSearch ? "\"" + ui.escapeJs(doInitialSearch) + "\"" : "null" },
            minSearchCharacters: ${ minSearchCharacters ?: 3 },
            afterSelectedUrl: '${ ui.escapeJs(config.afterSelectedUrl) }',
            breadcrumbOverride: '${ ui.escapeJs(breadcrumbOverride) }',
            searchDelayShort: ${ searchDelayShort },
            searchDelayLong: ${ searchDelayLong },
            searchOnline: ${searchOnline},
            onlinesearchString: "${simpleNationalIdString.replace('"', '\\"')}",
            onlineSearchURL: "${connectionProtocol+onlineIpAddress+queryURL}",
            handleRowSelection: ${ config.rowSelectionHandler ?: "handlePatientRowSelection" },
            dateFormat: '${ dateFormatJS }',
            locale: '${ locale }',
            defaultLocale: '${ defaultLocale }',
            attributeTypes: listableAttributeTypes,
            messages: {
                info: '${ ui.message("coreapps.search.info") }',
                first: '${ ui.message("coreapps.search.first") }',
                previous: '${ ui.message("coreapps.search.previous") }',
                next: '${ ui.message("coreapps.search.next") }',
                last: '${ ui.message("coreapps.search.last") }',
                noMatchesFound: '${ ui.message("coreapps.search.noMatchesFound") }',
                noData: '${ ui.message("coreapps.search.noData") }',
                recent: '${ ui.message("coreapps.search.label.recent") }',
                onlyInMpi: '${ ui.message("coreapps.search.label.onlyInMpi") }',
                searchError: '${ ui.message("coreapps.search.error") }',
                actionColHeader: 'Action',
                patientDashboardURL: patientDashboardURL,
                addPatientToQueueLink: addPatientToQueueLink,
                editPatientLink: editPatientLink,
                identifierColHeader: '${ ui.message("coreapps.search.identifier") }',
                nameColHeader: '${ ui.message("coreapps.search.name") }',
                genderColHeader: '${ ui.message("coreapps.gender") }',
                ageColHeader: '${ ui.message("coreapps.age") }',
                birthdateColHeader: '${ ui.message("coreapps.birthdate") }',
                ageInMonths: '${ ui.message("coreapps.age.months") }',
                ageInDays: '${ ui.message("coreapps.age.days") }'
            }
        };
        patientSearchWidget = new PatientSearchWidget(widgetConfig);
    });

    var socket = new SockJS('${fingerSocketPrintIpAddress}/search');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/showResult', function (calResult) {
            showResult(JSON.parse(calResult.body));
        });
    });


    function search() {
        document.getElementById('calResponse').innerHTML = "";
        document.getElementById('images').innerHTML = "";
        stompClient.send("/calcApp/search", {});
    }

    function showResult(message) {
        var response = document.getElementById('calResponse');
        var imageDiv = document.getElementById('images');
        if (message.type === "image") {
            var imageTag = document.createElement('img');
            imageTag.src = "data:image/png;base64," + message.result;
            imageDiv.appendChild(imageTag);
        } else if (message.type === "local" && message.patient !== "") {
            patientSearchWidget.searchByFingerPrint(message.patient);
        } else if (message.type === "online" && message.patient !== "" && ${searchOnline} === true) {
            window.location = "/"+OPENMRS_CONTEXT_PATH+"/ugandaemrfingerprint/patientInOtherFacility.page?patientId=" + message.patient;
        } else if (message.type === null && (message.patient === null || message.patient === "") && ${searchOnline} === true) {
            var message;
            message = '{"result":"Patient Not Found at Central Server"}';
            showResult(JSON.parse(message));
            jq().toastmessage('showErrorToast', "Patient Not Found");
        } else {
            response.innerHTML = message.result;
        }
    }

    function myFunction() {
        document.getElementById("loader").className = "load";
        myVar = setTimeout(showPage, 5000);
    }

    function showPage() {
        document.getElementById("loader").style.display = "none";
        document.getElementById("myDiv").style.display = "block";
    }

    jq(document).ready(function () {
        jq('#add_patient_to_queue_dialog').on('show.bs.modal', function (event) {
            var button = jq(event.relatedTarget);
            var patientId = button.data('patientid');
            var patientNames = button.data('patientnames');
            var modal = jq(this)
            modal.find("#patient_id").val(patientId);
            modal.find("#checkin_patient_names").val(patientNames);
        });

        jq("#checkin").click(function () {
            jq.get('${ ui.actionLink("ugandaemrpoc","checkIn","post") }', {
                patientId: jq("#patient_id").val().trim().toLowerCase(),
                locationId: jq("#location_id").val().trim().toLowerCase(),
                locationFromId: jq("#location_from_id").val().trim().toLowerCase(),
                patientStatus: jq("#patient_status").val().trim().toLowerCase(),
                visitComment: jq("#visit_comment").val().trim().toLowerCase()
            }, function (response) {
                var responseData = JSON.parse(response.replace("patientTriageQueue=", "\"patientTriageQueue\":").trim())
                printTriageRecord("printSection", responseData);
                jq("#add_patient_to_queue_dialog").modal('hide');
                if (!response) {
                    ${ ui.message("coreapps.none ") }
                }
            });
        });
    });
</script>

<div class="card">
    <div class="card-body">
        <div class="row">
            <div class="col col-lg-1">
                <div>
                    <div class="left">
                        <label style="color: maroon">Fingerprint</label>
                    </div>

                    <div class="center" style="margin-left: 22px;">
                        <i id="patient-search-finger-print-button" onclick="search();"
                           class="medium icon-fingerprint"></i>
                    </div>

                    <div class="vertical"></div>
                </div>
            </div>

            <div class="col col-lg-8" style="vertical-align: middle;">
                <form method="get" id="patient-search-form" onsubmit="return false">
                    <input type="text" id="patient-search"
                           placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                           autocomplete="off" <% if (doInitialSearch) { %>value="${doInitialSearch}" <% } %>/>
                    <i id="patient-search-clear-button" class="small icon-remove-sign"></i>
                </form>
            </div>

            <div class="col col-lg-1">
                <div id="patient-search-finger-print" style="display:none;">
                    <div id="calculationDiv">
                        <p id="calResponse"></p>

                        <div id="images"></div>
                    </div>
                </div>
            </div>

            <div class="col col-lg-2 right">
                <div class="center">
                    <% if (registrationAppLink ?: false) { %>
                    <span style="width: 40%;text-align:center;margin-left: 52px;"><a
                            id="patient-search-register-patient" href="/${contextPath}/${registrationAppLink}"><i
                                class="icon-plus-sign medium"></i><br/></a></span>
                    <span style="width:100%; text-align:center">Create New Patient</span>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</div>

${ui.includeFragment("ugandaemrpoc", "checkIn")}


<div id="patient-search-results"></div>
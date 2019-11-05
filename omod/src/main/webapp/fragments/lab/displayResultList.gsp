<script>
    function displayLabResult(labQueueList) {
        var referedTests = "";
        var workListTests = "";

        var tableHeader = "<table><thead><tr><th>ORDER NO</th><th>PATIENT NAME</th><th>TEST</th><th>STATUS</th><th>ACTION</th></tr></thead><tbody>";

        var tableFooter = "</tbody></table>";
        var resultListCounter=0;

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
            orderedTestsRows += "<td>" + element.orderNumber + "</td>";
            orderedTestsRows += "<td>" + element.patient + "</td>";
            orderedTestsRows += "<td>" + element.conceptName + "</td>";
            orderedTestsRows += "<td>" + element.status + "</td>";
            orderedTestsRows += "<td>";
            orderedTestsRows += "<a title=\"Edit Result\" onclick='showEditResultForm(" + element.orderId + ")'><i class=\"icon-list-ul small\"></i></a>";
            orderedTestsRows += "<a title=\"Print Results\" onclick='printresult(" + element.orderId + "," + element.patientId + ")'><i class=\"icon-print small\"></i></a>";
            orderedTestsRows += "</td>";
            orderedTestsRows += "</tr>";
            referedTests += orderedTestsRows;

            resultListCounter+=1;
        });

        jq("#lab-results-list-table").html("");
        if (referedTests.length > 0) {
            jq("#lab-results-list-table").append(tableHeader + referedTests + tableFooter);
        } else {
            jq("#lab-results-list-table").append("No Data ");
        }

        jq("#lab-results-number").html("");
        jq("#lab-results-number").append("   " + resultListCounter);
    }
</script>
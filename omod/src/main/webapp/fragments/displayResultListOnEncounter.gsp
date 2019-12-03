<script>
    jq(document).ready(function () {
        getResults();
    });

    function getResults() {
        jq.get('${ ui.actionLink("ugandaemrpoc","displayResultListOnEncounter","getOrderWithResultForEncounter") }', {
            encounterId: ${encounterId}
        }, function (response) {
            if (response.trim()!=="{}") {
                var responseData = JSON.parse(response.replace("ordersList=", "\"ordersList\":").trim());
                displayLabResult(responseData)
            }
        });
    }

    function displayLabResult(labQueueList) {
        printresult(labQueueList.ordersList[0].orderId, labQueueList.ordersList[0].patientId);
    }
</script>

<script>
    var _results = {'_items': ko.observableArray([])};
    var printResults = {'items': ko.observableArray([])};

    jq(document).ready(function () {
        ko.applyBindings(_results, jq("#patient-report")[0]);
    });

    function organize(data) {
        var final = [];
        var investigations = data.map(function (d) {
            return d['investigation'];
        });
        investigations = Array.from(new Set(investigations));

        investigations.forEach(function (investigation) {
            var obj = new Object();
            obj['investigation'] = investigation;

            var sets = data.filter(function (d) {
                return d['investigation'] == investigation;
            });
            sets = sets.map(function (d) {
                return d['set'];
            });
            sets = Array.from(new Set(sets));
            obj['sets'] = [];

            sets.forEach(function (set) {
                var _obj = new Object();
                _obj['name'] = set;
                var wanted = data.filter(function (d) {
                    return d['investigation'] == investigation && d['set'] == set;
                });
                _obj['data'] = wanted;
                obj['sets'].push(_obj);
            });
            final.push(obj);
        });

        console.log(final);
        final.forEach(function (o) {
            _results._items.push(o);
        });
    }

    function printresult(testId, patientId) {
        jq.get('${ ui.actionLink("ugandaemrpoc","printResults","getResults") }', {
            patientId: patientId,
            testId: testId
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("data=", "\"data\":").trim());
                organize(responseData.data);
            } else if (!response) {
            }
        });
    }
</script>
<section sectionTag="section" id="lab-results-tab" headerTag="h1">
    <div id="printSection" class="print-only">
        <center>
            <table style="table-layout: fixed; margin-top: 5px;">
                <thead>
                <tr>
                    <th width="50%" style="text-align: left">Test</th>
                    <th width="15%" style="text-align: left">Result</th>
                    <th width="15%" style="text-align: left">Units</th>
                    <th width="15%" style="text-align: left">Reference Range</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>

            <table id="patient-report" style="margin-top: 5px; table-layout: fixed;">
                <tbody data-bind="foreach: _items">
                <tr>
                    <td data-bind="foreach: sets">
                        <table style="table-layout:fixed;">
                            <thead>
                            <th data-bind="text: name" style="text-align: left"></th>
                            </thead>
                            <tbody data-bind="foreach: data">
                            <td width="50%"
                                data-bind="text: '' + test" style="text-align: left"></td>
                            <td width="15%" data-bind="text: value" style="text-align: left"></td>
                            <td width="15%" data-bind="text: unit" style="text-align: left"></td>
                            <td width="15%" style="text-align: left">
                                <div data-bind="if: (lowNormal || hiNormal)">
                                    <span data-bind="text: 'Adult/Male:' + lowNormal + '/' + hiNormal"></span>
                                </div>

                                <div data-bind="if: (lowCritical || lowCritical)">
                                    <span data-bind="text: 'Female:' + lowCritical + '/' + hiCritical"></span>
                                </div>

                                <div data-bind="if: (lowAbsolute || hiAbsolute)">
                                    <span data-bind="text: 'Child:' + lowAbsolute + '/' + hiAbsolute"></span>
                                </div>
                            </td>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
        </center>
    </div>
</section>


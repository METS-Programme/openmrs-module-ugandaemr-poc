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

        printDiv("printSection");
    }

    function printresult(testId, patientId) {
        jq.get('${ ui.actionLink("getResults") }', {
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

    function printDiv(divIdToPrint) {

        var divToPrint = document.getElementById(divIdToPrint);
        var newWin = window.open('', 'Print-Window');
        newWin.document.open();
        newWin.document.write('<html><body onload="window.print()">' + divToPrint.innerHTML + '</body></html>');
        newWin.document.close();
        setTimeout(function () {
            newWin.close();
        }, 10);
    }

    jq(function () {
        jq('#date').datepicker("option", "dateFormat", "dd/mm/yy");
    });

</script>
<style>
.print-only {
    display: none;
}
</style>

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
                <td><strong style="font-size: 22px;" data-bind="text: investigation"></strong></td>
            </tr>
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
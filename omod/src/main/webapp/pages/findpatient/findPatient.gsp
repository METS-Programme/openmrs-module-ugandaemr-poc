<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<script type="text/javascript">
    <% if (breadcrumbs) { %>
    var breadcrumbs = ${ breadcrumbs };
    <% } else { %>
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message(label)}"}
    ];
    <% } %>
    jq(function() {
        jq('#patient-search').focus();
    });
</script>
<% if (breadcrumbs) { %>
${ ui.includeFragment("ugandaemrpoc", "patientsearch/patientSearchWidget",
        [ afterSelectedUrl: afterSelectedUrl,
          showLastViewedPatients: showLastViewedPatients,
          breadcrumbOverride: breadcrumbs,
          registrationAppLink: registrationAppLink])}
<% } else { %>
${ ui.includeFragment("ugandaemrpoc", "patientsearch/patientSearchWidget",
        [ afterSelectedUrl: afterSelectedUrl,
          showLastViewedPatients: showLastViewedPatients,
          registrationAppLink: registrationAppLink])}
<% } %>
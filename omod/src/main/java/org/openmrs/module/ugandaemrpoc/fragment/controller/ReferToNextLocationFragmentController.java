package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;


public class ReferToNextLocationFragmentController {

    protected final Log log = LogFactory.getLog(getClass());

    public ReferToNextLocationFragmentController() {
    }

    public void controller(@SpringBean FragmentModel pageModel, @SpringBean("patientService") PatientService patientService, @SpringBean("locationService") LocationService locationService, @RequestParam(value = "patientId", required = false) Patient patient, UiSessionContext uiSessionContext) {
        if (patient != null) {
            pageModel.put("birthDate", patient.getBirthdate());
            pageModel.put("patient", patient);
            pageModel.put("patientId", patient.getPatientId());
        }
        pageModel.put("locationList", ((Location) locationService.getRootLocations(false).get(0)).getChildLocations());
        pageModel.put("providerList", Context.getProviderService().getAllProviders(false));
    }

    public SimpleObject post(@SpringBean("patientService") PatientService patientService, @RequestParam(value = "patientId") Patient patient, @RequestParam(value = "providerId", required = false) Provider provider, @RequestParam("locationId") Location location, @RequestParam(value = "locationFromId", required = false) Location locationFrom, @RequestParam(value = "patientStatus", required = false) String patientStatus, @RequestParam(value = "visitComment", required = false) String visitComment, @RequestParam(value = "returnUrl", required = false) String returnUrl, UiSessionContext uiSessionContext, UiUtils uiUtils, HttpServletRequest request) throws IOException, ParseException {

        PatientQueue patientQueue = new PatientQueue();
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleObject simpleObject = new SimpleObject();
        Location currentLocation = new Location();

        if (locationFrom != null) {
            currentLocation = locationFrom;

        } else {
            currentLocation = uiSessionContext.getSessionLocation();
        }

        if (patientStatus != null && patientStatus.equals("emergency")) {
            patientQueue.setPriority(0);
            patientQueue.setPriorityComment(patientStatus);
        }

        if (visitComment != null) {
            patientQueue.setComment(visitComment);
        }

        PatientQueue previousQueue = ugandaEMRPOCService.completePreviousQueue(patient, currentLocation, PatientQueue.Status.PENDING);

        patientQueue.setLocationFrom(currentLocation);
        patientQueue.setPatient(patient);
        patientQueue.setLocationTo(location);
        patientQueue.setProvider(provider);
        patientQueue.setStatus(PatientQueue.Status.PENDING);
        patientQueue.setCreator(uiSessionContext.getCurrentUser());
        patientQueue.setDateCreated(new Date());
        patientQueueingService.assignVisitNumberForToday(patientQueue);
        patientQueueingService.savePatientQue(patientQueue);
        simpleObject.put("patientTriageQueue", objectMapper.writeValueAsString(mapPatientQueueToMapper(patientQueue)));
        return simpleObject;
    }

    private PatientQueueMapper mapPatientQueueToMapper(PatientQueue patientQueue) {
        PatientQueueMapper patientQueueMapper = new PatientQueueMapper();
        if (patientQueue != null) {
            String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " " + patientQueue.getPatient().getMiddleName();

            patientQueueMapper.setId(patientQueue.getId());
            patientQueueMapper.setPatientNames(names.replace("null", ""));
            patientQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
            patientQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
            patientQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
            if (patientQueue.getProvider() != null) {
                patientQueueMapper.setProviderNames(patientQueue.getProvider().getName());
            }

            if (patientQueue.getCreator() != null) {
                patientQueueMapper.setCreatorNames(patientQueue.getCreator().getPersonName().getFullName());
            }
            patientQueueMapper.setStatus(patientQueue.getStatus().name());
            patientQueueMapper.setVisitNumber(patientQueue.getVisitNumber());
            patientQueueMapper.setGender(patientQueue.getPatient().getGender());
            patientQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
            patientQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
        }
        return patientQueueMapper;
    }

}

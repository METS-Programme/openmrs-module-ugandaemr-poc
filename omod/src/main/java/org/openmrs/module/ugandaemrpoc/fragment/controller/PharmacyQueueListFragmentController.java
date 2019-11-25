package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.pharmacy.DispensingModelWrapper;
import org.openmrs.module.ugandaemrpoc.pharmacy.mapper.DrugOrderMapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.*;

public class PharmacyQueueListFragmentController {

    protected final Log log = LogFactory.getLog(PharmacyQueueListFragmentController.class);

    public PharmacyQueueListFragmentController() {
        // This Method is a Constructor
    }

    public void controller(@SpringBean FragmentModel pageModel, UiSessionContext uiSessionContext) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = sdf.format(new Date());
        List<String> list = new ArrayList();
        list.add(PHARMACY_LOCATION_UUID);
        pageModel.addAttribute("currentDate", dateStr);
        pageModel.addAttribute("locationSession", uiSessionContext.getSessionLocation().getUuid());
        pageModel.put("clinicianLocation", list);
        pageModel.put("currentProvider", Context.getAuthenticatedUser());
    }


    public SimpleObject getPharmacyQueueList(@RequestParam(value = "pharmacySearchFilter", required = false) String searchfilter, UiSessionContext uiSessionContext) throws IOException, ParseException {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleObject simpleObject = new SimpleObject();
        List<PatientQueue> patientQueueList;
        if (!searchfilter.equals("")) {
            patientQueueList = patientQueueingService.getPatientQueueListBySearchParams(searchfilter, OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()), uiSessionContext.getSessionLocation(), null, null);
        } else {
            patientQueueList = patientQueueingService.getPatientQueueListBySearchParams(null, OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()), uiSessionContext.getSessionLocation(), null, null);
        }
        if (!patientQueueList.isEmpty()) {
            simpleObject.put("patientPharmacyQueueList", objectMapper.writeValueAsString(Context.getService(UgandaEMRPOCService.class).mapPatientQueueToMapperWithDrugOrders(patientQueueList)));
        } else {
            simpleObject.put("patientPharmacyQueueList", "");
        }

        return simpleObject;
    }


    public SimpleObject getPrescription(@RequestParam(value = "encounterId", required = false) Encounter encounter, UiSessionContext uiSessionContext) throws IOException, ParseException {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        SimpleObject simpleObject = new SimpleObject();


        return simpleObject;
    }


    public SimpleObject dispense(@BindParams("wrap") DispensingModelWrapper resultWrapper, UiSessionContext sessionContext) throws Exception {
        Provider provider = sessionContext.getCurrentProvider();
        EncounterService encounterService = Context.getEncounterService();

        Encounter previousEncounter = encounterService.getEncounter(resultWrapper.getEncounterId());
        Encounter encounter = new Encounter();
        encounter.setEncounterType(encounterService.getEncounterTypeByUuid(ENCOUNTER_TYPE_DISPENSE_UUID));
        encounter.setProvider(Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE_PHARMACIST), provider);
        encounter.setLocation(sessionContext.getSessionLocation());
        encounter.setPatient(previousEncounter.getPatient());
        encounter.setEncounterDatetime(new Date());

        List<DrugOrderMapper> referredOutPrescriptions = new ArrayList<>();
        Set<Obs> obs = new HashSet<>();

        for (DrugOrderMapper drugOrderMapper : resultWrapper.getDrugOrderMappers()) {

            if (drugOrderMapper.getOrderReasonNonCoded() != null && drugOrderMapper.getOrderReasonNonCoded().equals("REFERREDOUT")) {
                referredOutPrescriptions.add(drugOrderMapper);
                obs.addAll(processDispensingObservation(encounter, drugOrderMapper.getConcept(), 0.0, 0, drugOrderMapper.getOrderId(), false));
            } else {
                obs.addAll(processDispensingObservation(encounter, drugOrderMapper.getConcept(), drugOrderMapper.getQuantity(), drugOrderMapper.getDuration(), drugOrderMapper.getOrderId(), true));
            }
        }

        encounter.setObs(obs);
        encounterService.saveEncounter(encounter);

        ObjectMapper objectMapper = new ObjectMapper();

        SimpleObject simpleObject = new SimpleObject();

        if (!referredOutPrescriptions.isEmpty()) {
            simpleObject.put("referredOutPrescriptions", objectMapper.writeValueAsString(referredOutPrescriptions));
        } else {
            simpleObject = SimpleObject.create("status", "success", "message", "Saved!");
        }

        return simpleObject;
    }

    private Set<Obs> processDispensingObservation(Encounter encounter, String conceptDrug, Double quantity, Integer duration, Integer orderId, boolean recievedAtFacility) throws ParseException {

        ConceptService conceptService = Context.getConceptService();
        Set<Obs> obs = new HashSet<>();
        Order order = null;
        if (orderId != null) {
            order = Context.getOrderService().getOrder(orderId);
        }
        //Grouping Observation
        Obs parentObs = createDispensingObs(encounter, conceptService.getConcept(MEDICATION_DISPENSE_SET), null, null, order);
        obs.add(parentObs);

        //Drug Observation
        Obs drug = createDispensingObs(encounter, conceptService.getConcept(MEDICATION_ORDER_CONCEPT_ID), conceptDrug, "coded", order);
        parentObs.addGroupMember(drug);
        obs.add(drug);

        //Quantity Observation
        Obs drugQuantity = createDispensingObs(encounter, conceptService.getConcept(MEDICATION_DISPENSE_UNITS), quantity.toString(), "numeric", order);
        parentObs.addGroupMember(drugQuantity);
        obs.add(drugQuantity);

        //Duration Observation
        Obs periodDispensed = createDispensingObs(encounter, conceptService.getConcept(MEDICATION_DURATION_CONCEPT_ID), duration.toString(), "numeric", order);
        parentObs.addGroupMember(periodDispensed);
        obs.add(periodDispensed);

        //check if issued at facility

        Obs dispensedAtFacility = createDispensingObs(encounter, conceptService.getConcept(MEDICATION_DISPENSE_RECEIVED_AT_VIST), null, null, order);
        dispensedAtFacility.setValueBoolean(recievedAtFacility);
        parentObs.addGroupMember(dispensedAtFacility);
        obs.add(dispensedAtFacility);

        return obs;
    }

    private Obs createDispensingObs(Encounter encounter, Concept concept, String value, String valueType, Order order) throws ParseException {
        Obs obs = new Obs();
        obs.setObsDatetime(encounter.getEncounterDatetime());
        obs.setPerson(encounter.getPatient());
        obs.setLocation(encounter.getLocation());
        obs.setEncounter(encounter);
        obs.setOrder(order);
        obs.setConcept(concept);
        if (valueType != null) {
            if (valueType.equals("string")) {
                obs.setValueAsString(value);
            } else if (valueType.equals("numeric")) {
                obs.setValueNumeric(Double.parseDouble(value));
            } else if (valueType.equals("coded")) {
                obs.setValueCoded(Context.getConceptService().getConcept(value));
            } else if (valueType.equals("groupId")) {
                obs.setValueGroupId(Integer.parseInt(value));
            }
        }
        return obs;
    }
}

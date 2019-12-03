package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.api.lab.mapper.LabQueueMapper;
import org.openmrs.module.ugandaemrpoc.api.lab.mapper.OrderMapper;
import org.openmrs.module.ugandaemrpoc.api.lab.util.LaboratoryUtil;
import org.openmrs.module.ugandaemrpoc.api.lab.util.TestResultModel;
import org.openmrs.module.ugandaemrpoc.pharmacy.mapper.DrugOrderMapper;
import org.openmrs.module.ugandaemrpoc.pharmacy.mapper.PharmacyMapper;
import org.openmrs.module.ugandaemrpoc.utils.DateFormatUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.OpenmrsUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.*;

public class UgandaEMRPOCServiceImpl extends BaseOpenmrsService implements UgandaEMRPOCService {

    protected final Log log = LogFactory.getLog(UgandaEMRPOCServiceImpl.class);

    private String ordersListLabel = "ordersList";


    @Override
    public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
        List<PatientQueueMapper> patientQueueMappers = new ArrayList<>();

        for (PatientQueue patientQueue : patientQueueList) {
            String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " " + patientQueue.getPatient().getMiddleName();
            PatientQueueMapper patientQueueMapper = new PatientQueueMapper();
            patientQueueMapper.setId(patientQueue.getId());
            patientQueueMapper.setPatientNames(names.replace("null", ""));
            patientQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
            patientQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
            patientQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
            patientQueueMapper.setVisitNumber(patientQueue.getVisitNumber());

            if (patientQueue.getProvider() != null) {
                patientQueueMapper.setProviderNames(patientQueue.getProvider().getName());
            }

            if (patientQueue.getCreator() != null) {
                patientQueueMapper.setCreatorNames(patientQueue.getCreator().getDisplayString());
            }

            if (patientQueue.getEncounter() != null) {
                patientQueueMapper.setEncounterId(patientQueue.getEncounter().getEncounterId().toString());
            }

            if (patientQueue.getStatus() == PatientQueue.Status.PENDING && patientQueue.getLocationFrom().getUuid().equals(LAB_LOCATION_UUID)) {
                patientQueueMapper.setStatus(QUEUE_STATUS_FROM_LAB);
            } else {
                patientQueueMapper.setStatus(patientQueue.getStatus().name());
            }


            patientQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
            patientQueueMapper.setGender(patientQueue.getPatient().getGender());
            patientQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
            patientQueueMappers.add(patientQueueMapper);
        }
        return patientQueueMappers;
    }


    /**
     * Checks if Sample ID genereated is already issued out
     *
     * @param sampleId
     * @param orderNumber
     * @return
     * @throws ParseException
     */
    public boolean isSampleIdExisting(String sampleId, String orderNumber) throws ParseException {
        List list = Context.getAdministrationService().executeSQL(String.format("select * from orders where accession_number=\"%s\"", sampleId), true);
        boolean exists = false;
        if (!list.isEmpty()) {
            exists = true;
        }
        return exists;
    }

    /**
     * @param test
     * @return
     */
    public Set<TestResultModel> renderTests(Order test) {
        Set<TestResultModel> trms = new HashSet<>();
        if (test.getEncounter() != null) {
            Encounter encounter = test.getEncounter();
            for (Obs obs : encounter.getAllObs()) {
                if (obs.getOrder() != null) {
                    if (obs.hasGroupMembers()) {
                        for (Obs groupMemberObs : obs.getGroupMembers()) {
                            TestResultModel trm = new TestResultModel();
                            trm.setInvestigation(test.getConcept().getDisplayString());
                            trm.setSet(obs.getConcept().getDisplayString());
                            trm.setConcept(obs.getConcept());
                            setTestResultModelValue(groupMemberObs, trm);
                            trms.add(trm);
                        }
                    } else if (obs.getObsGroup() == null) {
                        TestResultModel trm = new TestResultModel();
                        trm.setInvestigation(test.getConcept().getName().getName());
                        trm.setSet(test.getConcept().getDatatype().getName());
                        trm.setConcept(obs.getConcept());
                        setTestResultModelValue(obs, trm);
                        trms.add(trm);
                    }
                }
            }
        }
        return trms;
    }

    /**
     * Process Lab Orders
     *
     * @param query
     * @param asOfDate
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public SimpleObject getProcessedOrders(String query, Date asOfDate, boolean includeProccesed) throws ParseException, IOException {
        Date date;
        SimpleObject simpleObject = new SimpleObject();
        ObjectMapper objectMapper = new ObjectMapper();
        OrderService orderService = Context.getOrderService();

        if (asOfDate != null) {
            date = asOfDate;
        } else {
            date = new Date();
        }
        query = String.format(query, DateFormatUtil.dateFormtterString(date, DAY_START_TIME), DateFormatUtil.dateFormtterString(date, DAY_END_TIME));

        List list = Context.getAdministrationService().executeSQL(query, true);
        Set<Order> unProcesedOrderList = new HashSet<>();

        Set<Order> proccesedOrderList = new HashSet<>();

        if (!list.isEmpty()) {
            for (Object o : list) {
                Order order = orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
                if (order.getAccessionNumber() == null) {
                    unProcesedOrderList.add(order);
                }
                proccesedOrderList.add(order);
            }
        }

        if (includeProccesed && !proccesedOrderList.isEmpty()) {
            simpleObject.put(ordersListLabel, objectMapper.writeValueAsString(processOrders(proccesedOrderList, true)));
        } else if (!unProcesedOrderList.isEmpty() && !includeProccesed) {
            simpleObject.put(ordersListLabel, objectMapper.writeValueAsString(processOrders(unProcesedOrderList, true)));
        }
        return simpleObject;
    }

    /**
     * Process Lab Orders
     *
     * @param query
     * @param encounterId
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public SimpleObject getOrderResultsOnEncounter(String query, int encounterId, boolean includeProccesed) throws ParseException, IOException {
        SimpleObject simpleObject = new SimpleObject();
        ObjectMapper objectMapper = new ObjectMapper();
        OrderService orderService = Context.getOrderService();

        query = String.format(query, encounterId);

        List list = Context.getAdministrationService().executeSQL(query, true);
        Set<Order> unProcesedOrderList = new HashSet<>();

        Set<Order> proccesedOrderList = new HashSet<>();

        if (!list.isEmpty()) {
            for (Object o : list) {
                Order order = orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
                if (order.getAccessionNumber() == null) {
                    unProcesedOrderList.add(order);
                }
                proccesedOrderList.add(order);
            }
        }

        if (includeProccesed && !proccesedOrderList.isEmpty()) {
            simpleObject.put(ordersListLabel, objectMapper.writeValueAsString(processOrders(proccesedOrderList, true)));
        } else if (!unProcesedOrderList.isEmpty() && !includeProccesed) {
            simpleObject.put(ordersListLabel, objectMapper.writeValueAsString(processOrders(unProcesedOrderList, true)));
        }
        return simpleObject;
    }

    /**
     * Processes Orders from Encounter to Order Mapper
     *
     * @param orders
     * @return
     */

    public Set<OrderMapper> processOrders(Set<Order> orders, boolean fiterOutProccessed) {
        Set<OrderMapper> orderMappers = new HashSet<>();
        for (Order order : orders) {
            if (order.getOrderType().equals(Context.getOrderService().getOrderTypeByUuid(ORDER_TYPE_LAB_UUID))) {
                String names = order.getPatient().getFamilyName() + " " + order.getPatient().getGivenName() + " " + order.getPatient().getMiddleName();
                OrderMapper orderMapper = new OrderMapper();
                orderMapper.setAccessionNumber(order.getAccessionNumber());
                orderMapper.setCareSetting(order.getCareSetting().getName());
                orderMapper.setConcept(order.getConcept().getConceptId().toString());
                orderMapper.setConceptName(order.getConcept().getDisplayString());
                orderMapper.setDateActivated(order.getDateActivated().toString());
                orderMapper.setOrderer(order.getOrderer().getName());
                orderMapper.setOrderNumber(order.getOrderNumber());
                orderMapper.setPatientId(order.getPatient().getPatientId());
                orderMapper.setInstructions(order.getInstructions());
                orderMapper.setUrgency(order.getUrgency().name());
                orderMapper.setPatient(names.replace("null", ""));
                orderMapper.setOrderId(order.getOrderId());
                orderMapper.setEncounterId(order.getEncounter().getEncounterId());
                if (order.isActive()) {
                    orderMapper.setStatus(QUEUE_STATUS_ACTIVE);
                }
                if (orderHasResults(order)) {
                    orderMapper.setStatus(QUEUE_STATUS_HAS_RESULTS);
                }
                orderMappers.add(orderMapper);
            }
        }
        return orderMappers;
    }


    /**
     * Processes Orders from Encounter to Order Mapper
     *
     * @param orders
     * @return
     */

    public Set<DrugOrderMapper> processDrugOrders(Set<Order> orders) {
        Set<DrugOrderMapper> orderMappers = new HashSet<>();

        for (Order order : orders) {
            if (order.getOrderType().equals(Context.getOrderService().getOrderTypeByUuid(ORDER_TYPE_DRUG_UUID)) && order.isActive()) {
                DrugOrder drugOrder = (DrugOrder) order;
                String names = order.getPatient().getFamilyName() + " " + order.getPatient().getGivenName() + " " + order.getPatient().getMiddleName();
                DrugOrderMapper drugOrderMapper = new DrugOrderMapper();

                drugOrderMapper.setAsNeeded(drugOrder.getAsNeeded());
                drugOrderMapper.setAsNeededCondition(drugOrder.getAsNeededCondition());
                drugOrderMapper.setBrandName(drugOrder.getBrandName());
                drugOrderMapper.setDose(drugOrder.getDose());
                drugOrderMapper.setDoseUnits(drugOrder.getDoseUnits().getDisplayString());
                drugOrderMapper.setDrug(drugOrder.getConcept().getDisplayString());
                drugOrderMapper.setDuration(drugOrder.getDuration());
                drugOrderMapper.setDurationUnits(drugOrder.getDurationUnits().getDisplayString());
                drugOrderMapper.setDrugNonCoded(drugOrder.getDrugNonCoded());
                drugOrderMapper.setFrequency(drugOrder.getFrequency().getName());
                drugOrderMapper.setNumRefills(drugOrder.getNumRefills());
                drugOrderMapper.setQuantity(drugOrder.getQuantity());
                drugOrderMapper.setQuantityUnits(drugOrder.getQuantityUnits().getDisplayString());
                drugOrderMapper.setRoute(drugOrder.getRoute().getDisplayString());
                drugOrderMapper.setAccessionNumber(drugOrder.getAccessionNumber());
                drugOrderMapper.setCareSetting(drugOrder.getCareSetting().getName());
                drugOrderMapper.setConcept(drugOrder.getConcept().getConceptId().toString());
                drugOrderMapper.setConceptName(drugOrder.getConcept().getDisplayString());
                drugOrderMapper.setDateActivated(drugOrder.getDateActivated().toString());
                drugOrderMapper.setOrderer(drugOrder.getOrderer().getName());
                drugOrderMapper.setOrderNumber(drugOrder.getOrderNumber());
                drugOrderMapper.setPatientId(drugOrder.getPatient().getPatientId());
                drugOrderMapper.setInstructions(drugOrder.getInstructions());
                drugOrderMapper.setUrgency(drugOrder.getUrgency().name());
                drugOrderMapper.setPatient(names.replace("null", ""));
                drugOrderMapper.setOrderId(drugOrder.getOrderId());
                drugOrderMapper.setEncounterId(drugOrder.getEncounter().getEncounterId());
                if (order.isActive()) {
                    drugOrderMapper.setStatus(QUEUE_STATUS_ACTIVE);
                }
                if (orderHasResults(order)) {
                    drugOrderMapper.setStatus(QUEUE_STATUS_HAS_RESULTS);
                }
                orderMappers.add(drugOrderMapper);
            }
        }
        return orderMappers;
    }

    /**
     * Set Results Model
     *
     * @param obs
     * @param trm
     */
    private void setTestResultModelValue(Obs obs, TestResultModel trm) {
        Concept concept = obs.getConcept();
        trm.setTest(obs.getConcept().getDisplayString());
        if (concept != null) {
            String datatype = concept.getDatatype().getName();
            if (datatype.equalsIgnoreCase("Text")) {
                trm.setValue(obs.getValueText());
            } else if (datatype.equalsIgnoreCase("Numeric")) {
                if (obs.getValueText() != null) {
                    trm.setValue(obs.getValueText());
                } else if (obs.getValueNumeric() != null) {
                    trm.setValue(obs.getValueNumeric().toString());
                }
                ConceptNumeric cn = Context.getConceptService().getConceptNumeric(concept.getConceptId());
                trm.setUnit(cn.getUnits());
                if (cn.getLowNormal() != null) trm.setLowNormal(cn.getLowNormal().toString());

                if (cn.getHiNormal() != null) trm.setHiNormal(cn.getHiNormal().toString());

                if (cn.getHiAbsolute() != null) {
                    trm.setHiAbsolute(cn.getHiAbsolute().toString());
                }

                if (cn.getHiCritical() != null) {
                    trm.setHiCritical(cn.getHiCritical().toString());
                }

                if (cn.getLowAbsolute() != null) {
                    trm.setLowAbsolute(cn.getLowAbsolute().toString());
                }

                if (cn.getLowCritical() != null) {
                    trm.setLowCritical(cn.getLowCritical().toString());
                }

            } else if (datatype.equalsIgnoreCase("Coded")) {
                trm.setValue(obs.getValueCoded().getName().getName());
            }
        }
    }

    private boolean orderHasResults(Order order) {
        boolean hasOrder = false;

        List list = Context.getAdministrationService().executeSQL("select obs_id from obs where order_id=" + order.getOrderId() + "", true);

        if (!list.isEmpty()) {
            hasOrder = true;
        }
        return hasOrder;
    }

    /**
     * Add Lab Results Observation to Encounter
     *
     * @param encounter
     * @param testConcept
     * @param testGroupConcept
     * @param result
     * @param test
     */
    public void addLaboratoryTestObservation(Encounter encounter, Concept testConcept, Concept testGroupConcept, String result, Order test) {
        log.warn("testConceptId=" + testConcept);
        log.warn("testGroupConceptId=" + testGroupConcept);
        Obs obs = null;
        obs = getObs(encounter, testConcept, testGroupConcept);
        setObsAttributes(obs, encounter);
        obs.setConcept(testConcept);
        obs.setOrder(test);

        if (testConcept.getDatatype().getName().equalsIgnoreCase("Text")) {
            obs.setValueText(result);
        } else if (testConcept.getDatatype().getName().equalsIgnoreCase("Numeric")) {
            if (StringUtils.isNotBlank(result)) {
                obs.setValueNumeric(Double.parseDouble(result));
            }
        } else if (testConcept.getDatatype().getName().equalsIgnoreCase("Coded")) {
            Concept answerConcept = LaboratoryUtil.searchConcept(result);
            obs.setValueCoded(answerConcept);
        }
        if (testGroupConcept != null) {
            Obs testGroupObs = getObs(encounter, testGroupConcept, null);
            if (testGroupObs.getConcept() == null) {
                testGroupObs.setConcept(testGroupConcept);
                testGroupObs.setOrder(test);
                setObsAttributes(testGroupObs, encounter);
                encounter.addObs(testGroupObs);
            }
            log.warn("Adding obs[concept=" + obs.getConcept() + ",uuid=" + obs.getUuid() + "] to obsgroup[concept=" + testGroupObs.getConcept() + ", uuid=" + testGroupObs.getUuid() + "]");
            testGroupObs.addGroupMember(obs);
        } else {
            encounter.addObs(obs);
        }

        log.warn("Obs size is: " + encounter.getObs().size());
    }

    /**
     * Convert PatientQueue List to PatientQueueMapping
     *
     * @param patientQueueList
     * @return
     */
    public List<PatientQueueMapper> mapPatientQueueToMapperWithOrders(List<PatientQueue> patientQueueList) {
        List<PatientQueueMapper> patientQueueMappers = new ArrayList<>();

        for (PatientQueue patientQueue : patientQueueList) {
            if (patientQueue.getEncounter() != null && !patientQueue.getEncounter().getOrders().isEmpty()) {
                String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " " + patientQueue.getPatient().getMiddleName();
                LabQueueMapper labQueueMapper = new LabQueueMapper();
                labQueueMapper.setId(patientQueue.getId());
                labQueueMapper.setPatientNames(names.replace("null", ""));
                labQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
                labQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
                labQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
                labQueueMapper.setProviderNames(patientQueue.getProvider().getName());
                labQueueMapper.setStatus(patientQueue.getStatus().name());
                labQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
                labQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
                labQueueMapper.setEncounterId(patientQueue.getEncounter().getEncounterId().toString());
                if (patientQueue.getEncounter() != null) {
                    labQueueMapper.setOrderMapper(Context.getService(UgandaEMRPOCService.class).processOrders(patientQueue.getEncounter().getOrders(), true));
                }
                patientQueueMappers.add(labQueueMapper);
            }
        }
        return patientQueueMappers;
    }

    /**
     * Convert PatientQueue List to PatientQueueMapping
     *
     * @param patientQueueList
     * @return
     */
    public List<PharmacyMapper> mapPatientQueueToMapperWithDrugOrders(List<PatientQueue> patientQueueList) {
        List<PharmacyMapper> patientQueueMappers = new ArrayList<>();

        for (PatientQueue patientQueue : patientQueueList) {
            if (patientQueue.getEncounter() != null && !patientQueue.getEncounter().getOrders().isEmpty()) {
                String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " " + patientQueue.getPatient().getMiddleName();
                PharmacyMapper pharmacyMapper = new PharmacyMapper();
                pharmacyMapper.setId(patientQueue.getId());
                pharmacyMapper.setPatientNames(names.replace("null", ""));
                pharmacyMapper.setPatientId(patientQueue.getPatient().getPatientId());
                pharmacyMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
                pharmacyMapper.setLocationTo(patientQueue.getLocationTo().getName());
                pharmacyMapper.setProviderNames(patientQueue.getProvider().getName());
                pharmacyMapper.setStatus(patientQueue.getStatus().name());
                pharmacyMapper.setAge(patientQueue.getPatient().getAge().toString());
                pharmacyMapper.setDateCreated(patientQueue.getDateCreated().toString());
                pharmacyMapper.setEncounterId(patientQueue.getEncounter().getEncounterId().toString());
                if (patientQueue.getEncounter() != null) {
                    pharmacyMapper.setDrugOrderMapper(processDrugOrders(patientQueue.getEncounter().getOrders()));
                }
                patientQueueMappers.add(pharmacyMapper);
            }
        }
        return patientQueueMappers;
    }

    /**
     * Set Attributes for Observation
     *
     * @param obs
     * @param encounter
     */
    private void setObsAttributes(Obs obs, Encounter encounter) {
        obs.setObsDatetime(encounter.getEncounterDatetime());
        obs.setPerson(encounter.getPatient());
        obs.setLocation(encounter.getLocation());
        obs.setEncounter(encounter);
    }

    /**
     * Get Existing Observation of The Encounter Which the results are going to be returned
     *
     * @param encounter
     * @param concept
     * @param groupingConcept
     * @return
     */
    private Obs getObs(Encounter encounter, Concept concept, Concept groupingConcept) {
        for (Obs obs : encounter.getAllObs()) {
            if (groupingConcept != null) {
                Obs obsGroup = getObs(encounter, groupingConcept, null);
                if (obsGroup.getGroupMembers() != null) {
                    for (Obs member : obsGroup.getGroupMembers()) {
                        if (member.getConcept().equals(concept)) {
                            return member;
                        }
                    }
                }
            } else if (obs.getConcept().equals(concept)) {
                return obs;
            }
        }
        return new Obs();
    }


    /**
     * @param session
     * @param locationToUUID
     * @param nextQueueStatus
     * @param completePreviousQueue
     */
    public void sendPatientToNextLocation(FormEntrySession session, String locationToUUID, String locationFromUUID, PatientQueue.Status nextQueueStatus, boolean completePreviousQueue) {
        PatientQueue patientQueue = new PatientQueue();
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        Location locationTo = Context.getLocationService().getLocationByUuid(locationToUUID);
        Location locationFrom = Context.getLocationService().getLocationByUuid(locationFromUUID);
        Provider provider = getProviderFromEncounter(session.getEncounter());


        try {
            if (!patientQueueExists(session.getEncounter(), locationTo, locationFrom, nextQueueStatus)) {
                PatientQueue previousQueue = null;
                if (completePreviousQueue) {
                    previousQueue = completePreviousQueue(session.getPatient(), session.getEncounter().getLocation(), PatientQueue.Status.PENDING);
                }
                patientQueue.setLocationFrom(session.getEncounter().getLocation());
                patientQueue.setPatient(session.getEncounter().getPatient());
                patientQueue.setLocationTo(locationTo);
                patientQueue.setProvider(provider);
                patientQueue.setEncounter(session.getEncounter());
                patientQueue.setStatus(nextQueueStatus);
                patientQueue.setCreator(Context.getUserService().getUsersByPerson(provider.getPerson(), false).get(0));
                patientQueue.setDateCreated(new Date());
                patientQueueingService.assignVisitNumberForToday(patientQueue);
                patientQueueingService.savePatientQue(patientQueue);
            }
        } catch (ParseException e) {
            log.error(e);
        }


    }

    public Encounter processLabTestOrdersFromEncounterObs(FormEntrySession session, boolean completePreviousQueue) {

        if (isRetrospective(session.getEncounter())) {
            return session.getEncounter();
        }

        EncounterService encounterService = Context.getEncounterService();
        Set<Order> orders = new HashSet<>();
        Encounter encounter = session.getEncounter();
        CareSetting careSetting = Context.getOrderService().getCareSettingByName(CARE_SETTING_OPD);
        Set<Obs> obsList = encounter.getObs();

        for (Obs obs : obsList) {
            if ((obs.getValueCoded() != null && (obs.getValueCoded().getConceptClass().getName().equals(LAB_SET_CLASS) || obs.getValueCoded().getConceptClass().getName().equals(TEST_SET_CLASS))) && !orderExists(obs.getValueCoded(), obs.getEncounter())) {
                TestOrder testOrder = new TestOrder();
                testOrder.setConcept(obs.getValueCoded());
                testOrder.setEncounter(obs.getEncounter());
                testOrder.setOrderer(getProviderFromEncounter(obs.getEncounter()));
                testOrder.setPatient(obs.getEncounter().getPatient());
                testOrder.setUrgency(Order.Urgency.STAT);
                testOrder.setCareSetting(careSetting);
                orders.add(testOrder);
            }
        }

        if (!orders.isEmpty()) {
            encounter.setOrders(orders);
            encounterService.saveEncounter(encounter);
            if (!session.getEncounter().getOrders().isEmpty()) {
                sendPatientToNextLocation(session, LAB_LOCATION_UUID, encounter.getLocation().getUuid(), PatientQueue.Status.PENDING, completePreviousQueue);
            }
        }
        return encounter;
    }

    private boolean orderExists(Concept concept, Encounter encounter) {
        List list = Context.getAdministrationService().executeSQL("select order_id from orders where concept_id=" + concept.getConceptId() + " AND encounter_id=" + encounter.getEncounterId(), true);
        boolean orderExists = false;
        if (!list.isEmpty()) {
            orderExists = true;
        }
        return orderExists;
    }


    public boolean patientQueueExists(Encounter encounter, Location locationTo, Location locationFrom, PatientQueue.Status status) throws ParseException {
        List list = Context.getAdministrationService().executeSQL("select patient_queue_id from patient_queue where encounter_id=" + encounter.getEncounterId() + " AND status='" + status.name() + "' AND location_to=" + locationTo.getLocationId() + " AND location_from=" + locationFrom.getLocationId() + " AND date_created BETWEEN \"" + DateFormatUtil.dateFormtterString(encounter.getEncounterDatetime(), DAY_START_TIME) + "\" AND \"" + DateFormatUtil.dateFormtterString(encounter.getEncounterDatetime(), DAY_END_TIME) + "\"", true);
        boolean orderExists = false;
        if (!list.isEmpty()) {
            orderExists = true;
        }
        return orderExists;
    }


    public Encounter processDrugOrdersFromEncounterObs(FormEntrySession session, boolean completePreviousQueue) {

        if (isRetrospective(session.getEncounter())) {
            return session.getEncounter();
        }

        EncounterService encounterService = Context.getEncounterService();
        ConceptService conceptService = Context.getConceptService();
        Set<Order> orders = new HashSet<>();
        Encounter encounter = session.getEncounter();
        CareSetting careSetting = Context.getOrderService().getCareSettingByName(CARE_SETTING_OPD);
        Set<Obs> obsList = encounter.getObs();
        OrderService orderService = Context.getOrderService();
        for (Obs obs : obsList) {
            if ((obs.getValueCoded() != null && (obs.getValueCoded().getConceptClass().getName().equals(DRUG_SET_CLASS))) && !orderExists(obs.getValueCoded(), obs.getEncounter())) {
                DrugOrder drugOrder = new DrugOrder();
                Set<Obs> obsGroupMembers = new HashSet<>();
                if (obs.getObsGroup() != null) {
                    obsGroupMembers.addAll((obs.getObsGroup().getGroupMembers()));

                    for (Obs groupMember : obsGroupMembers) {
                        switch (groupMember.getConcept().getConceptId()) {
                            case MEDICATION_QUANTITY_CONCEPT_ID:
                            case ARV_MEDICATION_QUANTITY_CONCEPT_ID:
                                drugOrder.setQuantity(groupMember.getValueNumeric());
                                drugOrder.setDose(groupMember.getValueNumeric());
                                break;
                            case MEDICATION_DURATION_CONCEPT_ID:
                            case ARV_MEDICATION_DURATION_CONCEPT_ID:
                                drugOrder.setDuration(groupMember.getValueNumeric().intValue());
                                break;
                            case MEDICATION_QUANTITY_UNIT_CONCEPT_ID:
                                drugOrder.setQuantityUnits(groupMember.getValueCoded());
                                drugOrder.setDoseUnits(groupMember.getValueCoded());
                                break;
                            case MEDICATION_DURATION_UNIT_CONCEPT_ID:
                                drugOrder.setDurationUnits(groupMember.getValueCoded());
                                break;
                            case MEDICATION_COMMENT_CONCEPT_ID:
                                drugOrder.setCommentToFulfiller(groupMember.getValueText());
                                break;
                            default:
                        }
                    }

                    if (drugOrder.getRoute() == null) {
                        drugOrder.setRoute(conceptService.getConcept(DEFALUT_ROUTE_CONCEPT_ID));
                    }

                    if (drugOrder.getDoseUnits() == null) {
                        drugOrder.setDoseUnits(conceptService.getConcept(DEFALUT_DOSE_UNIT_CONCEPT_ID));
                    }

                    if (drugOrder.getDurationUnits() == null) {
                        drugOrder.setDurationUnits(conceptService.getConcept(DEFALUT_DURATION_UNIT_CONCEPT_ID));
                    }

                    if (drugOrder.getFrequency() == null) {
                        drugOrder.setFrequency(Context.getOrderService().getOrderFrequencyByUuid(DEFALUT_ORDER_FREQUECNY_UUID));
                    }

                    if (drugOrder.getQuantityUnits() == null) {
                        drugOrder.setQuantityUnits(conceptService.getConcept(DEFALUT_DOSE_UNIT_CONCEPT_ID));
                    }

                    drugOrder.setNumRefills(1);
                    drugOrder.setEncounter(obs.getEncounter());
                    drugOrder.setOrderer(getProviderFromEncounter(obs.getEncounter()));
                    drugOrder.setPatient(obs.getEncounter().getPatient());
                    drugOrder.setUrgency(Order.Urgency.STAT);
                    drugOrder.setCareSetting(careSetting);
                    drugOrder.setConcept(obs.getValueCoded());
                    orders.add(drugOrder);
                }
            }
        }

        if (!orders.isEmpty()) {
            encounter.setOrders(orders);
            encounterService.saveEncounter(encounter);
            if (!session.getEncounter().getOrders().isEmpty()) {
                sendPatientToNextLocation(session, PHARMACY_LOCATION_UUID, encounter.getLocation().getUuid(), PatientQueue.Status.PENDING, completePreviousQueue);
                completePreviousQueue(session.getPatient(), session.getEncounter().getLocation(), PatientQueue.Status.PENDING);
            }
        }
        return encounter;
    }

    public Provider getProviderFromEncounter(Encounter encounter) {
        EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);

        Set<Provider> providers = encounter.getProvidersByRole(encounterRole);
        List<Provider> providerList = new ArrayList<>();
        for (Provider provider : providers) {
            providerList.add(provider);
        }

        if (!providerList.isEmpty()) {
            return providerList.get(0);
        } else {
            return null;
        }
    }

    public PatientQueue completePreviousQueue(Patient patient, Location location, PatientQueue.Status searchStatus) {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        PatientQueue patientQueue = getPreviousQueue(patient, location, searchStatus);
        if (patientQueue != null) {
            patientQueueingService.completePatientQueue(patientQueue);
        }
        return patientQueue;
    }

    public PatientQueue getPreviousQueue(Patient patient, Location location, PatientQueue.Status status) {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        PatientQueue previousQueue = null;

        List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueList(null, OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()), location, null, patient, null);

        if (!patientQueueList.isEmpty()) {
            previousQueue = patientQueueList.get(0);
        }
        return previousQueue;
    }


    private boolean isRetrospective(Encounter encounter) {
        return encounter.getEncounterDatetime().before(OpenmrsUtil.firstSecondOfDay(new Date()));
    }

}

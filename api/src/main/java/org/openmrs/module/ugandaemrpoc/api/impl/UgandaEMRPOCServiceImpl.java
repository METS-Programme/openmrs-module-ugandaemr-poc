package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.utils.DateFormatUtil;
import org.openmrs.util.OpenmrsUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.*;

public class UgandaEMRPOCServiceImpl extends BaseOpenmrsService implements UgandaEMRPOCService {

    protected final Log log = LogFactory.getLog(UgandaEMRPOCServiceImpl.class);

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
}

package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.LAB_LOCATION_UUID;
import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.QUEUE_STATUS_FROM_LAB;

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
}

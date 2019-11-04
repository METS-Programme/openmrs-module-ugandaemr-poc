package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.List;

public abstract interface UgandaEMRPOCService extends OpenmrsService {

	/**
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList);

	/**
	 *
	 * @param patient
	 * @param location
	 * @param searchStatus
	 * @return
	 */
	public PatientQueue completePreviousQueue(Patient patient, Location location, PatientQueue.Status searchStatus);


	/**
	 * @param patient
	 * @param location
	 * @return
	 */
	public PatientQueue getPreviousQueue(Patient patient, Location location,PatientQueue.Status status);
}

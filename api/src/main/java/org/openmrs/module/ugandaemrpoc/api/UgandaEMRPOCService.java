package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Order;
import org.openmrs.Encounter;
import org.openmrs.Concept;
import org.openmrs.Provider;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.lab.mapper.OrderMapper;
import org.openmrs.module.ugandaemrpoc.api.lab.util.TestResultModel;
import org.openmrs.module.ugandaemrpoc.pharmacy.DispensingModelWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.module.ugandaemrpoc.pharmacy.mapper.PharmacyMapper;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.List;

public abstract interface UgandaEMRPOCService extends OpenmrsService {

	/**
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueVisitMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList);

	/**
	 * Render Tests
	 * @param test
	 * @return
	 */
	public Set<TestResultModel> renderTests(Order test);

	/**
	 * Check if Sample ID exists
	 * @param sampleId
	 * @param orderNumber
	 * @return
	 * @throws ParseException
	 */
	public boolean isSampleIdExisting(String sampleId, String orderNumber) throws ParseException;

	/**
	 * Process Orders
	 * @param query
	 * @param asOfDate
	 * @param includeProccesed
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SimpleObject getProcessedOrders(String query, Date asOfDate, boolean includeProccesed) throws ParseException,
	        IOException;

	/**
	 * Convert Orders to OrderMappers
	 *
	 * @param orders
	 * @param fiterOutProccessed
	 * @return
	 */
	public Set<OrderMapper> processOrders(Set<Order> orders, boolean fiterOutProccessed);

	/**
	 * @param encounter
	 * @param testConcept
	 * @param testGroupConcept
	 * @param result
	 * @param test
	 */
	public void addLaboratoryTestObservation(Encounter encounter, Concept testConcept, Concept testGroupConcept,
	        String result, Order test);
	/**
	 * With Orders
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueMapper> mapPatientQueueToMapperWithOrders(List<PatientQueue> patientQueueList);



	/**
	 * With Orders
	 *
	 * @param patientQueueList
	 * @return
	 */
	public List<PharmacyMapper> mapPatientQueueToMapperWithDrugOrders(List<PatientQueue> patientQueueList);

	/**
	 * Process Orders
	 * @param formSession
	 * @return
	 */
	public Encounter processLabTestOrdersFromEncounterObs(FormEntrySession formSession, boolean completePreviousQueue);


	/**
	 * Process Orders
	 *
	 * @param formSession
	 * @return
	 */
	public Encounter processDrugOrdersFromEncounterObs(FormEntrySession formSession, boolean completePreviousQueue);


	/**
	 * Send Patient To Lab
	 * @param session
	 */
	public void sendPatientToNextLocation(FormEntrySession session, String locationUUID,String locationFromUUID,PatientQueue.Status nextQueueStatus,boolean completePreviousQueue);



	/**
	 * @param encounter
	 * @return
	 */
	Provider getProviderFromEncounter(Encounter encounter);

	/**
	 * @param query
	 * @param encounterId
	 * @param includeProccesed
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SimpleObject getOrderResultsOnEncounter(String query, int encounterId, boolean includeProccesed)
	        throws ParseException, IOException;

	/**
	 * @param encounter
	 * @param locationTo
	 * @return
	 * @throws ParseException
	 */
	public boolean patientQueueExists(Encounter encounter, Location locationTo,Location locationFrom,PatientQueue.Status status) throws ParseException;

	/**
	 * Complete Previous Queue of Patient
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


	/**
	 * This Method completes all facility out patient active patient visits found.
	 * @param patient the patient whose visits are to be completed
	 */
	public void completePatientActiveVisit(Patient patient);



	/**
	 * Dispenses medications in pharmacy
	 * @param resultWrapper the data object containing dispensing information
	 * @param provider the provider dispensing the medication
	 * @param location the location where the medication is being dispensed from
	 * @return simple object containing information about that status of dispensing
	 */
	public SimpleObject dispenseMedication(DispensingModelWrapper resultWrapper, Provider provider, Location location);
}

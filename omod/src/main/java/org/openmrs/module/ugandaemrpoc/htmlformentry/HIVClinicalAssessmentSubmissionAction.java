package org.openmrs.module.ugandaemrpoc.htmlformentry;


import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.aijar.api.AijarService;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;

import java.util.*;

import static org.openmrs.module.aijar.AijarConstants.GP_DSDM_CONCEPT_ID;
import static org.openmrs.module.aijar.AijarConstants.GP_DSDM_PROGRAM_UUID_NAME;
import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.CONCEPT_ID_NEXT_APPOINTMENT;
import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.CONCEPT_ID_TRANSFERED_OUT;

/**
 * Enrolls patients into DSDM programs
 */
public class HIVClinicalAssessmentSubmissionAction implements CustomFormSubmissionAction {

    @Override
    public void applyAction(FormEntrySession session) {
        UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
        Mode mode = session.getContext().getMode();
        if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
            return;
        }

        //Create DSDM Program on entry of ART Summary Page
        if (mode.equals(Mode.ENTER)
                && session.getEncounter().getEncounterType() == Context.getEncounterService().getEncounterTypeByUuid(
                "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f")) {
            createPatientProgram(session.getEncounter().getPatient(), session.getEncounter().getEncounterDatetime(), Context
                    .getProgramWorkflowService().getProgramByUuid("de5d54ae-c304-11e8-9ad0-529269fb1459"));
            return;
        }

        if (ugandaEMRPOCService.getPreviousQueue(session.getPatient(), session.getEncounter().getLocation(), PatientQueue.Status.PENDING) != null) {
            ugandaEMRPOCService.processLabTestOrdersFromEncounterObs(session, true);

            ugandaEMRPOCService.processDrugOrdersFromEncounterObs(session, true);

            completeClinicianQueue(session.getEncounter());
        }

        Patient patient = session.getPatient();
        Set<Obs> obsList = session.getEncounter().getAllObs();
        List<PatientProgram> patientPrograms = getActivePatientProgramAfterThisEncounter(patient, null, session
                .getEncounter().getEncounterDatetime());

        List<Program> dsdmPrograms = getDSDMPrograms();

        if (mode.equals(Mode.EDIT)
                && session.getEncounter().getEncounterType() != Context.getEncounterService().getEncounterTypeByUuid(
                "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f")) {
            List<PatientProgram> patientProgramOnEncounterDate = getActivePatientProgramAfterThisEncounter(patient, session
                    .getEncounter().getEncounterDatetime(), session.getEncounter().getEncounterDatetime());

            if (obsList != null && getProgramByConceptFromObs(obsList) == null && patientProgramOnEncounterDate.size() > 0) {
                for (PatientProgram currentPatientProgram : patientProgramOnEncounterDate) {
                    /**
                     * Void DSDM Program whose obs has been voided
                     */
                    voidPatientProgram(currentPatientProgram, "Matching Observation voided", session.getEncounter()
                            .getChangedBy(), session.getEncounter().getDateChanged());

                    List<PatientProgram> previousPatientPrograms = openPreviouslyClosedPatientProgram(patient,
                            currentPatientProgram.getDateEnrolled(), session.getEncounter().getChangedBy());

                    if (previousPatientPrograms.size() > 0) {
                        for (PatientProgram previousPatientProgram : previousPatientPrograms) {
                            if (dsdmPrograms.contains(currentPatientProgram.getProgram())) {

                                /**
                                 * Void Previous DSDM Program
                                 */
                                voidPatientProgram(previousPatientProgram, "Matching Observation voided", session
                                        .getEncounter().getChangedBy(), session.getEncounter().getDateChanged());

                                /**
                                 * Recreate Voided Program
                                 */
                                createPatientProgram(patient, previousPatientProgram.getDateEnrolled(),
                                        previousPatientProgram.getProgram());
                            }
                        }
                    }
                }

            } else if (obsList != null && patientProgramOnEncounterDate.size() > 0
                    && getProgramByConceptFromObs(obsList) != null
                    && getProgramByConceptFromObs(obsList) != patientProgramOnEncounterDate.get(0).getProgram()) {

                for (PatientProgram patientProgram : patientProgramOnEncounterDate) {
                    voidPatientProgram(patientProgram, "Matching Observation voided", session.getEncounter().getChangedBy(),
                            session.getEncounter().getDateChanged());
                }
            }
        }

        /**
         * Terminate wen patient is already enrolled in the program selected.
         */
        for (PatientProgram patientProgram : patientPrograms) {
            /**
             * Check if Same program is enrolled
             */
            if (patientProgram.getProgram() == getProgramByConceptFromObs(obsList)) {
                return;
            }
            /**
             * Check if Same program is enrolled on the same date
             */
            if (patientProgram.getProgram() == getProgramByConceptFromObs(obsList)
                    && patientProgram.getDateEnrolled() == session.getEncounter().getEncounterDatetime()) {
                return;
            }
        }

        /**
         * Completing all DSDM active programs of the patient
         */
        if (!patientPrograms.isEmpty()) {
            for (PatientProgram previousPatientDSDMProgram : patientPrograms) {
                /**
                 * Check if program to enroll is greater than
                 */
                if (session.getEncounter().getEncounterDatetime().compareTo(previousPatientDSDMProgram.getDateEnrolled()) > 0) {
                    if (getDSDMPrograms().contains(previousPatientDSDMProgram.getProgram())) {
                        previousPatientDSDMProgram.setDateCompleted(session.getEncounter().getEncounterDatetime());
                        Context.getProgramWorkflowService().savePatientProgram(previousPatientDSDMProgram);
                    }
                }
            }
        }

        /**
         * Enroll patient in new PatientProgram
         */
        if (getProgramByConceptFromObs(obsList) != null) {
            createPatientProgram(patient, session.getEncounter().getEncounterDatetime(), getProgramByConceptFromObs(obsList));
        }


        /**
         * Create TransferOut encounter When Patient is Transferred Out
         */
        createTransferOutEncounter(session);
    }

    private PatientProgram createPatientProgram(Patient patient, Date enrollmentDate, Program program) {
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setPatient(patient);
        patientProgram.setDateEnrolled(enrollmentDate);
        patientProgram.setProgram(program);
        patientProgram.setDateCompleted(null);
        return Context.getProgramWorkflowService().savePatientProgram(patientProgram);
    }

    private PatientProgram voidPatientProgram(PatientProgram patientProgram, String reason, User user, Date changedDated) {
        patientProgram.setVoided(true);
        patientProgram.setVoidedBy(user);
        patientProgram.setVoidReason(reason);
        patientProgram.setDateChanged(changedDated);
        return Context.getProgramWorkflowService().savePatientProgram(patientProgram);
    }

    private List<PatientProgram> openPreviouslyClosedPatientProgram(Patient patient, Date date, User user) {
        return getCompletedPatientProgramOnDate(patient, date);
    }

    /**
     * Get Program from selected value of the DSDM field on the form
     *
     * @param obsList
     * @return
     */
    private Program getProgramByConceptFromObs(Set<Obs> obsList) {
        List<Program> programList = new ArrayList<>();
        Program program = null;
        String dsdmConceptId = Context.getAdministrationService().getGlobalProperty(GP_DSDM_CONCEPT_ID);
        for (Obs obs : obsList) {
            if (obs.getConcept().getConceptId() == Integer.parseInt(dsdmConceptId) && obs.getConcept().getDatatype() == Context.getConceptService().getConceptDatatypeByName("Coded")) {
                programList = Context.getProgramWorkflowService().getProgramsByConcept(obs.getValueCoded());
            }
        }
        if (!programList.isEmpty()) {
            program = programList.get(0);
        }
        return program;
    }

    /**
     * This takes in a patient program and weather previous or after search and determines if there
     * is a previous program or there is an after program
     *
     * @param patient
     * @param minEnrollmentDate
     * @param maxEnrollmentDate
     * @return
     */
    private List<PatientProgram> getActivePatientProgramAfterThisEncounter(Patient patient, Date minEnrollmentDate,
                                                                           Date maxEnrollmentDate) {
        List<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();

        patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(patient, null, minEnrollmentDate,
                maxEnrollmentDate, null, null, false);
        return patientPrograms;
    }

    /**
     * This takes in a patient program and weather previous or after search and determines if there
     * is a previous program or there is an after program
     *
     * @param patient
     * @param completionDate
     * @return
     */
    private List<PatientProgram> getCompletedPatientProgramOnDate(Patient patient, Date completionDate) {
        List<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();

        patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null, completionDate,
                completionDate, false);
        return patientPrograms;
    }

    /**
     * Get List of DSDM Programs
     *
     * @return
     */
    public List<Program> getDSDMPrograms() {
        String dsdmuuids = Context.getAdministrationService().getGlobalProperty(GP_DSDM_PROGRAM_UUID_NAME);

        List<String> listOfDSDMPrograms = Arrays.asList(dsdmuuids.split("\\s*,\\s*"));
        List<Program> dsdmPrograms = new ArrayList<>();

        for (String s : listOfDSDMPrograms) {

            Program dsdmProgram = Context.getProgramWorkflowService().getProgramByUuid(s);
            if (dsdmProgram != null) {
                dsdmPrograms.add(dsdmProgram);
            }
        }
        return dsdmPrograms;
    }

    private void completeClinicianQueue(Encounter encounter) {
        UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
        for (Obs obs : encounter.getAllObs(false)) {
            if (obs.getConcept().getConceptId().equals(CONCEPT_ID_NEXT_APPOINTMENT) || obs.getConcept().getConceptId().equals(CONCEPT_ID_TRANSFERED_OUT)) {
                ugandaEMRPOCService.completePreviousQueue(obs.getEncounter().getPatient(), encounter.getLocation(), PatientQueue.Status.PENDING);
                Context.getVisitService().endVisit(encounter.getVisit(), new Date());
            }

        }
    }


    private Encounter createTransferOutEncounter(FormEntrySession formEntrySession) {
        EncounterService encounterService = Context.getEncounterService();
        AijarService aijarService = Context.getService(AijarService.class);
        Encounter encounter = null;

        if (getObsByConceptFromSet(formEntrySession.getEncounter().getAllObs(), 90306) != null) {
            encounter = new Encounter();
            encounter.setEncounterType(encounterService.getEncounterTypeByUuid("e305d98a-d6a2-45ba-ba2a-682b497ce27c"));
            encounter.setLocation(formEntrySession.getEncounter().getLocation());
            encounter.setPatient(formEntrySession.getPatient());
            encounter.setVisit(formEntrySession.getEncounter().getVisit());
            encounter.setEncounterDatetime(formEntrySession.getEncounter().getEncounterDatetime());
            encounter.setForm(Context.getFormService().getFormByUuid("45d9db68-e4b5-11e7-80c1-9a214cf093ae"));

            Obs transferOutToObs = aijarService.generateObsFromObs(formEntrySession.getEncounter().getAllObs(), 90211, 90211, encounter);
            encounter.addObs(transferOutToObs);

            encounterService.saveEncounter(encounter);
        }

        return encounter;
    }

    private Obs getObsByConceptFromSet(Set<Obs> obsSet, Integer lookupConceptId) {
        for (Obs obs : obsSet) {
            if (lookupConceptId.equals(obs.getConcept().getConceptId())) {
                return obs;
            }
        }
        return null;
    }
}

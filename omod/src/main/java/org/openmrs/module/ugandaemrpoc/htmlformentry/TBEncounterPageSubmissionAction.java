package org.openmrs.module.ugandaemrpoc.htmlformentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;

import java.util.List;
import java.util.Set;


/**
 * Enrolls patients into DSDM programs
 */
public class TBEncounterPageSubmissionAction implements CustomFormSubmissionAction {

    private static final Log log = LogFactory.getLog(TBEncounterPageSubmissionAction.class);
    UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);

    @Override
    public void applyAction(FormEntrySession session) {
        Mode mode = session.getContext().getMode();
        if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
            return;
        }

        if (ugandaEMRPOCService.getPreviousQueue(session.getPatient(), session.getEncounter().getLocation(), PatientQueue.Status.PENDING) != null) {
            ugandaEMRPOCService.processLabTestOrdersFromEncounterObs(session, true);

            ugandaEMRPOCService.processDrugOrdersFromEncounterObs(session, true);
        }
        endTBProgram(session);
    }

    private PatientProgram endTBProgram(FormEntrySession formEntrySession) {
        ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
        PatientProgram endedPatientProgram = null;
        Obs obs=getObsByConceptFromSet(formEntrySession.getEncounter().getAllObs(), 99423);


        if (getObsByConceptFromSet(formEntrySession.getEncounter().getAllObs(), 99423) != null) {

            List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(formEntrySession.getPatient(), programWorkflowService.getProgramByUuid("9dc21a72-0971-11e7-8037-507b9dc4c741"), null, null, null, null, false);

            for (PatientProgram patientProgram : patientPrograms) {
                if (patientProgram.getActive()) {
                    patientProgram.setDateCompleted(formEntrySession.getEncounter().getEncounterDatetime());
                    patientProgram.setOutcome(obs.getValueCoded());
                    endedPatientProgram = programWorkflowService.savePatientProgram(patientProgram);
                }
            }
        }

        return endedPatientProgram;
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



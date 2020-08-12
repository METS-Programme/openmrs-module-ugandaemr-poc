package org.openmrs.module.ugandaemrpoc.htmlformentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;

import java.util.List;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.*;

/**
 * Enrolls patients into DSDM programs
 */
public class TBSummaryPageSubmissionAction implements CustomFormSubmissionAction {

    private static final Log log = LogFactory.getLog(TBSummaryPageSubmissionAction.class);

    @Override
    public void applyAction(FormEntrySession session) {
        Mode mode = session.getContext().getMode();
        if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
            return;
        }
        ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
        UgandaEMRPOCService ugandaEMRPOCService=Context.getService(UgandaEMRPOCService.class);
        List<PatientProgram> patientProgramList = programWorkflowService.getPatientPrograms(session.getPatient(), programWorkflowService.getProgramByUuid("160541AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, null, null, null, false);
        PatientProgram currentPatientProgram = null;

        for (PatientProgram patientProgram : patientProgramList) {
            if (patientProgram.getActive()) {
                currentPatientProgram = patientProgram;
                continue;
            }
        }

        if (currentPatientProgram != null) {
            currentPatientProgram.setAttribute(ugandaEMRPOCService.generatePatientProgramAttributeFromObservation(currentPatientProgram, session.getEncounter().getAllObs(), TB_HEALTH_UNIT_NUMBER_CONCEPT_ID, TB_HEALTH_UNIT_NUMBER_PROGRAM_ATTRIBUTE_TYPE_UUID));
            currentPatientProgram.setAttribute(ugandaEMRPOCService.generatePatientProgramAttributeFromObservation(currentPatientProgram, session.getEncounter().getAllObs(), TB_DISTRICT_NUMBER_CONCEPT_ID, TB_DISTRICT_NUMBER_PROGRAM_ATTRIBUTE_TYPE_UUID));
            currentPatientProgram.setAttribute(ugandaEMRPOCService.generatePatientProgramAttributeFromObservation(currentPatientProgram, session.getEncounter().getAllObs(), TB_REGIMEN_CONCEPT_ID, TB_REGIMEN_PROGRAM_ATTRIBUTE_TYPE_UUID));
            programWorkflowService.savePatientProgram(currentPatientProgram);
        }
    }



}

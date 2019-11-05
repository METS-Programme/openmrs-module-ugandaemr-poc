package org.openmrs.module.ugandaemrpoc.htmlformentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.util.OpenmrsUtil;

import java.util.Date;
import java.util.List;

/**
 * Enrolls patients into DSDM programs
 */
public class TriageFormSubmissionAction implements CustomFormSubmissionAction {

    private static final Log log = LogFactory.getLog(TriageFormSubmissionAction.class);

    @Override
    public void applyAction(FormEntrySession session) {
        Mode mode = session.getContext().getMode();
        if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
            return;
        }

        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);

        PatientQueue patientQueue = new PatientQueue();

        List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueList(null,OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()),session.getEncounter().getLocation(),null,session.getPatient(), null);

        if (!patientQueueList.isEmpty()) {
            patientQueue = patientQueueList.get(0);
            patientQueue.setEncounter(session.getEncounter());
            patientQueueingService.savePatientQue(patientQueue);
        }

    }
}

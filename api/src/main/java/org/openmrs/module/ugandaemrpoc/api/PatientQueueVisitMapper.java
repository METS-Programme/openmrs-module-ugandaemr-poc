package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;

import java.io.Serializable;

public class PatientQueueVisitMapper extends PatientQueueMapper  implements Serializable {
    Integer visitId;

    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }
}

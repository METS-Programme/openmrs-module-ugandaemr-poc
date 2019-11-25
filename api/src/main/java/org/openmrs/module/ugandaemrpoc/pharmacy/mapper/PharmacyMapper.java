package org.openmrs.module.ugandaemrpoc.pharmacy.mapper;

import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;

import java.io.Serializable;
import java.util.Set;

public class PharmacyMapper extends PatientQueueMapper implements Serializable {

    Set<DrugOrderMapper> drugOrderMappers;

    public PharmacyMapper() {
    }

    public Set<DrugOrderMapper> getOrderMapper() {
        return drugOrderMappers;
    }

    public void setDrugOrderMapper(Set<DrugOrderMapper> orderMapper) {
        this.drugOrderMappers = orderMapper;
    }
}
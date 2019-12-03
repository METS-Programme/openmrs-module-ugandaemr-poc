package org.openmrs.module.ugandaemrpoc.pharmacy;

import org.openmrs.module.ugandaemrpoc.pharmacy.mapper.DrugOrderMapper;

import java.util.List;

public class DispensingModelWrapper {

	private Integer encounterId;
	private Integer patientQueueId;

	private List<DrugOrderMapper> drugOrderMappers;

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	public List<DrugOrderMapper> getDrugOrderMappers() {
		return drugOrderMappers;
	}

	public void setDrugOrderMappers(List<DrugOrderMapper> drugOrderMappers) {
		this.drugOrderMappers = drugOrderMappers;
	}

	public Integer getPatientQueueId() {
		return patientQueueId;
	}

	public void setPatientQueueId(Integer patientQueueId) {
		this.patientQueueId = patientQueueId;
	}
}


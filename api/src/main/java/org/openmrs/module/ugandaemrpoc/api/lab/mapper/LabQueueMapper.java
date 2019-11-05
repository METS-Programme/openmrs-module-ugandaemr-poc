package org.openmrs.module.ugandaemrpoc.api.lab.mapper;

import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;

import java.io.Serializable;
import java.util.Set;

public class LabQueueMapper extends PatientQueueMapper implements Serializable {

	Set<OrderMapper> orderMapper;

	public LabQueueMapper() {
	}

	public Set<OrderMapper> getOrderMapper() {
		return orderMapper;
	}

	public void setOrderMapper(Set<OrderMapper> orderMapper) {
		this.orderMapper = orderMapper;
	}
}

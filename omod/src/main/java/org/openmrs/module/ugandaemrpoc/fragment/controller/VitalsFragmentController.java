package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

public class VitalsFragmentController {

	protected final Log log = LogFactory.getLog(getClass());

	public VitalsFragmentController() {
	}

	public void controller(@SpringBean FragmentModel fragmentModel,
	        @SpringBean("patientService") PatientService patientService,
	        @SpringBean("locationService") LocationService locationService,
	        @RequestParam(value = "patientId", required = false) Patient patient, UiSessionContext uiSessionContext) {
		if (patient != null) {
			fragmentModel.put("birthDate", patient.getBirthdate());
			fragmentModel.put("patient", patient);
			fragmentModel.put("patientId", patient.getPatientId());
		}

		AppDescriptor appDescriptor = new AppDescriptor();
		ObjectNode jsonNodes = JsonNodeFactory.instance.objectNode();
		jsonNodes.put("encounterTypeUuid", "0f1ec66d-61db-4575-8248-94e10a88178f");
		jsonNodes.put("encounterDateLabel", "Vitals");
		appDescriptor.setConfig(jsonNodes);

		fragmentModel.put("app", appDescriptor);
		fragmentModel.put("patientId", patient.getPatientId());
	}
}

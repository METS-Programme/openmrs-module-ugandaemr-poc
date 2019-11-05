package org.openmrs.module.ugandaemrpoc.api.lab.util;

import java.io.Serializable;

public class ResultModel implements Serializable {

	private String conceptName;

	private String selectedOption;

	private String value;

	public String getConceptName() {
		return conceptName;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public String getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(String selectedOption) {
		this.selectedOption = selectedOption;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

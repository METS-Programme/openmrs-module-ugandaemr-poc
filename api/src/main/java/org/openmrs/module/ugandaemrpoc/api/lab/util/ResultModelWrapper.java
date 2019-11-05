package org.openmrs.module.ugandaemrpoc.api.lab.util;

import java.util.List;

public class ResultModelWrapper {

	private Integer testId;

	private List<ResultModel> results;

	public Integer getTestId() {
		return testId;
	}

	public void setTestId(Integer testId) {
		this.testId = testId;
	}

	public List<ResultModel> getResults() {
		return results;
	}

	public void setResults(List<ResultModel> results) {
		this.results = results;
	}
}

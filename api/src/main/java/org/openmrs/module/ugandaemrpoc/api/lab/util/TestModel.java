/**
 * Copyright 2011 Society for Health Information Systems Programmes, India (HISP India)
 * <p>
 * This file is part of Laboratory module.
 * <p>
 * Laboratory module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Laboratory module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Laboratory module.  If not, see <http://www.gnu.org/licenses/>.
 **/

package org.openmrs.module.ugandaemrpoc.api.lab.util;

import org.openmrs.Concept;

import java.util.Comparator;

public class TestModel implements Comparator<TestModel>, Comparable<TestModel> {

	private String startDate;

	private String patientIdentifier;

	private Integer patientId;

	private String patientName;

	private String gender;

	private Integer age;

	private Concept test;

	private Concept testName;

	private Integer orderId;

	private String status;

	private Integer testId;

	private String acceptedDate;

	private String investigation;

	private Integer encounterId;

	private Integer conceptId;

	private String sampleId;

	public String value;

	public TestModel() {
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Concept getTest() {
		return test;
	}

	public void setTest(Concept test) {
		this.test = test;
	}

	public Concept getTestName() {
		return testName;
	}

	public void setTestName(Concept testName) {
		this.testName = testName;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTestId() {
		return testId;
	}

	public void setTestId(Integer testId) {
		this.testId = testId;
	}

	public String getAcceptedDate() {
		return acceptedDate;
	}

	public void setAcceptedDate(String acceptedDate) {
		this.acceptedDate = acceptedDate;
	}

	public String getInvestigation() {
		return investigation;
	}

	public void setInvestigation(String investigation) {
		this.investigation = investigation;
	}

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	public Integer getConceptId() {
		return conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int compareTo(TestModel t) {
		return (this.patientName).compareTo(t.patientName);
	}

	public int compare(TestModel t, TestModel t1) {
		return 0;
	}

}

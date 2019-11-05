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

import org.openmrs.*;
import org.openmrs.api.context.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LaboratoryUtil {

	/**
	 * Generate parameter models
	 *
	 * @param parameters
	 * @param concept
	 */
	public static void generateParameterModels(List<ParameterModel> parameters, Concept concept, Concept parentConcept,
	        Order order) {
		if (concept.getConceptClass().getName().equalsIgnoreCase("LabSet")) {
			List<Concept> concepts = getParameterConcepts(concept);
			for (Concept c : concepts) {
				generateParameterModels(parameters, c, concept, order);
			}
		} else {
			ParameterModel parameter = generateParameterModel(concept, parentConcept, order);
			parameters.add(parameter);
		}
	}

	private static List<Concept> getParameterConcepts(Concept concept) {

		List<Concept> concepts = new ArrayList<Concept>();
		for (ConceptSet cs : concept.getConceptSets()) {
			Concept c = cs.getConcept();
			concepts.add(c);
		}
		return concepts;
	}

	private static ParameterModel generateParameterModel(Concept concept, Concept parentConcept, Order order) {
		ParameterModel parameter = new ParameterModel();
		parameter.setId(concept.getConceptId().toString());
		if (parentConcept != null) {
			parameter.setContainer(parentConcept.getDisplayString());
			parameter.setContainerId(parentConcept.getId());
		}
		setDefaultParameterValue(concept, parentConcept, order.getEncounter(), parameter);
		if (concept.getDatatype().getName().equalsIgnoreCase("Text")) {
			parameter.setType("text");
		} else if (concept.getDatatype().getName().equalsIgnoreCase("Numeric")) {
			parameter.setType("number");
			parameter.setUnit(getUnit(concept));
		} else if (concept.getDatatype().getName().equalsIgnoreCase("Coded")) {
			parameter.setType("select");

			for (ConceptAnswer ca : concept.getAnswers()) {
				Concept c = ca.getAnswerConcept();
				parameter.addOption(new ParameterOption(c.getName().getName(), c.getId().toString()));
			}
		}
		parameter.setValidator(" required");
		parameter.setTitle(concept.getName().getName());
		return parameter;
	}

	/**
	 * Generate list of test models using tests
	 *
	 * @param tests
	 * @return
	 */
	public static List<TestModel> generateModelsFromTests(Order tests) {

		List<TestModel> models = new ArrayList<TestModel>();
		TestModel tm = generateModel(tests);
		models.add(tm);
		return models;
	}

	private static void setDefaultParameterValue(Concept concept, Concept parentConcept, Encounter encounter,
	        ParameterModel parameter) {
		if (encounter != null) {
			for (Obs obs : encounter.getAllObs()) {
				if (parentConcept != null && obs.getObsGroup() != null
				        && obs.getObsGroup().getConcept().equals(parentConcept) && obs.getConcept().equals(concept)) {
					parameter.setDefaultValue(obs.getValueAsString(Context.getLocale()));
					break;
				} else if (concept.equals(obs.getConcept()) && obs.getObsGroup() == null) {
					parameter.setDefaultValue(obs.getValueAsString(Context.getLocale()));
					break;
				}
			}
		}
	}

	private static TestModel generateModel(Order order) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		TestModel tm = new TestModel();
		tm.setStartDate(sdf.format(order.getDateActivated()));
		tm.setPatientId(order.getPatient().getPatientId());
		tm.setPatientIdentifier(order.getPatient().getPatientIdentifier().getIdentifier());
		tm.setPatientName(order.getPatient().getFamilyName());
		tm.setGender(order.getPatient().getGender());
		tm.setAge(order.getPatient().getAge());
		tm.setTest(order.getConcept());
		tm.setOrderId(order.getOrderId());

		if (order != null) {

			tm.setTestId(order.getOrderId());
			tm.setAcceptedDate(sdf.format(order.getDateActivated()));
			tm.setConceptId(order.getConcept().getConceptId());
			tm.setSampleId(order.getAccessionNumber());
			if (order.getEncounter() != null)
				tm.setEncounterId(order.getEncounter().getEncounterId());
		} else {
			tm.setStatus(null);
		}

		// get investigation from test tree map

		tm.setInvestigation(order.getConcept().getName().getName());

		return tm;
	}

	/**
	 * Search for concept using name
	 *
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Concept searchConcept(String name) {
		Concept concept = Context.getConceptService().getConcept(name);
		if (concept != null) {
			return concept;
		} else {
			List<Concept> cws = Context.getConceptService().getConceptsByName(name, new Locale("en"), false);
			if (!cws.isEmpty())
				return cws.get(0);
		}
		return null;
	}

	private static String getUnit(Concept concept) {
		ConceptNumeric cn = Context.getConceptService().getConceptNumeric(concept.getConceptId());
		return cn.getUnits();
	}
}

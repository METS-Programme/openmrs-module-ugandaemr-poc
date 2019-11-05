/**
 *  Copyright 2011 Society for Health Information Systems Programmes, India (HISP India)
 *
 *  This file is part of Laboratory module.
 *
 *  Laboratory module is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  Laboratory module is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Laboratory module.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/

package org.openmrs.module.ugandaemrpoc.api.lab.util;

import java.util.ArrayList;
import java.util.List;

public class ParameterModel implements Comparable<ParameterModel> {

	public String id;

	public String type;

	public String title;

	public String container;

	public Integer containerId;

	public List<ParameterOption> options = new ArrayList<ParameterOption>();

	public String defaultValue;

	public String unit;

	public String validator;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ParameterOption> getOptions() {
		return options;
	}

	public void addOption(ParameterOption option) {
		this.options.add(option);
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String value) {
		this.defaultValue = value;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public Integer getContainerId() {
		return containerId;
	}

	public void setContainerId(Integer containerId) {
		this.containerId = containerId;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getValidator() {
		return validator;
	}

	public void setValidator(String validator) {
		this.validator = validator;
	}

	public int compareTo(ParameterModel otherParameterModel) {
		if (otherParameterModel.getContainerId() == null)
			return 1;
		if (this.getContainerId() == null)
			return -1;
		Integer thisContainerId = containerId;
		Integer otherContainerId = otherParameterModel.getContainerId();
		return thisContainerId.compareTo(otherContainerId);
	}

	public String toString() {
		return "ParameterModel [id=" + id + "]";
	}
}

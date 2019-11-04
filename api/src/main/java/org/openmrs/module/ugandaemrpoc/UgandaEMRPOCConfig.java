package org.openmrs.module.ugandaemrpoc;

import org.springframework.stereotype.Component;

@Component("ugandaemrpoc.UgandaEMRPOCConfig")
public class UgandaEMRPOCConfig {
	
	public static final String MODULE_ID = "ugandaemrpoc";
	
	public static final String MODULE_PRIVILEGE = "UgandaEMRPOC Privilege";
	
	public static final String TRIAGE_LOCATION_UUID = "ff01eaab-561e-40c6-bf24-539206b521ce";
	public static final String LAB_LOCATION_UUID = "ba158c33-dc43-4306-9a4a-b4075751d36c";

	public static final String QUEUE_STATUS_FROM_LAB = "from lab";

	public UgandaEMRPOCConfig() {
	}
}

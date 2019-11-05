package org.openmrs.module.ugandaemrpoc;

import org.springframework.stereotype.Component;

@Component("ugandaemrpoc.UgandaEMRPOCConfig")
public class UgandaEMRPOCConfig {
	
	public static final String MODULE_ID = "ugandaemrpoc";
	
	public static final String MODULE_PRIVILEGE = "UgandaEMRPOC Privilege";
	
	public static final String TRIAGE_LOCATION_UUID = "ff01eaab-561e-40c6-bf24-539206b521ce";
	public static final String LAB_LOCATION_UUID = "ba158c33-dc43-4306-9a4a-b4075751d36c";

    public static final String  DAY_START_TIME = "00:00:00";
    public static final String  DAY_END_TIME = "23:59:59";

	public static final String QUEUE_STATUS_FROM_LAB = "from lab";

	public static final int CONCEPT_ID_NEXT_APPOINTMENT = 5096;
	public static final int CONCEPT_ID_TRANSFERED_OUT = 90306;

    public static final String PROCESSED_ORDER_WITH_RESULT_OF_ENCOUNTER_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where orders.accession_number!=\"\" and specimen_source!=\"\" AND orders.encounter_id=%s";

    public static final String ENCOUNTER_ROLE = "240b26f9-dd88-4172-823d-4a8bfeb7841f";
	public UgandaEMRPOCConfig() {
	}
}

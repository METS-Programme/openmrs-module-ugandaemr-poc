package org.openmrs.module.ugandaemrpoc;

import org.springframework.stereotype.Component;

@Component("ugandaemrpoc.UgandaEMRPOCConfig")
public class UgandaEMRPOCConfig {
	
	public static final String MODULE_ID = "ugandaemrpoc";
	
	public static final String MODULE_PRIVILEGE = "UgandaEMRPOC Privilege";

	public static final String TRIAGE_LOCATION_UUID = "ff01eaab-561e-40c6-bf24-539206b521ce";
	public static final String PHARMACY_LOCATION_UUID = "3ec8ff90-3ec1-408e-bf8c-22e4553d6e17";
	public static final String LAB_LOCATION_UUID = "ba158c33-dc43-4306-9a4a-b4075751d36c";

	public static final String DRUG_SET_CLASS = "Drug";
	public static final String LAB_SET_CLASS = "LabSet";
	public static final String TEST_SET_CLASS = "LabSet";

	public static final String ORDER_TYPE_DRUG_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7";
	public static final String ORDER_TYPE_LAB_UUID =  "52a447d3-a64a-11e3-9aeb-50e549534c5e";

	public static final int MEDICATION_ORDER_CONCEPT_ID = 1282;
	public static final int MEDICATION_STRENGTH_CONCEPT_ID = 1444;
	public static final int MEDICATION_QUANTITY_CONCEPT_ID = 160856;
	public static final int ARV_MEDICATION_QUANTITY_CONCEPT_ID = 99038;
	public static final int MEDICATION_QUANTITY_UNIT_CONCEPT_ID = 160855;
	public static final int MEDICATION_FORM_CONCEPT_ID = 1519;
	public static final int MEDICATION_START_DATE_CONCEPT_ID = 1190;
	public static final int MEDICATION_END_DATE_CONCEPT_ID = 1191;
	public static final int MEDICATION_INDICATOR_CONCEPT_ID = 160742;
	public static final int MEDICATION_DURATION_CONCEPT_ID = 159368;
	public static final int ARV_MEDICATION_DURATION_CONCEPT_ID = 99036;
	public static final int MEDICATION_DURATION_UNIT_CONCEPT_ID = 1732;
	public static final int CURRENTLY_TAKING_MEDICATION_CONCEPT_ID = 159367;
	public static final int MEDICATION_COMMENT_CONCEPT_ID = 160632;
	public static final int DEFALUT_ROUTE_CONCEPT_ID = 160240;
	public static final int DEFALUT_DOSE_UNIT_CONCEPT_ID = 160862;
	public static final int DEFALUT_DURATION_UNIT_CONCEPT_ID = 1072;

	public static final int MEDICATION_DISPENSE_SET = 163711;
	public static final int MEDICATION_DISPENSE_UNITS = 1443;
	public static final int MEDICATION_DISPENSE_RECEIVED_AT_VIST = 1276;

	public static final int CONCEPT_ID_NEXT_APPOINTMENT = 5096;
	public static final int CONCEPT_ID_TRANSFERED_OUT = 90306;

	public static final String  DEFALUT_ORDER_FREQUECNY_UUID = "efeaa2dc-e417-11e9-81b4-2a2ae2dbcce4";

	public static final String  DAY_START_TIME = "00:00:00";
	public static final String  DAY_END_TIME = "23:59:59";



	public static final String QUEUE_STATUS_PENDING = "pending";
	public static final String QUEUE_STATUS_COMPLETED = "completed";
	public static final String QUEUE_STATUS_ACTIVE = "active";
	public static final String QUEUE_STATUS_SENT_TO_LAB = "sent to lab";
	public static final String QUEUE_STATUS_FROM_LAB = "from lab";
	public static final String QUEUE_STATUS_SENT_TO_PHARMACY = "sent to pharmacy";
	public static final String QUEUE_STATUS_HAS_RESULTS = "has results";


	public static final String PROCESSED_ORDER_WITH_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where orders.accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";

	public static final String PROCESSED_ORDER_WITH_RESULT_OF_ENCOUNTER_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where orders.accession_number!=\"\" and specimen_source!=\"\" AND orders.encounter_id=%s";

	public static final String PROCESSED_ORDER_WITHOUT_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) where accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";

	public static final String ENCOUNTER_ROLE = "240b26f9-dd88-4172-823d-4a8bfeb7841f";
	public static final String ENCOUNTER_ROLE_PHARMACIST= "8da340f3-c690-439f-b21f-3a8367ff4057";

	public  static final String ENCOUNTER_TYPE_DISPENSE_UUID="22902411-19c1-4a02-b19a-bf1a9c24fd51";

	public static final String CARE_SETTING_OPD = "Outpatient";

	public UgandaEMRPOCConfig() {
	}
}

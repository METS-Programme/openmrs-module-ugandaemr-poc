package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.aijar.AijarConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.pharmacy.DispensingModelWrapper;
import org.openmrs.module.ugandaemrpoc.pharmacy.mapper.DrugOrderMapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.*;

public class PharmacyQueueListFragmentController {

    protected final Log log = LogFactory.getLog(PharmacyQueueListFragmentController.class);

    public PharmacyQueueListFragmentController() {
        // This Method is a Constructor
    }

    public void controller(@SpringBean FragmentModel pageModel, UiSessionContext uiSessionContext) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = sdf.format(new Date());
        List<String> list = new ArrayList();
        list.add(PHARMACY_LOCATION_UUID);
        pageModel.addAttribute("currentDate", dateStr);
        pageModel.addAttribute("locationSession", uiSessionContext.getSessionLocation().getUuid());
        pageModel.put("clinicianLocation", list);
        pageModel.put("currentProvider", Context.getAuthenticatedUser());
        pageModel.put("healthCenterName", Context.getAdministrationService().getGlobalProperty(AijarConstants.GP_HEALTH_CENTER_NAME));
    }


    public SimpleObject getPharmacyQueueList(@RequestParam(value = "pharmacySearchFilter", required = false) String searchfilter, UiSessionContext uiSessionContext) throws IOException, ParseException {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleObject simpleObject = new SimpleObject();
        List<PatientQueue> patientQueueList;
        if (!searchfilter.equals("")) {
            patientQueueList = patientQueueingService.getPatientQueueListBySearchParams(searchfilter, OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()), uiSessionContext.getSessionLocation(), null, null);
        } else {
            patientQueueList = patientQueueingService.getPatientQueueListBySearchParams(null, OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()), uiSessionContext.getSessionLocation(), null, null);
        }
        if (!patientQueueList.isEmpty()) {
            simpleObject.put("patientPharmacyQueueList", objectMapper.writeValueAsString(Context.getService(UgandaEMRPOCService.class).mapPatientQueueToMapperWithDrugOrders(patientQueueList)));
        } else {
            simpleObject.put("patientPharmacyQueueList", "");
        }

        return simpleObject;
    }

    public SimpleObject dispense(@BindParams("wrap") DispensingModelWrapper resultWrapper, UiSessionContext sessionContext) throws Exception {
        UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
        return ugandaEMRPOCService.dispenseMedication(resultWrapper, sessionContext.getCurrentProvider(), sessionContext.getSessionLocation());
    }


}

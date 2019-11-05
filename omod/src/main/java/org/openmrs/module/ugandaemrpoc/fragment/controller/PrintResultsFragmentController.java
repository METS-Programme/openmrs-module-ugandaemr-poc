package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.api.lab.util.TestResultModel;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Francis on 2/3/2016.
 */
public class PrintResultsFragmentController {

	public void controller(UiSessionContext sessionContext, FragmentModel model) {

		sessionContext.requireAuthentication();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = sdf.format(new Date());
		model.addAttribute("currentDate", dateStr);
	}

	/**
	 * Getting Results
	 *
	 * @param testId
	 * @param ui
	 * @return
	 */
	public SimpleObject getResults(@RequestParam(value = "testId") Integer testId, UiUtils ui) throws IOException {
		if (testId != null) {
			Order labTest = Context.getOrderService().getOrder(testId);
			UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);

			ObjectMapper objectMapper = new ObjectMapper();
			Set<TestResultModel> trms = ugandaEMRPOCService.renderTests(labTest);

			List<SimpleObject> results = SimpleObject.fromCollection(trms, ui, "investigation", "set", "test", "value",
			    "hiNormal", "lowNormal", "lowAbsolute", "hiAbsolute", "hiCritical", "lowCritical", "unit", "level",
			    "concept", "encounterId", "testId");

			SimpleObject currentResults = SimpleObject.create("data", objectMapper.writeValueAsString(results));
			return currentResults;
		}
		return null;
	}
}

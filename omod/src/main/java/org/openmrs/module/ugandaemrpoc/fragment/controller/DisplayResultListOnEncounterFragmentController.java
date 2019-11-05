package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.PROCESSED_ORDER_WITH_RESULT_OF_ENCOUNTER_QUERY;

public class DisplayResultListOnEncounterFragmentController {

    protected final Log log = LogFactory.getLog(getClass());

    public DisplayResultListOnEncounterFragmentController() {
    }

    public void controller(@SpringBean FragmentModel pageModel, @RequestParam(value = "encounterId", required = false) Encounter encounter, UiSessionContext uiSessionContext) {
        pageModel.put("encounterId", encounter.getEncounterId());

    }

    /**
     * Get Lab Orders without Results
     *
     * @param encounterId
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public SimpleObject getOrderWithResultForEncounter(@RequestParam(value = "encounterId", required = false) Integer encounterId) throws IOException, ParseException {

        Date date = new Date();
        return Context.getService(UgandaEMRPOCService.class).getOrderResultsOnEncounter(PROCESSED_ORDER_WITH_RESULT_OF_ENCOUNTER_QUERY, encounterId, true);
    }
}

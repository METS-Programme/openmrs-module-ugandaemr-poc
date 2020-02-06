package org.openmrs.module.ugandaemrpoc.page.controller.findpatient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrfingerprint.core.Commons;
import org.openmrs.module.ugandaemrfingerprint.remoteserver.FingerPrintHttpURLConnection;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static org.openmrs.module.ugandaemrfingerprint.core.FingerPrintConstant.*;

/**
 * Created by lubwamasamuel on 24/02/2017.
 */
public class FindPatientOnlineController {

    protected final Log log = LogFactory.getLog(this.getClass());

    public void controller(UiSessionContext sessionContext, PageModel model) {
    }

    public void get(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride) {

    }

    public void post(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, @RequestParam(value = "searchString", required = false) String searchString) {

        try {
            FingerPrintHttpURLConnection fingerPrintHttpURLConnection = new FingerPrintHttpURLConnection();

            if (fingerPrintHttpURLConnection.getCheckConnection(GP_CONNECTION_PROTOCOL) == CONNECTION_SUCCESS) {
                Commons commons = new Commons();
                Map map = fingerPrintHttpURLConnection.sendPostBy(SEARCH_URL,"");

                if (!map.isEmpty()) {
                    pageModel.put("patient", map.get(PATIENT_ONLINE_ID));
                } else {
                    pageModel.put("patient", PATIENT_NOT_FOUND);
                }
            } else {
                pageModel.put("connectionError", "No Internet Access");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }
}

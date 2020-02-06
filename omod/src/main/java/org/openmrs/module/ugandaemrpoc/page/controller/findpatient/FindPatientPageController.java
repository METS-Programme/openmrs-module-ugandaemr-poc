package org.openmrs.module.ugandaemrpoc.page.controller.findpatient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.helper.BreadcrumbHelper;
import org.openmrs.module.ugandaemrfingerprint.core.FingerPrintConstant;
import org.openmrs.module.ugandaemrfingerprint.remoteserver.FingerPrintGlobalProperties;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
public class FindPatientPageController {

    protected final Log log = LogFactory.getLog(org.openmrs.module.ugandaemrfingerprint.page.controller.findpatient.FindPatientPageController.class);

    /**
     * This page is built to be shared across multiple apps. To use it, you must pass an "app"
     * request parameter, which must be the id of an existing app that is an instance of
     * coreapps.template.findPatient
     *
     * @param model
     * @param app
     * @param sessionContext
     */
    public void get(PageModel model, @RequestParam("app") AppDescriptor app, UiSessionContext sessionContext, UiUtils ui) {
        FingerPrintGlobalProperties fingerPrintGlobalProperties = new FingerPrintGlobalProperties();
        model.addAttribute("afterSelectedUrl", app.getConfig().get("afterSelectedUrl").getTextValue());
        model.addAttribute("heading", app.getConfig().get("heading").getTextValue());
        model.addAttribute("label", app.getConfig().get("label").getTextValue());
        model.addAttribute("showLastViewedPatients", app.getConfig().get("showLastViewedPatients").getBooleanValue());


        if (app.getConfig().get("registrationAppLink") == null) {
            model.addAttribute("registrationAppLink", "");
        } else {
            model.addAttribute("registrationAppLink", app.getConfig().get("registrationAppLink").getTextValue());
        }
        model.put("fingerSocketPrintIpAddress", fingerPrintGlobalProperties.getGlobalProperty(FingerPrintConstant.GP_DEVICE_SOCKET_IP));
        BreadcrumbHelper.addBreadcrumbsIfDefinedInApp(app, model, ui);
    }
}

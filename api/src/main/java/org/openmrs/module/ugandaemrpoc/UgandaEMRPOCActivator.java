package org.openmrs.module.ugandaemrpoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.aijar.activator.HtmlFormsInitializer;
import org.openmrs.module.aijar.activator.Initializer;

import java.util.ArrayList;
import java.util.List;

public class UgandaEMRPOCActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(getClass());
	
	public UgandaEMRPOCActivator() {
	}
	
	public void started() {
		for (Initializer initializer : getInitializers()) {
			initializer.started();
		}
		log.info("Started UgandaEMRPOC");

	}
	
	public void shutdown() {
		log.info("Shutdown UgandaEMRPOC");
	}
	
	private List<Initializer> getInitializers() {
		List<Initializer> l = new ArrayList<Initializer>();
		l.add(new HtmlFormsInitializer(UgandaEMRPOCConfig.MODULE_ID));
		return l;
	}
}

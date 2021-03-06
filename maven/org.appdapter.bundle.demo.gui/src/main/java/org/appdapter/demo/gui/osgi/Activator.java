package org.appdapter.demo.gui.osgi;

import ext.osgi.common.ExtBundleActivatorBase;
import org.osgi.framework.BundleContext;

public class Activator extends ExtBundleActivatorBase {

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		super.handleFrameworkStartedEvent(bundleCtx);
		main(new String[0]);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[System.out] OK so far...");
		debugLoaders(org.appdapter.core.boot.ClassLoaderUtils.class);
		debugLoaders(org.appdapter.gui.demo.DemoBrowser.class);
		debugLoaders(org.appdapter.gui.browse.Utility.class);
		//   moved to o.a.bundle.fileconv Activator.
		//	ext.bundle.openconverters.osgi.Activator.ensureConvertersClassesAreFindable();
		org.appdapter.gui.demo.DemoBrowser.main(args);
		System.out.println("[System.out] ...addRepoLoaderMenu !");
		org.appdapter.gui.trigger.UtilityMenuOptions.setAllFeatures(true);
		System.out.println("[System.out] ...all done!");
	}
}

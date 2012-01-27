/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.osgi.registry;

import org.appdapter.api.registry.Registry;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.registry.basic.BasicRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RegistryServiceFuncs {
	
	static Logger theLogger = LoggerFactory.getLogger(RegistryServiceFuncs.class);
	
	public static Class	THE_WELL_KNOWN_REG_DEFAULT_INTF = VerySimpleRegistry.class;
	public static Class	THE_WELL_KNOWN_REG_DEFAULT_IMPL = BasicRegistry.class;
	
	// Used for testing when there is no OSGi framework available.
	// Should always be null in an OSGi environment.
	private static VerySimpleRegistry	theNonOsgiWKR;
	
	public static <RT extends Registry> ServiceRegistration registerTheWellKnownRegistry(BundleContext bundleCtx, 
					Class<RT> regClazz, RT wellKnownReg) {
		// OSGi 4.3  return bundleCtx.registerService(regClazz, wellKnownReg, null);
		// OSGi 4.2
		return bundleCtx.registerService(regClazz.getName(), wellKnownReg, null);
	}
	public static  ServiceRegistration registerTheWellKnownRegistry(BundleContext bundleCtx, 
					VerySimpleRegistry wellKnownReg) {
		return registerTheWellKnownRegistry(bundleCtx, VerySimpleRegistry.class, wellKnownReg);
	}		
	public static <RT extends Registry> RT lookupTheWellKnownRegistry(BundleContext bundleCtx, Class<RT> rtClazz) {
		ServiceReference ref = bundleCtx.getServiceReference(rtClazz.getName());
		if(ref == null){
			return null;
		}
		return (RT) bundleCtx.getService(ref);
	}
	public static VerySimpleRegistry lookupTheWellKnownRegistry(BundleContext bundleCtx) { 
		return lookupTheWellKnownRegistry(bundleCtx, VerySimpleRegistry.class);
	}
	
	public static VerySimpleRegistry getTheWellKnownRegistry(BundleContext bundleCtx) {
		if (bundleCtx == null) {
			theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Getting singleton WellKnownRegistry in non-OSGi context");
			if (theNonOsgiWKR == null) { 
				theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Making singleton WellKnownRegistry for non-OSGi context");
				theNonOsgiWKR = new BasicRegistry();
			}
			return theNonOsgiWKR;
		}
		// Find the existing registry, OR make it
		VerySimpleRegistry vsr = lookupTheWellKnownRegistry(bundleCtx);
		if (vsr == null) {
			theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Creating default WellKnownRegistry");
			VerySimpleRegistry theVsr = new BasicRegistry();
			ServiceRegistration sreg = registerTheWellKnownRegistry(bundleCtx, theVsr);
			theLogger.info("Got ServiceRegistration: " + sreg);
			vsr = lookupTheWellKnownRegistry(bundleCtx);
		}
		return vsr;
	}
	public static VerySimpleRegistry getTheWellKnownRegistry() {
		BundleContext localBundleCtx = null;
		Bundle localBundle = FrameworkUtil.getBundle(RegistryServiceFuncs.class);
		if (localBundle != null) { 
			localBundleCtx  = localBundle.getBundleContext();
		} else {
			theLogger.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Cannot get local bundle - we are outside OSGi!");
		}
		return getTheWellKnownRegistry(localBundleCtx);
	}

}

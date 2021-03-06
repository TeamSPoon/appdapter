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
 * Can work with or without OSGi.   To use (possibly) outside of OSGi,
 * use getTheWellKnownRegistry() (flexible, will try OSGi lookup first),
 * or, to be extra <i>something</i>, you could call getTheWellKnownStaticRegistry(),
 * but that will cause trouble if you really are in OSGi.
 * @author Stu B. <www.texpedient.com>
 */
public class RegistryServiceFuncs {

	private static Logger theLogger = LoggerFactory.getLogger(RegistryServiceFuncs.class);

	public static Class THE_WELL_KNOWN_REG_DEFAULT_INTF = VerySimpleRegistry.class;
	public static Class THE_WELL_KNOWN_REG_DEFAULT_IMPL = BasicRegistry.class;

	// Used for testing when there is no OSGi framework available.
	// Should always be null in an OSGi environment.
	private static VerySimpleRegistry theNonOsgiWKR;

	/**
	 *  Requires OSGi context.
	 * @param <RT>
	 * @param bundleCtx
	 * @param regClazz
	 * @param wellKnownReg
	 * @return
	 */
	public static <RT extends Registry> ServiceRegistration registerTheWellKnownRegistry(BundleContext bundleCtx, Class<RT> regClazz, RT wellKnownReg) {
		// OSGi 4.3  return bundleCtx.registerService(regClazz, wellKnownReg, null);
		// OSGi 4.2
		return bundleCtx.registerService(regClazz.getName(), wellKnownReg, null);
	}

	/**
	 *  Requires OSGi context.
	 * @param bundleCtx
	 * @param wellKnownReg
	 * @return
	 */
	public static ServiceRegistration registerTheWellKnownRegistry(BundleContext bundleCtx, VerySimpleRegistry wellKnownReg) {
		return registerTheWellKnownRegistry(bundleCtx, VerySimpleRegistry.class, wellKnownReg);
	}

	/**
	 *  Requires OSGi context.
	 *
	 * @param <RT>
	 * @param bundleCtx
	 * @param rtClazz
	 * @return
	 */
	public static <RT extends Registry> RT lookupTheWellKnownRegistry(BundleContext bundleCtx, Class<RT> rtClazz) {
		ServiceReference ref = bundleCtx.getServiceReference(rtClazz.getName());
		if (ref == null) {
			return null;
		}
		return (RT) bundleCtx.getService(ref);
	}

	/** Requires OSGi context.
	 *
	 */
	public static VerySimpleRegistry lookupTheWellKnownRegistry(BundleContext bundleCtx) {
		return lookupTheWellKnownRegistry(bundleCtx, VerySimpleRegistry.class);
	}

	/**
	 * Use this when you are sure you should be in OSGi, and willing to bring us a bona-fide non-null bundleCtx.
	 * This approach has value when an Activator.start() needs to get the registry (which should normally
	 * only be to register *itself* for others to find), without going through a credentialClaz (which
	 * should also work, if credClaz is from same bundle as the Activator).
	 * @param bundleCtx
	 * @return
	 */
	public static VerySimpleRegistry getTheWellKnownRegistryUsingReqContext(BundleContext bundleCtx) {
		// Find the existing registry, OR make it
		VerySimpleRegistry vsr = lookupTheWellKnownRegistry(bundleCtx);
		if (vsr == null) {
			theLogger.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Creating default WellKnownRegistry");
			VerySimpleRegistry theVsr = new BasicRegistry();
			ServiceRegistration sreg = registerTheWellKnownRegistry(bundleCtx, theVsr);
			theLogger.info("Got ServiceRegistration: " + sreg);
			vsr = lookupTheWellKnownRegistry(bundleCtx);
		}
		return vsr;
	}

	/**
	 *  Don't use in OSGi context, b/c you will probably wind up with two separate well known registries.
	 */
	public static VerySimpleRegistry getTheWellKnownStaticRegistry() {
		theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Getting singleton WellKnownRegistry in non-OSGi context");
		if (theNonOsgiWKR == null) {
			theLogger.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Making singleton WellKnownRegistry for non-OSGi context");
			theNonOsgiWKR = new BasicRegistry();
		}
		return theNonOsgiWKR;
	}

	/** Will use the bundleCtx, if it is not null.  Else will use static registry.
	 *
	 * @param bundleCtx
	 * @return
	 */
	public static VerySimpleRegistry getTheWellKnownRegistryUsingOptContext(BundleContext bundleCtx) {
		if (bundleCtx != null) {
			return getTheWellKnownRegistryUsingReqContext(bundleCtx);
		} else {
			return getTheWellKnownStaticRegistry();
		}
	}

	/**
	 * Attempts to find OSGi bundle context, using OSG Framework.getBundle(RegistryServiceFuncs.class).
	 * Uses it when successful, otherwise uses static version.
	 * @return
	 */
	public static VerySimpleRegistry getTheWellKnownRegistry(Class osgiCredentialClaz) {
		BundleContext localBundleCtx = null;
		Bundle localBundle = FrameworkUtil.getBundle(osgiCredentialClaz);
		if (localBundle != null) {

			/*  http://www.osgi.org/javadoc/r4v42/org/osgi/framework/Bundle.html#getBundleContext()
			 * If this bundle is not in the STARTING, ACTIVE, or STOPPING states or this bundle is a fragment bundle,
			 * then this bundle has no valid BundleContext. This method will return null if this bundle has no
			 * valid BundleContext.
			*/

			localBundleCtx = localBundle.getBundleContext();
			if (localBundleCtx != null) {
				theLogger.debug("Found legit bundleCtx {} associated to bundle {} via credClaz {} ", new Object[] { localBundleCtx, localBundle, osgiCredentialClaz });
			} else {
				theLogger.warn("Bundle getBundleContext() returned null - OSGi permissions or load-ordering problem for bundle {} in state {} via credClaz {}",
						new Object[] { localBundle, localBundle.getState(), osgiCredentialClaz });
				Exception e = new Exception("OSGi bundle permissions problem");
				e.fillInStackTrace();
				e.printStackTrace();
			}
		} else {
			theLogger.warn("%%%%%%%%%% Cannot get local bundle, so we are assumed to be outside OSGi (credentialClaz={})", osgiCredentialClaz);
		}
		return getTheWellKnownRegistryUsingOptContext(localBundleCtx);
	}

}

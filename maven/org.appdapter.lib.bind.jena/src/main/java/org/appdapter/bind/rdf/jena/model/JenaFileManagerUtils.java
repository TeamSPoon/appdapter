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

package org.appdapter.bind.rdf.jena.model;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.shared.Env;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.LocatorClassLoader;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class JenaFileManagerUtils {
	static Logger theLogger = LoggerFactory.getLogger(JenaFileManagerUtils.class);

	/**
	 * There are actually at least TWO static Jena FileManager instances of importance to us.
	 * 1) In FileManager itself, as returned by the FileManager.get() method, as of Jena 2.6.4.
	 * 2) In SDB "Env" class com.hp.hpl.jena.sdb.shared.Env, as returned by .fileManager()
	 * (and used by the SDBFactory) as of SDB 1.3.4.
	 * @param cl
	 */
	public static void ensureClassLoaderRegisteredWithJenaFM(FileManager fm, ClassLoader cl) {
		LocatorClassLoader candidateLCL = new LocatorClassLoader(cl);
		// First, ensure that cl is not already registered
		try {
			Iterator<Locator> locs = fm.locators();
			while (locs.hasNext()) {
				Locator l = locs.next();
				if (candidateLCL.equals(l)) {
					theLogger.info("Found existing equivalent Jena FM loader for: " + cl);
					return;
				}
			}
		} catch (UnsupportedOperationException uoe) {
			// new jena throws if there are no locators yet?
			uoe.printStackTrace();
		}
		theLogger.info("Registering new Jena FM loader for: " + cl);
		fm.addLocator(candidateLCL);
	}

	public static void ensureClassLoadersRegisteredWithJenaFM(FileManager fm, List<ClassLoader> clList) {
		if (clList != null) { 
			theLogger.info("Registering list of {} loaders", clList.size());
			for (ClassLoader cl : clList) {
				ensureClassLoaderRegisteredWithJenaFM(fm, cl);
			}
		} else {
			theLogger.warn("classLoader list is null, ignoring");
		}
	}

	public static FileManager getDefaultJenaFM() {
		FileManager fManagerE = Env.fileManager();
		FileManager fManager = FileManager.get();
		if (fManager != fManagerE) {
			theLogger.info("Mismatched Jena FMs: " + fManagerE + "!=" + fManager);
			//  2014-07-11 - this happens during turtle load, also during online sheet load?
			// (JenaFileManagerUtils.java:74) getDefaultJenaFM 
			// - Mismatched Jena FMs: com.hp.hpl.jena.util.FileManager@4ac34fd9
			// !=org.apache.jena.riot.adapters.AdapterFileManager@5418f143			
		}
		return fManagerE;
	}

	/** Ensures cl is registered with the FileManager returned by FileManager.get()
	 * (which is *not* used for SDB Store Config!  See SdbStoreFactory).
	 * @param cl
	 */
	public static void ensureClassLoaderRegisteredWithDefaultJenaFM(ClassLoader cl) {
		FileManager fm = getDefaultJenaFM();
		ensureClassLoaderRegisteredWithJenaFM(fm, cl);
	}

	public static void ensureClassLoadersRegisteredWithDefaultJenaFM(List<ClassLoader> clList) {
		FileManager fm = getDefaultJenaFM();
		ensureClassLoadersRegisteredWithJenaFM(fm, clList);
	}
}

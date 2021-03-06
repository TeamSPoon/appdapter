/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package org.appdapter.peru.binding.jena;


import org.appdapter.bind.rdf.jena.reason.JenaReasonerUtils;

import com.hp.hpl.jena.ontology.OntModelSpec;


import com.hp.hpl.jena.util.PrintUtil;

import org.appdapter.peru.core.config.Config;

import org.appdapter.peru.core.name.Address;

import org.appdapter.peru.core.vocabulary.SubstrateAddressConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Static convenience methods for rule-oriented processing.
 * <br/>
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */

public class ReasonerUtils  extends JenaReasonerUtils implements SubstrateAddressConstants {
	private static Logger 		theLogger = LoggerFactory.getLogger(ReasonerUtils.class );
	
	static {
		PrintUtil.registerPrefix("lut", "http://www.peruser.net/substrate#");
	}
	
	
	static public OntModelSpec lookupOntModelSpec (Config conf, Address configAddress) throws Throwable {
		OntModelSpec result = null;
		if (configAddress != null) {
			Address inferenceMarkerAddress = conf.getSingleAddress(configAddress, inferenceMarkerPropAddress);
			theLogger.debug("inferenceMarkerAddress is " + inferenceMarkerAddress);
			theLogger.debug("canonical jenaRdfsInferenceMarkerAddress  is " + jenaRdfsInferenceMarkerAddress);
			theLogger.debug("canonical noInferenceMarkerAddress  is " + noInferenceMarkerAddress);
			if (inferenceMarkerAddress.equals(jenaRdfsInferenceMarkerAddress)) {
				result = OntModelSpec.RDFS_MEM_RDFS_INF;
			} else if (inferenceMarkerAddress.equals(noInferenceMarkerAddress)) {
				result = null;
			} else {
				throw new Throwable ("Unknown inference type: " + inferenceMarkerAddress);
			}
		}
		return result;
	}			
}

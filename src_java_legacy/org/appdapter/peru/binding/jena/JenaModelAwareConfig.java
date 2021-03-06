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

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


import org.appdapter.peru.core.config.MutableConfig;
import org.appdapter.peru.core.config.AbstractConfig;

import org.appdapter.peru.core.environment.Environment;
import org.appdapter.peru.core.document.Doc;
import org.appdapter.peru.core.document.SentenceValue;

import org.appdapter.peru.binding.dom4j.Dom4jDoc;


import org.appdapter.peru.core.name.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JenaModelAwareConfig uses a Jena model to provide a set of queryable and 
 * updatable configuration information.
 * <br/>
 * Information in the model is contrued as "Frames" and "Slots".
 * <br/>
 * This class does not know what kind of Model it is using, or where it comes from.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class JenaModelAwareConfig extends AbstractConfig implements MutableConfig {
	private static Logger 		theLogger = LoggerFactory.getLogger(JenaModelAwareConfig.class);
	
	private		Model				myActiveJenaModel;
	private		JenaAbbreviator		myAbbreviator;
	
	protected synchronized void setActiveJenaModel(Model m) {
		myActiveJenaModel = m;
		myAbbreviator = new JenaAbbreviator(m);
	}
	protected synchronized Model getActiveJenaModel() {
		return myActiveJenaModel;
	}
	
	public MutableConfig makeMutableCloneConfig(Environment env) throws Throwable {
		Model actMod = getActiveJenaModel();
		return new JenaModelBackedConfig (actMod);
	}
	
	public final Resource resolveFrame (Address thingAddress) {
		Model actMod = getActiveJenaModel();
		// thingAddress.setPrefixMapping(actMod);
		return actMod.getResource(thingAddress.getResolvedPath());
	}
	public final Property resolveSlot (Address thingAddress) {
		Model actMod = getActiveJenaModel();
		// thingAddress.setPrefixMapping(actMod);	
		return actMod.getProperty(thingAddress.getResolvedPath());
	}
	public final Literal produceLiteral (String literalString) {
		Literal result = null;
		Model actMod = getActiveJenaModel();
		// The jena 2.5 method signature is "Literal createTypedLiteral(java.lang.Object value)" 
		result = actMod.createTypedLiteral(literalString);
		return result;
	}
	public final Literal produceLiteral (Doc literalDoc) {
		String xmlString = ((Dom4jDoc) literalDoc).getDom4jDoc().asXML();
		return produceLiteral (xmlString);
	}
	
	protected final void addSentence (Address thingAddress, Address fieldAddress, SentenceValue sv)
			throws Throwable {
		RDFNode sentenceObject = (RDFNode) sv.getCompatibleValue(this);
		addSentence (thingAddress, fieldAddress, sentenceObject);
	}
	protected final void addSentence (Address thingAddress, Address fieldAddress, RDFNode o)
			throws Throwable {
		Resource targetFrame = resolveFrame(thingAddress);
		Property targetSlot  = resolveSlot(fieldAddress);
		writeStatement(targetFrame, targetSlot, o);
	}
	protected final void writeStatement (Resource s, Property p, RDFNode o) 
			throws Throwable {
		Model actMod = getActiveJenaModel();
		Statement statement = actMod.createStatement(s, p, o);
		actMod.add(statement);
	}
	public final void addAddressValuedSentence (Address thingAddress, Address fieldAddress, 
			Address valueAddress) throws Throwable {
		addSentence (thingAddress, fieldAddress, resolveFrame(valueAddress));
	}
	public final void addStringValuedSentence (Address thingAddress, Address fieldAddress, 
			String valueString)  throws Throwable {
		addSentence (thingAddress, fieldAddress, produceLiteral(valueString));
	}
	public final void addDocValuedSentence (Address thingAddress, Address fieldAddress,
			Doc valueDoc)  throws Throwable {
		addSentence (thingAddress, fieldAddress, produceLiteral(valueDoc));
	}
/*
	private void addStatement (Address thingAddress, Address fieldAddress, RDFNode o) {
		Statement statement = myModel.createStatement(resolveFrame(thingAddress), resolveSlot(fieldAddress), o);
		myModel.add(statement);
	}
	public void addAddressValue (Address thingAddress, Address fieldAddress, Address valueAddress) {
		addStatement (thingAddress, fieldAddress, resolveFrame(valueAddress));
	}
	public void addStringValue (Address thingAddress, Address fieldAddress, String valueString) {
		addStatement (thingAddress, fieldAddress, myModel.createTypedLiteral(valueString));
	}
*/	
	public void clearValues (Address thingAddress, Address fieldAddress) throws Throwable {
		Model actMod = getActiveJenaModel();
		theLogger.debug("Clearing values for field " + fieldAddress + " in thing " + thingAddress);
		Resource frameResource = resolveFrame(thingAddress);
		Property slotProperty = resolveSlot(fieldAddress);
		theLogger.debug("Clearing values for slot " + slotProperty + " in frame " + frameResource);
		StmtIterator	matchIter = actMod.listStatements(frameResource, slotProperty, (RDFNode) null);
		List matchList = new ArrayList();
		while (matchIter.hasNext()) {
			Statement defstate = (Statement) matchIter.next();
			matchList.add(defstate);
		}
		theLogger.debug("Clearing default statements: " + matchList);
		actMod.remove(matchList);
	}
	public List getFieldValues (Address thingAddress, Address fieldAddress) throws Throwable {
		Model actMod = getActiveJenaModel();
		List result = new ArrayList();
		Resource frameResource = resolveFrame(thingAddress);
		Property slotProperty = resolveSlot(fieldAddress);		
		theLogger.debug("Fetching values for slot " + slotProperty + " in frame " + frameResource);		
		StmtIterator	matchIter = actMod.listStatements(frameResource, slotProperty, (RDFNode) null);
		while (matchIter.hasNext()) {
			Statement statement = (Statement) matchIter.next();
			RDFNode valNode = statement.getObject();
			theLogger.debug("FOUND: " + valNode);
			if (valNode instanceof Resource) {
				Address valAddress = new JenaAddress((Resource) valNode);
				result.add(valAddress);
			} else {
				Literal lit = (Literal) valNode.as(Literal.class);
				// Decision - convert this literal into a Doc or not?
				result.add(lit.getString());
			}
		}
		return result;
	}
	public List getBackpointerFieldValues (Address thingAddress, Address fieldAddress) throws Throwable {
		Model actMod = getActiveJenaModel();
		List result = new ArrayList();
		StmtIterator	matchIter = actMod.listStatements(null, resolveSlot(fieldAddress), resolveFrame(thingAddress));
		while (matchIter.hasNext()) {
			Statement statement = (Statement) matchIter.next();
			Address valAddress = new JenaAddress(statement.getSubject());
			result.add(valAddress);
		}
		return result;
	}
	
	public void applyOverrides (Doc d) throws Throwable {
		// CoreAbbreviator abb = CoreAbbreviator.makeCoreAbbreviator("WRONG", "NOPE", null, null);
		// Using "myAbbreviator" means that prefixes in the doc must be same as in our backing model.
		d.applyOverrides (this, myAbbreviator);
	}
	
	public void dumpConfigThing(Address thing) {
		try {
			theLogger.debug("===============================================================");
			theLogger.debug("JMAC dumping config thing:  " + thing);
			Model actMod = getActiveJenaModel();
			Resource frameResource = resolveFrame(thing);
			theLogger.debug("frameResource=" + frameResource);
			StmtIterator	matchIter = actMod.listStatements(frameResource, null, (RDFNode) null);
			while (matchIter.hasNext()) {
				Statement statement = (Statement) matchIter.next();
				theLogger.debug("statement=" + statement.toString());
			}
			//theLog.debug("---------------------------------------------------------------");
			//ModelUtils.dumpPrefixes(actMod);
			//theLog.debug("---------------------------------------------------------------");
			//theLog.debug(actMod);
			theLogger.debug("===============================================================");
		} catch (Throwable t) {
			theLogger.error("Caught exception in dumpConfigThing", t); 
		}
	}
	/*
	protected Object getSingleValue (Address thing, Address field, boolean searchBackwards, boolean optional) throws Throwable  {
		List valList = null;
		if (!searchBackwards) {
			valList = getFieldValues (thing, field);
		} else {
			valList = getBackpointerFieldValues (thing, field);
		}
		if (valList.size () == 1) {
			return valList.get(0);
		} else if (optional) {
			return null;
		} else {
			throw new Exception ("Expected to find 1 value at " + thing.getResolvedPath() + "." + 
					field.getResolvedPath() + " -  but found " + valList.size() + "values");
		} 	
	}
	*/
}


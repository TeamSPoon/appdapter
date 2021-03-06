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


package org.appdapter.peru.core.config;


import org.appdapter.peru.core.name.Address;

import org.appdapter.peru.core.document.Doc;

/**
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface MutableConfig extends Config {
	public void clearValues(Address thingAddress, Address fieldAddress) throws Throwable;
	
	public void addAddressValuedSentence(Address thingAddress, Address fieldAddress, Address valueAddress) throws Throwable;
	public void addStringValuedSentence(Address thingAddress, Address fieldAddress, String valueString) throws Throwable;
	public void addDocValuedSentence(Address thingAddress, Address fieldAddress, Doc valueDoc) throws Throwable;
	
	public void applyOverrides(Doc d) throws Throwable;
}


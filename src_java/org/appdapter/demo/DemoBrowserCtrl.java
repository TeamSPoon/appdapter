/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.demo;

/**
 * 
 * Projects that want to use Appdapter Demos and Tests need a set of interfaces that exist in Appdapter
 * That is what this package is for.. though notice there is not implementation.. but going to allow packages like 
 * org.appdapter.gui to register demos during the use of org.appdaptor.bundle loading
 * 
 * 
 * @author Administrator
 *
 */
import javax.swing.JFrame;

import org.appdapter.api.trigger.UserResult;

public interface DemoBrowserCtrl {

	void launchFrame(String string);

	JFrame getFrame();

	void initialize(String[] args);

	UserResult addObject(String title, Object anyObject, boolean showASAP, boolean expandChildren);

	UserResult addObject(String title, Object anyObject, boolean showASAP);

	void show();
}

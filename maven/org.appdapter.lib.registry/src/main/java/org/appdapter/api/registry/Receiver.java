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
package org.appdapter.api.registry;

/**
 *
 *  Receiver receives type-safe results found by a Finder.
 * 
 * @author Stu B. <www.texpedient.com>
 * 
 *
 */
public interface Receiver<OC>  {
	public enum Status {
		SEEKING,
		DONE
	}
	/**
	 * 
	 * @return SEEKING if more matches should be sought, else DONE.
	 * 
	 */
	public Status receiveMatch(OC match, ResultSequence<OC> seq, long seqIndex);
}

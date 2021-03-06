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
package org.appdapter.api.trigger;

import java.util.List;

import javax.swing.tree.TreeNode;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * TODO:  Parametrize in B <: Box
 */
public interface BoxContext {
	public Box getRootBox();

	public Box getParentBox(Box child);

	public List<Box> getOpenChildBoxes(Box parent);

	public <BTo extends Box<TT>, TT extends Trigger<BTo>> List<BTo> getOpenChildBoxesNarrowed(Box parent, Class<BTo> boxClass, Class<TT> trigClass);

	public void contextualizeAndAttachChildBox(Box<?> parentBox, MutableBox<?> childBox);

	public void contextualizeAndDetachChildBox(Box<?> parentBox, MutableBox<?> childBox);

	public TreeNode findNodeForBox(Box<?> parentBox, Box<?> childBox);
}

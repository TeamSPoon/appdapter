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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ScreenBoxTreeNode extends DefaultMutableTreeNode implements DisplayContext {
	private DisplayContext myDisplayContext;

	public ScreenBoxTreeNode() {
		super();
	}

	public ScreenBoxTreeNode(Box box) {
		super(box);
	}

	public ScreenBoxTreeNode(Box box, boolean allowsChildren) {
		super(box, allowsChildren);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#setDisplayContext(org.appdapter.gui.browse.DisplayContext)
	 */
	public void setDisplayContext(DisplayContext dc) {
		myDisplayContext = dc;
	}

	/*
	 * @see org.appdapter.gui.box.DisplayNode#getDisplayContext()
	 */

	public DisplayContext getDisplayContext() {
		return myDisplayContext;
	}

	public DisplayContext findDisplayContext() {
		DisplayContext foundDC = getDisplayContext();
		if (foundDC == null) {
			ScreenBoxTreeNode parentNode = (ScreenBoxTreeNode) getParent();
			if (parentNode != null) {
				foundDC = parentNode.getDisplayContext();
			}
		}
		return foundDC;
	}

	public Box getBox() {
		return (Box) getUserObject();
	}

	public ScreenBoxTreeNode findDescendantNodeForBox(Box b) {
		if (b == getUserObject()) {
			return this;
		} else {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				ScreenBoxTreeNode childNode = (ScreenBoxTreeNode) getChildAt(i);
				ScreenBoxTreeNode matchNode = childNode.findDescendantNodeForBox(b);
				if (matchNode != null) {
					return matchNode;
				}
			}
		}
		return null;
	}

	@Override public void add(MutableTreeNode childNode) {
		super.add((MutableTreeNode) childNode);

	}

	@Override public JTabbedPane getBoxPanelTabPane() {
		return myDisplayContext.getBoxPanelTabPane();
	}
}
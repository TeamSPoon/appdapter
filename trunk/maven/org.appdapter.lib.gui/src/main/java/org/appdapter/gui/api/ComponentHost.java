package org.appdapter.gui.api;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.impl.JJPanel;

public class ComponentHost extends JJPanel implements DisplayContext {

	//JLayeredPane desk;
	//JSplitPane split;
	public Component componet;
	public JComponent jcomponet;
	public Object objectValue;

	public void updateUI() {
		if (jcomponet != null) {
			jcomponet.updateUI();
		}
	}

	//OJOApp context;

	public ComponentHost(Component view, Object object) {
		super(false);
		this.componet = view;
		if (view instanceof JComponent) {
			jcomponet = (JComponent) view;
		}
		if (object instanceof JComponent) {
			jcomponet = (JComponent) object;
		}
		objectValue = object;
		//this.context = context;
		initGUI();
	}

	void initGUI() {
		removeAll();
		adjustSize();
		setLayout(new BorderLayout());
		add(componet);
		setName(componet.getName());
	}

	private void adjustSize() {
		Container p = getParent();
		if (p != null) {
			setSize(p.getSize());
		}
	}

	@Override public String getName() {
		return componet.getName();
	}

	@Override public void focusOnBox(Box b) {
		Debuggable.notImplemented();

	}

	@Override public Object getValue() {
		return Utility.dref(objectValue, componet);
	}

	public static JPanel asPanel(Component customizer, Object val) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult showError(String msg, Throwable error) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult showScreenBox(Object value) throws Exception {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult showMessage(String string) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public NamedObjectCollection getLocalBoxedChildren() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public Collection getTriggersFromUI(Object object) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult attachChildUI(String title, Object value) throws Exception {
		Debuggable.notImplemented();
		return null;
	}

	@Override public String getTitleOf(Object value) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public ITabUI getLocalCollectionUI() {
		Debuggable.notImplemented();
		return null;
	}

}

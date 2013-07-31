package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.util.PromiscuousClassUtilsA;

public class ClassChooserPanel extends JPanel implements ActionListener, DocumentListener, POJOCollectionListener {
	Class selectedClass = null;

	//JLayeredPane desk;
	//JSplitPane split;
	JButton classBrowserButton;
	JComboBox classField;
	DisplayContext context;
	HashSet<String> classesSaved = new HashSet<String>();
	Thread classGroveler;

	public ClassChooserPanel(DisplayContext context0) {
		super(true);
		this.context = context0;
		Utility.registerEditors();
		Utility.setBeanInfoSearchPath();

		initGUI();
		//startClassGroveler();
	}

	private void startClassGroveler() {
		if (classGroveler == null) {
			classGroveler = new Thread("Class groveler") {
				public void run() {
					while (true) {
						PromiscuousClassUtilsA.getImplementingClasses(PropertyEditor.class);
						PromiscuousClassUtilsA.getImplementingClasses(GetSetObject.class);
						resetAutoComplete();
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
		}
		classGroveler.start();
	}

	private void resetAutoComplete() {
		/*  JIDESOFT
		 
		 leaving it off as it requires large PermGen to hold all the class
		 
		   AutoCompletion autoCompletion;
		autoCompletion = new AutoCompletion(classField);
		autoCompletion.setStrict(false);
		autoCompletion.setStrictCompletion(false);
		for (Class c : PromiscuousClassUtils.getInstalledClasses()) {
			String n = c.getName();
			if (!classesSaved.contains(n))
				classField.addItem(n);
		}*/
	}

	@Override public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == classField || evt.getSource() == classBrowserButton) {
			openClassBrowser();
		}
	}

	private synchronized void openClassBrowser() {
		if (selectedClass != null) {
			try {
				Utility.browserPanel.showScreenBox(selectedClass);
			} catch (Throwable err) {
				Utility.showError(context, null, err);
			}
		}
	}

	void initGUI() {
		removeAll();

		adjustSize();

		classBrowserButton = new JButton("Examine...");
		classBrowserButton.setToolTipText("Opens a new window that lets you examine classes and create new object instances");
		classBrowserButton.addActionListener(this);
		classBrowserButton.setEnabled(false);
		//classBrowserButton.setActionCommand(COMMAND_CREATE_BEAN);

		classField = new JComboBox();
		classField.setSize(400, (int) classField.getSize().getHeight());
		classField.addActionListener(this);
		classField.setEnabled(true);
		final JTextComponent tc = (JTextComponent) classField.getEditor().getEditorComponent();
		tc.getDocument().addDocumentListener(this);

		setBorder(new TitledBorder("Class browser"));
		setLayout(new BorderLayout());
		add("North", new JLabel("Full class name:"));
		add("Center", classField);
		add("East", classBrowserButton);
	}

	private void adjustSize() {
		Container p = getParent();
		if (p != null) {
			setSize(p.getSize());
		}
	}

	private void classFieldChanged() {
		try {
			final JTextComponent tc = (JTextComponent) classField.getEditor().getEditorComponent();
			selectedClass = PromiscuousClassUtilsA.forName(tc.getText());
		} catch (Exception err) {
			selectedClass = null;
		}
		classBrowserButton.setEnabled(selectedClass != null || true);
	}

	@Override public void insertUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override public void removeUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override public void changedUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	public Object getValue() {
		return selectedClass;
	}

	@Override public void pojoAdded(Object obj, BT box) {
		if (obj instanceof Class) {
			Class clz = (Class) obj;
			if (!classesSaved.add(clz.getCanonicalName()))
				return;
			for (Class c : clz.getInterfaces()) {
				pojoAdded(c, null);
			}
			pojoAdded(clz.getSuperclass(), null);
		}
	}

	@Override public void pojoRemoved(Object obj, BT box) {
		// TODO Auto-generated method stub

	}

}

package org.appdapter.gui.swing;

import static org.appdapter.core.log.Debuggable.printStackTrace;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.ToFromKeyConverter;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.trigger.TriggerPopupMenu;

/**
 * A GUI widget that lets you choose among a set of objects
 * of a certain type.
 *
 */
public class ObjectChoiceComboPanel extends JJPanel implements POJOCollectionListener, MouseListener {

	PropertyEditorSupport editorSupport = new PropertyEditorSupport();

	final NamedObjectCollection context;
	PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	Class type;
	JComboBox combo;
	Model model;
	ToFromKeyConverter converter;

	public ObjectChoiceComboPanel(Class type, Object value) {
		this(null, type, value, Utility.getToFromStringConverter(type));
	}

	public ObjectChoiceComboPanel(NamedObjectCollection context0, Class type0, Object value, ToFromKeyConverter conv) {
		super(false);
		this.converter = conv;
		this.type = type0;
		if (context0 == null)
			context0 = Utility.getTreeBoxCollection();
		this.context = context0;
		if (type == null)
			type = Object.class;
		initGUI();
		if (context != null)
			context.addListener(this, true);
		combo.setSelectedItem(value);
	}

	@Override public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	private void checkTransient() {
		if (propSupport == null)
			propSupport = new PropertyChangeSupport(this);
	}

	@Override public void removePropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.removePropertyChangeListener(p);
	}

	public void setSelection(Object object) {
		model.setSelectedItem(object);
		String whatWasSelected = model.getSelectedItem();
		Object whatWasSelectedObj = model.getSelectedBean();

		if (whatWasSelectedObj != object) {
			Utility.bug("SetSelection broken  on this " + this);
		}
	}

	@Override public void pojoAdded(Object obj, BT box, Object senderCollection) {
		if (type.isInstance(obj))
			model.reload();
	}

	@Override public void pojoRemoved(Object obj, BT box, Object senderCollection) {
		if (type.isInstance(obj))
			model.reload();
	}

	private void initGUI() {
		model = new Model();
		combo = new JComboBox(model);
		combo.setEditable(false);
		setLayout(new BorderLayout());
		add("Center", combo);
		combo.addMouseListener(this);
	}

	@Override public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5, e);
		}
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5, e);
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5, e);
		}
	}

	@Override public void mouseEntered(MouseEvent e) {
		//@temp
		//label.setForeground(Color.blue);
	}

	@Override public void mouseExited(MouseEvent e) {
		//label.setForeground(Color.black);
	}

	public Object getSelection() {
		return model.getSelectedItem();
	}

	private void showMenu(int x, int y, MouseEvent e) {
		Object object = model.getSelectedBean();
		if (object != null) {
			TriggerPopupMenu menu = new TriggerPopupMenu(null, e, null);
			menu.addMenuFromObject(object);
			add(menu);
			menu.show(this, x, y);
		}
	}

	public String objectToString(Object object) {
		if (object == null)
			return "<null>";
		if (object instanceof String)
			return (String) object;
		try {
			if (converter != null) {
				Object key = converter.toKey(object);
				if (key instanceof String)
					return (String) key;

			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return Utility.getUniqueName(object, context);
	}

	public Object stringToObject(String title) {
		if (title.equals("<null>"))
			return null;
		Object obj = stringToObjectImpl(title);
		if (obj == null || (type != null && !type.isInstance(obj))) {
			obj = stringToObjectImpl(title);
			Utility.bug("stringToObjectImpl producing inccorect " + type + ": " + obj);
		}
		return obj;
	}

	public Object stringToObjectImpl(String title) {
		if (title == null || title.equals("<null>"))
			return null;
		if (type == String.class)
			return title;

		if (converter != null) {
			Object obj = converter.fromKey(title, type);
			if (obj != null) {
				return obj;
			}
		}
		Object obj = context.findObjectByName(title);
		if (obj != null)
			return obj;
		if (Utility.isToStringType(type)) {
			try {
				return Utility.fromString(title, type);
			} catch (NoSuchConversionException e) {
				printStackTrace(e);
			}
		}
		return null;
	}

	class Model extends AbstractListModel implements ComboBoxModel {
		//Vector listeners = new Vector();
		java.util.List<String> values;
		String selected = null;

		@SuppressWarnings("unchecked") public Model() {
			reload();
		}

		@Override public synchronized void setSelectedItem(Object anItem) {
			if (anItem instanceof String) {
				setSelectedName((String) anItem);
				return;
			}
			String title = objectToString(anItem);
			Object obj = stringToObject(title);
			if (obj != anItem) {
				Utility.bug("Not round tripping " + anItem);
			}
			setSelectedName(title);
		}

		public synchronized void setSelectedName(String anItem) {
			if (selected == null) {

			}
			String old = getSelectedItem();
			selected = anItem;

			//if (old != selected)
			//  notifyListeners();

			if (selected != null && !values.contains(selected)) {
				values.add(selected);
			}
			fireContentsChanged(this, -1, -1);
			Object o = stringToObject(old);
			Object n = stringToObject(selected);
			if (o != n) {
				propSupport.firePropertyChange("selection", o, n);
			}
		}

		@Override public String getSelectedItem() {
			if (selected == null)
				return "<null>";
			else
				return selected;
		}

		public Object getSelectedBean() {
			if (selected == null)
				return null;
			return stringToObject(selected);
		}

		@Override public int getSize() {
			return values.size();
		}

		@Override public String getElementAt(int index) {
			try {
				return values.get(index);
			} catch (Exception err) {
				return null;
			}
		}

		public synchronized void reload() {
			Object selected = getSelectedBean();
			if (context == null)
				values = new LinkedList();
			else {
				Collection col = context.findObjectsByType(type);
				values = new LinkedList();
				for (Object o : col) {

					values.add(context.getTitleOf(o));
				}
			}
			values.add("<null>");
			setSelectedItem(selected);
		}
	}

	public Object getValue() {
		return model.getSelectedBean();
	}

}
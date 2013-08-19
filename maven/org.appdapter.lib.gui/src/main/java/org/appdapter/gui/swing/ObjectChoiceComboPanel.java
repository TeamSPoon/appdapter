package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditorSupport;
import java.util.*;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.SearchableDemo;
import org.appdapter.gui.browse.ToFromKeyConverter;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.ObjectChoiceComboPanel.ObjectChoiceModel;
import org.appdapter.gui.swing.ObjectChoiceComboPanel.ObjectComboPrettyRender;
import org.appdapter.gui.trigger.TriggerPopupMenu;

/**
 * A GUI widget that lets you choose among a set of objects
 * of a certain type.
 *
 */
public class ObjectChoiceComboPanel extends JJPanel implements POJOCollectionListener, MouseListener {

	public static final Object NULLOBJECT = "<null>";

	PropertyEditorSupport editorSupport = new PropertyEditorSupport();

	final NamedObjectCollection context;
	PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	Class type;
	JComboBox combo;
	ObjectChoiceModel model;
	ToFromKeyConverter converter;

	public boolean useStringProxies;
	public boolean useSmallObjectViewInLists;
	public boolean isStringChooser;

	Object selectedObject = null;

	public ObjectChoiceComboPanel(Class type, Object value) {
		this(null, type, value, Utility.getToFromStringConverter(type));
	}

	public ObjectChoiceComboPanel(NamedObjectCollection context0, Class type0, Object value, ToFromKeyConverter conv) {
		super(false);
		this.converter = conv;
		selectedObject = value;
		this.type = type0;
		if (context0 == null)
			context0 = Utility.getTreeBoxCollection();
		this.context = context0;
		if (type == null) {
			Utility.bug("type of value unknown: " + value);
			type = Object.class;
			useStringProxies = false;
		} else {
			this.isStringChooser = type == String.class;
			useStringProxies = Utility.isToStringType(type) && !isStringChooser;
		}
		useSmallObjectViewInLists = !useStringProxies && !isStringChooser;
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
		model = new ObjectChoiceModel();
		combo = new JComboBox(model);
		//combo.setEditable(false);
		SearchableDemo.installSearchable(combo);
		combo.setRenderer(new ObjectComboPrettyRender());
		setLayout(new BorderLayout());
		add("Center", combo);
		combo.addMouseListener(this);
		model.reload();
		combo.setEditable(isStringChooser || useStringProxies);
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
		if (object == null || object == NULLOBJECT)
			return NULLOBJECT.toString();
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
		if (title == null || title.equals(NULLOBJECT.toString()))
			return null;
		Object obj = stringToObjectImpl(title);
		if (obj == null || (type != null && !type.isInstance(obj))) {
			obj = stringToObjectImpl(title);
			Utility.bug("stringToObjectImpl producing inccorect " + type + ": " + obj);
		}
		return obj;
	}

	public Object stringToObjectImpl(String title) {
		if (title == null || title.equals(NULLOBJECT.toString()))
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
				Debuggable.printStackTrace(e);
			}
		}
		Utility.bug("cant find " + type + " object for: " + title);
		return null;
	}

	Map<String, Object> s2o = new HashMap();
	Map<Object, String> o2s = new HashMap();

	class ObjectComboPrettyRender extends JLabel implements ListCellRenderer {

		public ObjectComboPrettyRender() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		/*
		* This method finds the image and text corresponding
		* to the selected value and returns the label, set up
		* to display the text and image.
		*/
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JComponent view = this;
			if (value != null && value == NULLOBJECT) {
				value = null;
			}
			if (value != null && useSmallObjectViewInLists) {
				String title = null;

				view = new SmallObjectView(null, context, value, title, true, true, true, true) {
					@Override public boolean isRemovable(Object value) {
						return false;
					}
				};
			} else {
				//Set the icon and text.  If icon was null, say so.
				ImageIcon icon = null;// images[selectedIndex];
				String title = Utility.getUniqueNamePretty(value);
				if (icon != null) {
					setIcon(icon);
				} else {
					//setUhOhText(pet + " (no image available)", list.getFont());
				}
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				setHorizontalAlignment(SwingConstants.LEFT);
				setText(title);
				setFont(list.getFont());
			}

			if (isSelected) {
				view.setBackground(list.getSelectionBackground());
				view.setForeground(list.getSelectionForeground());
			} else {
				view.setBackground(list.getBackground());
				view.setForeground(list.getForeground());
			}
			return view;
		}

	}

	class ObjectChoiceModel extends AbstractListModel implements ComboBoxModel {
		//Vector listeners = new Vector();
		java.util.List<Object> objectValues;

		@SuppressWarnings("unchecked") public ObjectChoiceModel() {
		}

		@Override public synchronized void setSelectedItem(Object anItem) {
			if (anItem instanceof String) {
				if (useStringProxies) {
					anItem = stringToObject((String) anItem);
				}
			}
			if (!Debuggable.isRelease()) {
				String title = objectToString(anItem);
				Object obj = stringToObject(title);
				if (obj != anItem) {
					Utility.bug("Not round tripping " + anItem);
				}
			}
			if (selectedObject != anItem) {
				Object oldValue = selectedObject;
				selectedObject = anItem;
				combo.setToolTipText(Utility.makeTooltipText(selectedObject));
				propSupport.firePropertyChange("selection", oldValue, anItem);
			}
		}

		@Override public Object getSelectedItem() {
			return selectedObject;
		}

		public Object getSelectedBean() {
			return selectedObject;
		}

		@Override public int getSize() {
			if (objectValues == null)
				return 0;
			return objectValues.size();
		}

		@Override public Object getElementAt(int index) {
			try {
				return objectValues.get(index);
			} catch (Exception err) {
				return null;
			}
		}

		public synchronized void reload() {
			Object selected = getSelectedBean();
			if (context == null)
				objectValues = new LinkedList();
			else {
				Class ft = type;
				Collection col = context.findObjectsByType(ft);
				if (col.size() == 0) {
					Utility.theLogger.warn("col.size() == 0 for " + ft);
				}
				objectValues = new LinkedList();
				for (Object o : col) {

					objectValues.add(o);
				}
			}
			objectValues.add(NULLOBJECT);
			setSelectedItem(selected);
		}
	}

	public Object getValue() {
		if (model == null)
			return model;
		return model.getSelectedBean();
	}

}
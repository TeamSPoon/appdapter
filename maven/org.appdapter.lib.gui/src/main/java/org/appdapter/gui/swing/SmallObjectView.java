package org.appdapter.gui.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.ValueChangeListener;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.ObjectPanelHost;
import org.appdapter.gui.trigger.TriggerPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel containing a very minimal GUI for an object
 * <p>
 *
 *
 */
public class SmallObjectView<BoxType extends Box>

extends ObjectView<Box>

implements PropertyChangeListener, MouseListener, ActionListener, DragGestureListener, Transferable, DragSourceListener, DropTargetListener, ObjectPanelHost {

	@Override public String toString() {
		String ttt = getToolTipText();
		if (ttt == null || ttt.length() == 0) {
			if (objectValue == null)
				return "<null>";
			ttt = Utility.makeTooltipText(objectValue);
		}
		return ttt;
	}

	class PropertyButton extends JButton {
		public PropertyButton() {
			super();
			try {
				setIcon(Utility.getImageIcon(IconView.class.getResource("PropertyButton.gif")));
			} catch (Throwable err) {
				setText("...");
			}
			//setFont(new Font("Serif", Font.PLAIN, 8));
			setToolTipText("Open a property window for this object");
		}

		@Override public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override public Dimension getPreferredSize() {
			return new Dimension(16, 16);
		}
	}

	class RemoveButton extends JButton {
		public RemoveButton() {
			super();
			try {
				setIcon(Utility.getImageIcon(IconView.class.getResource("RemoveButton.gif")));
			} catch (Throwable err) {
				setText("x");
				setForeground(Color.red);
			}
			//setFont(new Font("Serif", Font.PLAIN, 8));
			setToolTipText("Removes this object from its parent collection");
		}

		@Override public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override public Dimension getPreferredSize() {
			return new Dimension(16, 16);
		}
	}

	private final static Color normalColor = Color.black;
	private final static Color selectedColor = Color.blue;
	private static Logger theLogger = LoggerFactory.getLogger(SmallObjectView.class);
	DisplayContext context;
	DragSource dragSource;

	//Invisible panel in front that captures menu events and drag/drop events
	JPanel frontGlass;

	//public Object objectValue;

	IconView iconView;

	JLabel label;

	JButton propButton;
	JButton removeButton;

	ValueChangeListener objectCollectionRemoveListener;
	boolean showIcon;
	boolean showLabel;
	boolean showPropButton;
	boolean showRemoveButton;
	boolean showToString;
	protected NamedObjectCollection nameMaker;
	protected boolean isRemoved;
	protected String title;

	/**
	 * @param parent if a parent is provided, a "remove" button will be added allowing you to remove this object from the given collection
	 */
	public SmallObjectView(DisplayContext context, NamedObjectCollection namer, Object object, String title, boolean showLabel, boolean showIcon, boolean showPropButton, boolean showToString) {
		super(false);
		showIcon = false;
		objectValue = object;
		isRemoved = false;
		if (context == null)
			context = Utility.getCurrentContext();
		this.context = context;
		this.showLabel = showLabel;
		this.showIcon = showIcon;
		this.showPropButton = showPropButton;
		this.showToString = showToString;
		initGUI();
		this.addMouseListener(this);
		checkColor();

		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
	}

	public SmallObjectView(DisplayContext context, NamedObjectCollection namer, Object object) {
		this(context, namer, object, null, true, true, true, true);
	}

	@Override public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == propButton) {
			actionShowProperties();
		} else if (evt.getSource() == removeButton) {
			actionRemove();
		} else {

		}

	}

	public void actionRemove() {
		Object v = getValue();
		valueChanged(this, v, null);
		isRemoved = true;
		objectValue = null;
		Container p = getParent();
		if (p != null)
			p.remove(this);
	}

	//==== Drag/drop methods ==========================

	public void actionShowProperties() {
		if (context != null) {
			try {
				Utility.browserPanel.showScreenBox(getValue());
			} catch (Throwable err) {
				Utility.browserPanel.showError("An error occurred while creating an interface for " + getValue(), err);
			}
		}
	}

	private void checkColor() {
		//if (getPOJO().isSelected()) {
		//  label.setForeground(selectedColor);
		//} else {
		if (label != null) {
			label.setForeground(normalColor);
		}
		//}
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override public void dragEnter(DropTargetDragEvent dtde) {
		todo();

	}

	public void dragExit(DragSourceEvent dse) {
	}

	@Override public void dragExit(DropTargetEvent dte) {
		todo();

	}

	//==== Drag/drop methods ==========================

	public void dragGestureRecognized(DragGestureEvent event) {
		theLogger.debug("source dragGestureRecognized");
		dragSource.startDrag(event, DragSource.DefaultMoveDrop, this, this);
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override public void dragOver(DropTargetDragEvent dtde) {
		todo();

	}

	public void drop(DropTargetDropEvent event) {
		Transferable t = event.getTransferable();
		try {
			Object o = t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
			setObject(o);
		} catch (Exception err) {
			new ErrorDialog("An error occurred while handling a drop operation", err).show();
		}

	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	@Override public void dropActionChanged(DropTargetDragEvent dtde) {
		todo();

	}

	@Override public void focusOnBox(Box b) {
		setObject(b);
	}

	@Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		return getValue();
	}

	@Override public DataFlavor[] getTransferDataFlavors() {
		try {
			return new DataFlavor[] { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType) };
		} catch (ClassNotFoundException err) {
			theLogger.error("An error occurred", err);
			return new DataFlavor[0];
		}
	}

	@Override public Object getValue() {
		return objectValue;
	}

	public boolean initGUI() {

		if (nameMaker == null)
			nameMaker = Utility.getTreeBoxCollection();
		setLayout(new OverlayLayout(this));

		JPanel panel = new JPanel();
		frontGlass = new JPanel();
		frontGlass.setOpaque(false);

		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		Object object = getValue();
		if (object != null) {
			Class objectClass = object.getClass();
			if (showIcon) {
				try {
					Icon image = Utility.getIcon(objectClass);
					if (image != null) {
						JLabel label = new JLabel(image);
						panel.add(label);
					}
				} catch (Throwable err) {
				}
				//iconView = new IconView(object.getIcon());
				//add(iconView);
			}
			if (showLabel) {
				if (this.title == null) {
					title = Utility.getUniqueName(object, nameMaker, false, false);
				}
				String labelname = title;
				if ((title == null || title.equals("<null>")) && object != null && !(object instanceof String)) {
					Utility.bug("title for" + object);
				} else {
					if (labelname.length() > 128) {
						labelname = labelname.substring(0, 128);
					}
				}
				label = new JLabel(labelname);
				panel.add(label);
			}
			if (showPropButton && !objectClass.isPrimitive()) {
				propButton = new PropertyButton();
				panel.add(propButton);
				propButton.addActionListener(this);
			}
			if (isRemovable(object)) {
				removeButton = new RemoveButton();
				panel.add(removeButton);
				removeButton.addActionListener(this);
			}
			String tooltip = Utility.makeTooltipText(object);
			setToolTipText(tooltip);
			if (showToString) {
				if (tooltip.length() > 128) {
					tooltip = tooltip.substring(0, 128);
				}
				panel.add(new JLabel(tooltip));
			}
		} else {
			panel.add(new JLabel("null"));
		}
		add(frontGlass);
		add(panel);
		return true;
	}

	private void todo(Object... params) {
		Debuggable.notImplemented(params);

	}

	public boolean isRemovable(Object value) {
		return false && !isRemoved;
	}

	public void valueChanged(Object sender, Object oldObject, Object newObject) {
		if (sender != this) {
			reallySetValue(newObject);
		} else {
			if (objectCollectionRemoveListener != null) {
				objectCollectionRemoveListener.valueChanged(sender, oldObject, newObject);
			}
		}
	}

	//=================================================================

	@Override public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getMimeType().equals(DataFlavor.javaJVMLocalObjectMimeType);
	}

	@Override public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e);
		} else {
			/* try {
			   collection.setSelected(getPOJO());
			 } catch (Exception err) {}*/
		}
	}

	@Override public void mouseEntered(MouseEvent e) {
		Object gv = getValue();
		gv = Utility.makeTooltipText(gv);
		setToolTipText("" + gv);
		label.setForeground(selectedColor);
	}

	@Override public void mouseExited(MouseEvent e) {
		label.setForeground(normalColor);
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e);
		} else {
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e);
		}
		//setFont(new Font("Serif", Font.PLAIN, 8));
		//setToolTipText("Removes this object from its parent collection");
	}

	//=================================================================

	@Override public void objectValueChanged(Object oldBean, Object newBean) {
		reallySetValue(newBean);
	}

	@Override public void propertyChange(PropertyChangeEvent evt) {
		if (label != null) {
			label.setText(Utility.getUniqueName(getValue(), nameMaker));
		}
		checkColor();
	}

	@Override protected void reallySetValue(Object bean) {
		if (objectValue == bean)
			return;
		removeAll();
		objectValue = bean;
		initGUI();
	}

	public void addChangeListener(ValueChangeListener l) {
		this.objectCollectionRemoveListener = l;
	}

	private void showMenu(MouseEvent e) {
		showMenu(e.getX() + 5, e.getY() + 5, e);
	}

	private void showMenu(int x, int y, MouseEvent e) {
		Object object = getValue();
		if (object == null) {
			object = this;
		}
		if (true || !object.getClass().isPrimitive()) {
			TriggerPopupMenu menu = new TriggerPopupMenu(context, e, nameMaker);
			menu.addMenuFromObject(object);
			frontGlass.add(menu);
			menu.show(frontGlass, x, y);
		}
	}

}

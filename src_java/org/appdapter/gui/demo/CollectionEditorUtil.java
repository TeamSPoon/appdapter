package org.appdapter.gui.demo;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.appdapter.gui.box.AppGUIWithTabsAndTrees;
import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.BoxPanelSwitchableViewImpl;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.POJOCollectionImpl;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.POJOAppContext;
import org.appdapter.gui.swing.TriggersMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The top-level Test for the POJOCollection code. It also contains the
 * main(...) method.
 * 
 * 
 */
@SuppressWarnings("serial")
public class CollectionEditorUtil implements PropertyChangeListener {

	// ==== Static variables ===========
	//static ObjectNavigator defaultFrame = null;
	static Logger theLogger = LoggerFactory.getLogger(CollectionEditorUtil.class);
	private static DemoNavigatorCtrl dnc;
	private static boolean dncSetup;

	// ==== Instance variables ==========
	NamedObjectCollection collection;
	POJOAppContext context;

	// The currently opened ObjectNavigator file (may be null)
	File file = null;

	// ==== GUI elements ===================
	//static JMenuBar menuBar = Utility.getMenuBar();
	FileMenu fileMenu;
	//POJOCollectionViewPanelWithContext panel;
	//JToolBar toolbar;
	// JButton aboutButton;
	TriggersMenu selectedMenu;

	// ==== Actions =============================
	Action saveAction = new SaveAction();
	Action openAction = new OpenAction();
	Action saveAsAction = new SaveAsAction();
	Action newAction = new NewAction();
	Action searchAction = new SearchAction();

	// ======== Constructors =============================0

	/**
	 * Creates a new ObjectNavigator that shows the given collection
	 */
	public CollectionEditorUtil(POJOAppContext context, NamedObjectCollection collection) {
		this.nameItemChooserPanel = Utility.namedItemChooserPanel = new NamedItemChooserPanel(context);
		this.context = context;
		setNamedObjectCollection(collection);
	}

	private JFrame getFrame() {
		return Utility.getAppFrame();
	}

	// ====== Property getters ==============

	public NamedItemChooserPanel getNamedItemChooserPanel() {
		return Utility.namedItemChooserPanel;
	}

	public static BoxPanelSwitchableView getDefaultFrame() {
		return Utility.boxPanelDisplayContext;
	}

	public BoxPanelSwitchableView getChildCollectionWithContext() {
		return context.getNamedObjectCollection();
	}

	/**
	 * The current ObjectNavigator being displayed
	 */
	public NamedObjectCollection getCollectionWithSwizzler() {
		return collection;
	}

	final NamedItemChooserPanel nameItemChooserPanel;

	/**
	 * Sets the collection to be displayed
	 */
	private void setNamedObjectCollection(NamedObjectCollection newCollection) {
		NamedObjectCollection oldCollection = collection;
		if (newCollection != oldCollection) {
			this.collection = newCollection;
			//panel = Utility.selectionOfCollectionPanel;
			nameItemChooserPanel.invalidate();
			nameItemChooserPanel.validate();
			if (oldCollection != null)
				oldCollection.removePropertyChangeListener(this);
			if (newCollection != null)
				newCollection.addPropertyChangeListener(this);
			updateSelectedMenu();
		}
	}

	// ==== Property notification methods ===============

	@Override public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == collection) {
			if (evt.getPropertyName().equals("selected")) {
				updateSelectedMenu();
			}
		}
	}

	protected void processEvent(AWTEvent e) {
		if (e.getID() == Event.WINDOW_DESTROY) {
			theLogger.info("Shutting down ObjectNavigator...");
			try {
				Settings.saveToFile();
			} catch (Exception err) {
				theLogger.warn("Warning - failed to save settings: " + err.getMessage(), err);
			}
			nameItemChooserPanel.removeAll();
			getFrame().dispose();
			theLogger.info("ObjectNavigator is now shut down!");
		}
		if (nameItemChooserPanel != (Object) this)
			;//panel.handleEvent(e);
	}

	// ==== Action execution methods =======================
	private void makeRepoNav() {
		try {
			if (dnc != null) {
				if (!dncSetup) {
					dncSetup = true;
					dnc.launchFrame("Appdapter Demo Browser");
				}
				// dnc.getFrame().setVisible(true);
				dnc.getComponent().show();
				return;
			}
			Class.forName("org.cogchar.gui.demo.RepoNavigator").getMethod("main", new Class[] { String.class }).invoke(new String[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void openCollection() {
		FileDialog dialog = new FileDialog(getFrame(), "Load ObjectNavigator", FileDialog.LOAD);
		dialog.show();
		String fileName = dialog.getFile();
		String directory = dialog.getDirectory();
		if (fileName != null) {
			openCollection(new File(directory, fileName));
		}
	}

	void openCollection(File file) {
		if (file.exists()) {
			try {
				setNamedObjectCollection(POJOCollectionImpl.load(file));
				Settings.addRecentFile(file);
				fileMenu.refreshRecentFileList();
			} catch (Exception err) {
				context.showError("Opening failed", err);
			}
		} else {
			context.showError("File does not exist: " + file.getPath(), null);
		}
	}

	void newCollection() {
		// @feature ask save changes?
		setNamedObjectCollection(new BoxPanelSwitchableViewImpl());
		file = null;
		checkControls();
	}

	void saveCollection() {
		if (file == null) {
			saveCollectionAs();
		} else {
			saveCollection(file);
		}
	}

	void saveCollectionAs() {
		FileDialog dialog = new FileDialog(getFrame(), "Save ObjectNavigator", FileDialog.SAVE);
		dialog.setFile("mycollection.ser");
		dialog.show();
		String fileName = dialog.getFile();
		String directory = dialog.getDirectory();
		theLogger.debug("fileName = " + fileName);
		theLogger.debug("directory = " + directory);
		if (fileName != null) {
			saveCollection(new File(directory, fileName));
		}
	}

	void saveCollection(File file) {
		theLogger.debug("saveCollection(" + file.getAbsoluteFile() + ")");
		// if (file.exists()) {
		this.file = file;
		try {
			collection.save(file);
		} catch (NotSerializableException err) {
			context.showError("This collection contains an unserializable object", err);
		} catch (Exception err) {
			context.showError("Saving failed", err);
		}
		checkControls();
		// } else {
		// showError("File does not exist: " + file.getPath());
		// }
	}

	// ==== Private methods ===================

	private void updateSelectedMenu() {
		JMenuBar menuBar = Utility.getMenuBar();
		if (selectedMenu != null) {
			menuBar.remove(selectedMenu);
			selectedMenu = null;
		}

		Object selected = collection.getSelectedComponent();
		if (selected != null) {
			selectedMenu = new TriggersMenu("With Selected", selected);
			menuBar.add(selectedMenu);
		}
		nameItemChooserPanel.invalidate();
		nameItemChooserPanel.validate();
		nameItemChooserPanel.repaint();
	}

	void checkControls() {
		saveAction.setEnabled(file != null);
	}

	/**
	 * Creates and initialized the GUI components within the ObjectNavigator.
	 * Should only be called once.
	 */
	public JMenu getMenu() {

		//panel.setLayout(new BorderLayout());
		//Utility.selectionOfCollectionPanel = new LargeClassChooser(context);
		if (fileMenu == null) {
			Utility.fileMenu = fileMenu = new FileMenu();
		}
		checkControls();
		//menuBar.add(fileMenu);
		return fileMenu;

		//toolbar = new MyToolBar();
		//toolbar.setFloatable(true);

		// JPanel northPanel = new JPanel();
		// northPanel.setLayout(new BorderLayout());
		// northPanel.add("Center", toolbar);

		// aboutButton = new ActionButton(aboutAction);
		// northPanel.add("East", aboutButton);
		//panel.add(Utility.selectionOfCollectionPanel);
		//getContentPane().add("Center", Utility.selectionOfCollectionPanel);
		//getContentPane().add("North", toolbar);
	}

	// ==== Action classes =================================

	private Container getContentPane() {
		return nameItemChooserPanel;
	}

	class SaveAction extends AbstractAction {
		SaveAction() {
			super("Save", Icons.saveCollection);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			saveCollection();
		}
	}

	class SaveAsAction extends AbstractAction {
		SaveAsAction() {
			super("Save as...", Icons.saveCollectionAs);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			saveCollectionAs();
		}
	}

	class NewAction extends AbstractAction {
		NewAction() {
			super("New", Icons.newCollection);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			newCollection();
		}
	}

	class OpenAction extends AbstractAction {
		OpenAction() {
			super("Open", Icons.openCollection);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			openCollection();
		}
	}

	class SearchAction extends AbstractAction {
		SearchAction() {
			super("Search...", Icons.search);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			makeRepoNav();
		}

	}

	class RecentFileAction extends AbstractAction {
		File file;

		RecentFileAction(File file) {
			super(file.getName(), Icons.recentFile);
			this.file = file;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			openCollection(file);
		}
	}

	// ==== GUI component inner classes ===========

	class FileMenu extends JMenu {
		Vector recentFiles = new Vector();

		FileMenu() {
			super("File");
			addItems();
		}

		private void addItems() {
			add(newAction);
			add(openAction);
			addSeparator();
			add(saveAction);
			add(saveAsAction);
			addSeparator();

			recentFiles = new Vector();
			Iterator it = Settings.getRecentFiles();
			while (it.hasNext()) {
				File file = (File) it.next();
				Action a = new RecentFileAction(file);
				recentFiles.addElement(a);
				add(a);
			}
		}

		public void refreshRecentFileList() {
			removeAll();
			addItems();
		}
	}

	class MyToolBar extends JToolBar {
		MyToolBar() {
			super();
			add(newAction);
			add(openAction);
			addSeparator();
			add(saveAction);
			add(saveAsAction);
			addSeparator();
			add(searchAction);
		}
	}

	/**
	 * Contains settings for the ObjectNavigator application
	 * 
	 * 
	 */
	static public class Settings implements java.io.Serializable {
		private final static String FILENAME = Settings.class.getName() + ".ser";
		private final static int RECENT_FILE_COUNT = 5;
		transient static Settings settings;

		public List recentFiles;

		public Settings() {
			recentFiles = new LinkedList();
		}

		public static void addRecentFile(File file) {
			if (file != null && !settings.recentFiles.contains(file)) {
				settings.recentFiles.add(0, file);
			}
			while (settings.recentFiles.size() > 5) {
				settings.recentFiles.remove(4);
			}
		}

		public static Iterator getRecentFiles() {
			return settings.recentFiles.iterator();
		}

		static {
			if (getFile().exists()) {
				try {
					loadFromFile();
				} catch (Exception err) {
					System.err.println("Warning - settings could not be loaded: " + err.getMessage());
					settings = new Settings();
				}
			} else {
				settings = new Settings();
			}
		}

		@Override protected void finalize() {
			try {
				saveToFile();
			} catch (Exception err) {
				System.err.println("Warning - settings could not be saved: " + err.getMessage());
			}
		}

		public static void loadFromFile() throws Exception {
			InputStream fileIn = new FileInputStream(getFile());
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			settings = (Settings) (objectIn.readObject());
			objectIn.close();
			fileIn.close();
		}

		public static void saveToFile() throws Exception {
			FileOutputStream fileOut = new FileOutputStream(getFile());
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(settings);
			objectOut.close();
			fileOut.close();
		}

		private static File getFile() {
			File homeDir = new File(System.getProperties().getProperty("user.home"));
			return new File(homeDir, FILENAME);
		}

	}

	public static class Icons {
		static Logger theLogger = LoggerFactory.getLogger(Icon.class);

		static Icon saveCollection = new DummyIcon();
		static Icon saveCollectionAs = new DummyIcon();
		static Icon newCollection = new DummyIcon();
		static Icon openCollection = new DummyIcon();
		static Icon recentFile = new DummyIcon();
		static Icon search = new DummyIcon();

		public static Icon addToCollection = new DummyIcon();
		public static Icon removeFromCollection = new DummyIcon();
		public static Icon viewBean = new DummyIcon();
		public static Icon properties = new DummyIcon();

		static Icon mainFrame = new DummyIcon();

		private Icons() {
		}

		static {
			saveCollection = loadIcon("saveCollection.gif");
			saveCollectionAs = loadIcon("saveCollectionAs.gif");
			newCollection = loadIcon("newCollection.gif");
			openCollection = loadIcon("openCollection.gif");
			recentFile = loadIcon("recentFile.gif");
			search = loadIcon("search.gif");
			addToCollection = loadIcon("addToCollection.gif");
			removeFromCollection = loadIcon("removeFromCollection.gif");
			viewBean = loadIcon("viewObject.gif");
			properties = loadIcon("properties.gif");
			mainFrame = loadIcon("mainFrame.gif");
		}

		static Image loadImage(String filename) {
			Toolkit tk = Toolkit.getDefaultToolkit();
			Image i = null;
			try {
				i = tk.getImage(Icons.class.getResource("icons/" + filename));
			} catch (Exception err) {
				theLogger.warn("Warning - icon '" + filename + "' could not be loaded: " + err, err);
			}
			return i;
		}

		static Icon loadIcon(String filename) {
			try {
				Object r = Icons.class.getResource(".");
				return new ImageIcon(Icons.class.getResource("icons/" + filename));
			} catch (Exception err) {
				theLogger.warn("Warning - icon '" + filename + "' could not be loaded: " + err, err);
				return new DummyIcon();
			}
		}

		static class DummyIcon implements Icon, java.io.Serializable {
			@Override public int getIconWidth() {
				return 16;
			}

			@Override public int getIconHeight() {
				return 16;
			}

			@Override public void paintIcon(Component c, Graphics g, int x, int y) {
				g.setColor(Color.blue);
				g.setFont(new Font("serif", Font.BOLD, 12));
				g.drawString("?", x, y + 12);
			}
		}
	}

	public void showMessage(String string) {
		Utility.boxPanelDisplayContext.showMessage(string);
	}

	public BoxPanelSwitchableView getBoxPanelTabPane() {
		return Utility.boxPanelDisplayContext.getBoxPanelTabPane();
	}

}

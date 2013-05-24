package org.appdapter.gui.pojo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.appdapter.gui.demo.ObjectNavigator;
import org.appdapter.gui.demo.RepoNavigator;
import org.appdapter.gui.editors.BooleanEditor;
import org.appdapter.gui.editors.ColorEditor;
import org.appdapter.gui.editors.DateEditor;
import org.appdapter.gui.editors.IntEditor;
import org.slf4j.Logger;

public class Utility {

	static Logger theLogger = org.slf4j.LoggerFactory.getLogger(Utility.class);
	private static final Class[] CLASS0 = new Class[0];

	public static POJOCollectionWithSwizzler context = new POJOCollectionImpl();

	public static POJOApp pojoApp;
	public static RepoNavigator repoNav;
	public static ObjectNavigator objectNavigator;

	public static POJOApp getCurrentPOJOApp() {
		if (pojoApp != null)
			return pojoApp;
		if (repoNav != null) {
			pojoApp = new ScreenBoxedPOJOCollectionContextWithNavigator(repoNav);
			return pojoApp;
		}
		if (objectNavigator != null) {
			pojoApp = new ScreenBoxedPOJOCollectionContextWithNavigator(objectNavigator);
			return pojoApp;
		}
		return null;
	}

	/*
	 * public static void setDefaultContext(POJOCollectionWithSwizzler c) {
	 * context = c; }
	 * 
	 * public static POJOCollectionWithSwizzler getDefaultContext() { return
	 * context; }
	 */
	//public static POJOApp objectsContext = null;

	public static Object asPOJO(Object object) {
		// TODO Auto-generated method stub
		if (object instanceof POJOBox) {
			object = ((org.appdapter.gui.pojo.POJOBox) object).getObject();
		}
		return null;
	}

	public static POJOBox findOrCreateBox(Object object) {

		if (object instanceof POJOBox) {
			return ((org.appdapter.gui.pojo.POJOBox) object);
		}
		try {
			return context.findOrCreateBox(object);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static List EMPTYLIST = new ArrayList();

	public static List getTriggersFromBeanInfo(BeanInfo beanInfo) {
		return EMPTYLIST;
	}

	/**
	 * Returns the global objectsContext, or null if none has been set
	 */
	public static POJOApp getCurrentContext() {
		return getCurrentPOJOApp();
	}

	public static POJOCollectionWithSwizzler getCurrentContext2() {
		if (context != null)
			return context;
		return null;
	}

	/**
	 * Sets the global objects context
	 */
	public static void setInstancesOfObjects(POJOCollectionWithSwizzler newValue) {
		context = (POJOCollectionWithSwizzler) newValue;
	}

	public static void setInstancesOfObjects(POJOApp newValue) {
		pojoApp = newValue;
	}

	private Utility() {
	}

	public static void registerEditors() {
		PropertyEditorManager.registerEditor(int.class, IntEditor.class);
		PropertyEditorManager.registerEditor(Integer.class, IntEditor.class);

		PropertyEditorManager.registerEditor(boolean.class, BooleanEditor.class);
		PropertyEditorManager.registerEditor(Boolean.class, BooleanEditor.class);

		PropertyEditorManager.registerEditor(Color.class, ColorEditor.class);

		PropertyEditorManager.registerEditor(Date.class, DateEditor.class);

		PropertyEditorManager.setEditorSearchPath(new String[] { "org.appdapter.gui.editors" });
	}

	static int ADD_ALL = 255;
	static int ADD_FROM_PASTE_SRC_ONLY = 255;
	static int ADD_FROM_PASTE_TARG_ONLY = 255;

	public static <TrigType> void addClassLevelTriggers(Class cls, List<TrigType> tgs, POJOBox poj) {
		HashSet<Class> flat = new HashSet<Class>();
		addClasses(cls, flat);
		for (Class cls2 : flat) {
			addClassLevelTriggersPerClass(cls2, tgs, poj, ADD_ALL);
		}
	}

	public static <TrigType> void addClasses(Class cls, HashSet<Class> classesVisited) {
		if (cls == null)
			return;
		if (classesVisited.contains(cls))
			return;
		classesVisited.add(cls);
		addClasses(cls, classesVisited);
		for (Class cls2 : cls.getInterfaces()) {
			addClasses(cls2, classesVisited);
		}
		addClasses(cls.getSuperclass(), classesVisited);
	}

	@SuppressWarnings("unchecked") public static <TrigType> void addClassLevelTriggersPerClass(Class cls, List<TrigType> tgs, POJOBox poj, int rulesOfAdd) {
		try {
			BeanInfo bi = getBeanInfo(cls);
			if (bi == null)
				return;
			addFeatureTriggers(cls, bi.getMethodDescriptors(), tgs, poj, rulesOfAdd);
			addFeatureTriggers(cls, bi.getEventSetDescriptors(), tgs, poj, rulesOfAdd);
			addFeatureTriggers(cls, bi.getPropertyDescriptors(), tgs, poj, rulesOfAdd);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	@SuppressWarnings("unchecked") public static <TrigType> void addFeatureTriggers(Class cls, FeatureDescriptor[] fd, List<TrigType> tgs, POJOBox poj, int rulesOfAdd) {
		for (FeatureDescriptor f : fd) {
			TriggerForInstance tfi = new TriggerForInstance(cls, poj, f);
			if (!tgs.contains(tfi))
				tgs.add((TrigType) tfi);
		}

	}

	public static void setBeanInfoSearchPath() {
		Introspector.setBeanInfoSearchPath(new String[] { "org.appdapter.gui.editors" });
	}

	public static Object invokeFromUI(Object obj0, Method method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return invoke(obj0, method);
	}

	public static Object invoke(Object obj0, Method method, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object obj = obj0;
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		Class[] ts = method.getParameterTypes();
		int ml = ts.length;
		if (ml == 0) {
			if (isStatic)
				obj = null;
			return method.invoke(obj);
		}
		int pl = params.length;
		// not an array of array
		if (pl > 1 && ml == pl) {
			if (isStatic)
				obj = null;
			return method.invoke(obj, params);
		}

		if (pl == 1 && ml > 1) {
			Object p0 = params[0];
			if (p0 instanceof Object[]) {
				method.invoke(obj, (Object[]) p0);
			}
		}
		if (isStatic)
			obj = null;
		return method.invoke(obj, params);
	}

	public static BeanInfo getBeanInfo(Class c) throws IntrospectionException {
		return Introspector.getBeanInfo(c);
	}

	public static BeanInfo getPOJOInfo(Class<? extends Object> c, int useAllBeaninfo) throws IntrospectionException {
		return Introspector.getBeanInfo(c, useAllBeaninfo);
	}

	public static String loadFile(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		ByteArrayOutputStream stringOut = new ByteArrayOutputStream();
		copyStream(fileIn, stringOut);
		fileIn.close();
		return new String(stringOut.toByteArray());
	}

	public static String loadURL(URL url) throws Exception {
		InputStream in = url.openStream();
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		copyStream(in, bytesOut);
		in.close();
		bytesOut.close();
		return new String(bytesOut.toByteArray());
	}

	public static void saveFile(File file, String contents) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		PrintWriter writer = new PrintWriter(fileOut);
		writer.print(contents);
		writer.close();
	}

	public static void copyStream(InputStream from, OutputStream to) throws IOException {
		int i = from.read();
		while (i > -1) {
			to.write(i);
			i = from.read();
		}
	}

	public static boolean isEqual(Object o1, Object o2) {
		if (o1 == null)
			return (o2 == null);
		else
			return (o1.equals(o2));
	}

	/**
	 * Checks if the given object is null, or if toString().trim() == "".
	 */
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		} else {
			return o.toString().trim().equals("");
		}
	}

	/**
	 * Moves the given window to the center of the screen
	 */
	public static void centerWindow(Window win) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dim = win.getSize();
		win.setLocation((screen.width / 2) - (dim.width / 2), (screen.height / 2) - (dim.height / 2));
	}

	public static Dimension getConstrainedDimension(Dimension min, Dimension pref, Dimension max) {
		Dimension result = getMinDimension(max, getMaxDimension(min, pref));
		return result;
	}

	public static Dimension getMaxDimension(Dimension a, Dimension b) {
		return new Dimension(Math.max(a.width, b.width), Math.max(a.height, b.height));
	}

	public static Dimension getMinDimension(Dimension a, Dimension b) {
		return new Dimension(Math.min(a.width, b.width), Math.min(a.height, b.height));
	}

	public static String getShortClassName(Class c) {
		String name = c.getName();
		int i = name.lastIndexOf(".");
		if (i == -1)
			return name;
		else
			return name.substring(i + 1);
	}

	/**
	 * Replace all occurences of 'a' in string 's' with 'b'
	 */
	public static String replace(String s, String a, String b) {

		int aLength = a.length();
		int bLength = b.length();
		StringBuffer buf = new StringBuffer(s);

		// how much length changes after each replacement
		int dif = b.length() - a.length();

		int len = s.length();
		int i = buf.toString().indexOf(a);
		int startFrom = 0;
		while (i != -1) {
			buf.replace(i, i + aLength, b);
			startFrom = i + bLength;
			i = buf.toString().indexOf(a, startFrom);
		}

		return buf.toString();
	}

	public static byte[] serialize(Object o) throws Exception {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
		objectOut.writeObject(o);
		objectOut.close();
		return bytesOut.toByteArray();
	}

	public static Object deserialize(byte[] bytes) throws Exception {
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
		ObjectInputStream objectIn = new ObjectInputStream(bytesIn);
		Object o = objectIn.readObject();
		objectIn.close();
		return o;
	}

	/**
	 * Generates a default name for the given object, while will be something
	 * like "Button1", "Button2", etc.
	 */
	public static String generateUniqueName(Object object) {
		if (object == null)
			return "<null>";
		String className = Utility.getShortClassName(object.getClass());
		int counter = 1;
		boolean done = false;
		String name = "???";
		while (!done) {
			name = className + counter;
			Object otherPOJO = context.findPOJO(name);
			if (otherPOJO == null) {
				done = true;
			} else {
				++counter;
			}
		}
		return name;
	}

	public static int identityHashCode(Object object) {
		return System.identityHashCode(object);
	}

	public static void setLastResult(Object whereFrom, Object obj, Class expected) {
		GetSetObject pnl = Utility.getPanelFor(expected);
		if (pnl != null) {
			try {
				pnl.setObject(obj);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			theLogger.info("result from " + whereFrom + " was " + obj);
		}

	}

	private static GetSetObject getPanelFor(Class expected) {
		return null;
	}

}
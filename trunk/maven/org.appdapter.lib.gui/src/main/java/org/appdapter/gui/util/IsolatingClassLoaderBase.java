package org.appdapter.gui.util;

import static org.appdapter.gui.util.CollectionSetUtils.addAllNew;
import static org.appdapter.gui.util.CollectionSetUtils.addIfNew;
import static org.appdapter.gui.util.PromiscuousClassUtils.rememberClass;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;

import org.appdapter.core.log.Debuggable;

abstract public class IsolatingClassLoaderBase extends URLClassLoader implements HRKRefinement, Ontologized.HRKAdded {

	public static <ET> boolean addCollection(List<Object> strings, ClassLoader thiz, ET[] elems) {
		boolean changed = false;
		if (addAllNew(strings, elems))
			changed = true;
		return changed;
	}

	public static <T> void appendURLS(final StringBuilder str, String sep, Iterable<T> urls) {
		boolean first = true;
		for (T u : urls) {
			Object url = u;
			if (url instanceof URL) {
				url = u.toString();
			}
			if (!(url instanceof String))
				continue;
			if (!first) {
				str.append(sep);
			} else {
				first = false;
			}
			str.append(url);
		}
	}

	public static boolean pathsOf(List<Object> strings, ClassLoader cl, boolean includeParent) {
		if (cl == getSystemClassLoader())
			return addIfNew(strings, "$CLASSPATH");
		if (cl instanceof IsolatingClassLoaderBase) {
			return ((IsolatingClassLoaderBase) cl).addPathStringsForDebug(strings, includeParent);
		}
		if (cl instanceof URLClassLoader) {
			boolean changed = addCollection(strings, cl, ((URLClassLoader) cl).getURLs());
			if (includeParent) {
				if (pathsOf(strings, cl.getParent(), includeParent))
					changed = true;
			}
			return changed;
		}
		return addIfNew(strings, cl.toString());
	}

	public IsolatingClassLoaderBase() {
		super(new URL[0]);
		PromiscuousClassUtils.addClassloader(this);
	}

	public IsolatingClassLoaderBase(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		PromiscuousClassUtils.addClassloader(parent);
		PromiscuousClassUtils.addClassloader(this);
	}

	public IsolatingClassLoaderBase(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
		PromiscuousClassUtils.addClassloader(parent);
		PromiscuousClassUtils.addClassloader(this);
	}

	public abstract boolean addPathStringsForDebug(List<Object> strings, boolean includeParent);

	/**
	 * <code>
	 * public void addURL(final URL url) {
	 *   super.addURL_super(url);
	 * }
	 * </code>
	 */
	@Override public abstract void addURL(URL url);

	public final void addURL_super(URL url) {
		super.addURL(url);
	}

	/**
	 * Adds the given URLs to the classpath.
	 * 
	 * @param additions
	 *            URLs to add.
	 */
	public void addURLs(final URL[] additions) {
		for (URL url : additions) {
			addURL(url);
		}
	}

	/**
	* Returns a class loaded by the bootstrap class loader;
	* or return null if not found.
	*/
	final public Class findBootstrapClassOrNull(String name) {
		return rememberClass(name, (Class) PromiscuousClassUtils.callProtectedMethodNullOnUncheck(this, "findBootstrapClassOrNull", name));
	}

	final protected Class<?> findClass(final String name) throws ClassNotFoundException {
		return rememberClass(name, findClassLocalMethodologyActuallyDefines(name));
	}

	abstract public Class findLoadedClassLocalMethodology(String name) throws ClassNotFoundException;

	abstract public Class<?> findClassLocalMethodologyActuallyDefines(String name) throws ClassNotFoundException;

	protected Class<?> findClassSuperThruURLS(String name) throws ClassNotFoundException {
		return rememberClass(name, super.findClass(name));
	}

	/**
	 * {@inheritDoc}
	 */
	final @Override public Class<?> loadClass(final String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	final @Override public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		ClassLoader parent = getParent();
		if (c == null) {
			try {
				c = findLoadedClassLocalMethodology(name);
			} catch (Throwable e) {
				Debuggable.printStackTrace(e);
			}
		}
		if (c == null) {
			try {
				if (parent != null) {
					c = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(parent, "loadClass", name, false);
				} else {
					c = findBootstrapClassOrNull(name);
				}
			} catch (Throwable e) {
				Debuggable.printStackTrace(e);
				// ClassNotFoundException thrown if class not found
				// from the non-null parent class loader
			}

		}
		if (c == null) {
			//try {
				c = findClass(name);
			//} catch (Throwable e) {
				//Debuggable.printStackTrace(e);
			//}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	public Class loadClassParentNoResolve(String class_name) throws ClassNotFoundException {
		ClassLoader p = getParent();
		try {
			return PromiscuousClassUtils.callProtectedMethod(false, p, "loadClass", class_name, false);
		} catch (InvocationTargetException e1) {
			Throwable ee = e1.getCause();
			if (ee instanceof ClassNotFoundException)
				throw (ClassNotFoundException) ee;
			Debuggable.UnhandledException(ee);
			if (ee instanceof RuntimeException)
				throw (RuntimeException) ee;
			throw new ClassNotFoundException("InvallidTarget: ", ee);
		} catch (NoSuchMethodException ee) {
			throw new ClassNotFoundException("PROGRAMMER ERROR: ", ee);
		}
	}

	@Override public String toString() {
		ArrayList<Object> strings = new ArrayList<Object>();
		this.addPathStringsForDebug(strings, true);
		final StringBuilder str = new StringBuilder();
		str.append(getClass());
		str.append('[');
		appendURLS(str, ";", strings);
		str.append(']');
		return str.toString();
	}
}

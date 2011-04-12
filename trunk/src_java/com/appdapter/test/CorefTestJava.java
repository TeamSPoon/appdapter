/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.test;

/**
 *
 * @author winston
 */
public class CorefTestJava {
	/* Consider dual concepts+types Tao and Zen (co-referent might be another way to say "dual")
		which we wish to bind together in a flexible way as follows.  Each concept may be both:
			1) Subclassed within its own branch of the type tree.
			2) Parametrized by the other concept's type system.
	 *
	 *  Such a construct can be useful when we want to innovate on two coupled axes, for example,
	 *  with razors and blades (think about all the different kinds of both at the store, and the
	 *  type restrictions they place on each other), or ski-bindings and ski-boots, or cars and tires.
	 *  If I want to model such compatibility in my type hierarchy (rather than via attribute values
	 *  checked at runtime)
	 */

	// These "Raw" coreferent classes use the raw version of the dual as type bound for the parameter.
	// This works OK, up to a point, in java 1.6.  But this framework probably cannot be extended
	// in Scala 2.8.0, as discussed in http://stackoverflow.com/questions/5580158
	public static interface JavaRawTao<JRZ extends JavaRawZen> {
		public JRZ considerAndProduceZen(JRZ jrz);
		public JavaRawTao<JRZ> considerAndProduceTao(JRZ jrz);
	}
	public static interface JavaRawZen<JRT extends JavaRawTao> {
		public JRT meditateAndProduceTao(JRT jrt);
	}
	// Next we create a dual java pair with more type specificity in the parameters, but still no methods,
	// hence "Empty".  Note that either or both types can use "? extends", but at least one must do so.
	// If neither type provides this covariant wildcard notation, we get javac 1.6.0.21 errors for both types:
	//		"type parameter com.appdapter.test.CorefTestJava.JavaEmptyTao<JEZ> is not within its bound"
	//		"type parameter com.appdapter.test.CorefTestJava.JavaEmptyZen<JET> is not within its bound"
	// With at least one "? extends", we are OK in Java, *but* again, we don't have any methods yet.

	public static interface JavaEmptyTao<JEZ extends JavaEmptyZen<? extends JavaEmptyTao<? extends JEZ>>> { }
 	public static interface JavaEmptyZen<JET extends JavaEmptyTao<? extends JavaEmptyZen<? extends JET>>> { }

	// OK, now let's make some versions that are similar, but include methods, making them "Fun"!
	public static interface JavaFunTao<JFZ extends JavaFunZen<? extends JavaFunTao<JFZ>>> {
		public JFZ consider(JFZ someZen, JavaFunTao<JFZ> otherTao);
	}
 	public static interface JavaFunZen<JFT extends JavaFunTao<? extends JavaFunZen<JFT>>> { 
		public JFT meditate(JFT someTao, JavaFunZen<JFT> otherZen);
	}
	// It's easy enough to resolve the Empty versions into some specific, nonparametric types.
	public static interface JavaResolvedTao extends JavaEmptyTao<JavaResolvedZen> { }
	public static interface JavaResolvedZen extends JavaEmptyZen<JavaResolvedTao> { }

	// Now let's make a concrete implementation of the Fun interfaces.
	public static class JCRFTao implements JavaFunTao<JCRFZen> {
		// This signature does not satisfy the interface contract.
		// public JCRFZen consider(JCRFZen someZen, JCRFTao otherTao) {
		// But this one does:
		public JCRFZen consider(JCRFZen someZen, JavaFunTao<JCRFZen> otherTao) {
			return someZen;
		}
	}
	public static class JCRFZen implements JavaFunZen<JCRFTao> {
		public JCRFTao meditate(JCRFTao someTao, JavaFunZen<JCRFTao> otherZen) {
			return someTao;
		}
	}

	public static void haveFun() {
		JCRFTao tao = new JCRFTao();
		JCRFZen zen = new JCRFZen();
		JCRFZen freshZen = tao.consider(zen, tao);
		JavaFunTao<JCRFZen> absTao = tao;
	}

// 	public static abstract class JavaZen<JT extends JavaTao<? extends JavaZen<JT>>> {
		// public abstract void meditate(JT jt); // {
//			System.out.println("JavaZen is meditiating on JavaTao:" + jt);
//		}


}

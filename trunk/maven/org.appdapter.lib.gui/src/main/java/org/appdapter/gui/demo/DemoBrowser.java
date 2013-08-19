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

package org.appdapter.gui.demo;

import static org.appdapter.core.log.Debuggable.printStackTrace;

import java.awt.BorderLayout;
import java.util.concurrent.Callable;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.CallableWithParameters;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.MutableTrigger;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.RepoBox;
import org.appdapter.demo.DemoBrowserUI;
import org.appdapter.demo.DemoNavigatorCtrlFactory;
import org.appdapter.demo.DemoResources;
import org.appdapter.gui.api.DisplayContextProvider;
import org.appdapter.gui.api.EditableTrigger;
import org.appdapter.gui.api.ScreenBox.Kind;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxTreeNodeImpl;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.repo.BridgeTriggers;
import org.appdapter.gui.repo.DatabaseTriggers;
import org.appdapter.gui.repo.DefaultMutableRepoBoxImpl;
import org.appdapter.gui.repo.RepoBoxImpl;
import org.appdapter.gui.repo.RepoModelBoxImpl;
import org.appdapter.gui.repo.RepoTriggers;
import org.appdapter.gui.trigger.BootstrapTriggerFactory;
import org.appdapter.gui.trigger.SysTriggers;
import org.appdapter.gui.trigger.TriggerForClass;
import org.appdapter.gui.trigger.TriggerForInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
final public class DemoBrowser implements AnyOper.Singleton {
	public static Logger theLogger = getLogger();

	public static DemoNavigatorCtrl mainControl;

	public static void showObject(final String optionalName, final Object any, final boolean showASAP, final boolean loadChildren) {
		// This can take up to a few seconds, depending on log level.

		try {
			ensureRunning(false);
			Utility.invokeAfterLoader(new Runnable() {
				@Override public void run() {
					mainControl.addObject(optionalName, any, showASAP, loadChildren);
					mainControl.addObject(optionalName, any, showASAP, loadChildren);
					if (showASAP)
						mainControl.show();
				}
			});
		} catch (Throwable e) {
			Debuggable.printStackTrace(e);
		}
	}

	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args) {
		if (mainControl == null) {
			try {
				ensureRunning(false, args);
			} catch (InterruptedException e) {
				printStackTrace(e);
			}
		}
		return mainControl;
	}

	// These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	public static String BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c";
	public static int BMC_NAMESPACE_SHEET_NUM = 4;
	public static int BMC_DIRECTORY_SHEET_NUM = 3;

	// These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	// When exported to Disk
	public static String BMC_WORKBOOK_PATH = "GluePuma_BehavMasterDemo.xlsx";
	public static String BMC_NAMESPACE_SHEET_NAME = "Nspc";
	public static String BMC_DIRECTORY_SHEET_NAME = "Dir";

	public static Logger getLogger() {
		try {
			theLogger = LoggerFactory.getLogger(DemoBrowser.class);
		} catch (Throwable t) {
			printStackTrace(t);
		}
		return theLogger;
	}

	public static void testLoggingSetup() {
		//System.out.println("[System.out] - DemoBrowser.pretendToBeAwesome()");
		theLogger.info("[SLF4J] - DemoBrowser.pretendToBeAwesome()");
	}

	static public boolean defaultExampleCode = false;

	static public String appName = "Wackamole";

	/**
	 * Register a Trigger to places on all instances of 'cls'
	 * @param cls
	 * @param menuLabel
	 * @param trigger
	 * 
	 * @return a TriggerForInstance (will let you further customize the behaviour for the trigger)
	 * 
	 */
	public static EditableTrigger registerTriggerForClassInstances(Class cls, String menuLabel, Trigger trigger) {
		return Utility.registerTriggerForClassInstances(cls, menuLabel, trigger);
	}

	/**
	 * Register a Trigger onto a class Object
	 * @param cls
	 * @param menuLabel
	 * @param trigger
	 * 
	 * @return a TriggerForInstance (will let you further customize the behaviour for the trigger)
	 * 
	 */
	public static EditableTrigger registerTriggerForClass(Class cls, String menuLabel, Trigger trigger) {
		return Utility.registerTriggerForClass(cls, menuLabel, trigger);
	}

	/**
	 * Register a Trigger onto a class Object
	 * @param cls
	 * @param menuLabel
	 * @param trigger
	 * 
	 * @return a TriggerForInstance (will let you further customize the behaviour for the trigger)
	 * 
	 */
	public static EditableTrigger registerTriggerForPredicate(CallableWithParameters<Box, Boolean> predicate, String menuLabel, Trigger trigger) {
		return Utility.registerTriggerForPredicate(predicate, menuLabel, trigger);
	}

	/**
	 * Register a Trigger onto a class Object
	 * @param cls
	 * @param menuLabel
	 * @param trigger
	 * 
	 * @return a TriggerForInstance (will let you further customize the behaviour for the trigger)
	 * 
	 */
	public static EditableTrigger registerCallableForPredicate(CallableWithParameters<Box, Boolean> predicate, String menuLabel, CallableWithParameters function) {
		return Utility.registerCallableForPredicate(predicate, menuLabel, function);
	}

	/**
	 * Register a Factory for a Class
	 * @param cls
	 * @param menuLabel
	 * @param trigger
	 * 
	 * @return a TriggerForInstance (will let you further customize the behaviour for the trigger)
	 * 
	 */
	public static <T> EditableTrigger registerFactoryForClass(Class<T> cls, String menuLabel, CallableWithParameters<? extends T, Class> trigger) {
		return Utility.registerFactoryForClass(cls, menuLabel, trigger);
	}

	/**
	 * Register a Trigger on a specific object
	 * @param cls
	 * @param menuLabel
	 * @param trigger
	 * 
	 * @return a TriggerForInstance (will let you further customize the behaviour for the trigger)
	 * 
	 */
	public static EditableTrigger registerTriggerForObject(Object anyObject, String shortLabel, Trigger trigger) {
		return Utility.registerTriggerForObject(anyObject, shortLabel, trigger);
	}

	/**
	 *  Ensure the main instance is started
	 * 
	 * @param bringToFront
	 * 
	 * @throws Exception
	 */
	public static synchronized void ensureRunning(boolean bringToFront, String... args) throws InterruptedException {
		if (mainControl == null) {

			theLogger.info(appName + ".ensureRunning()-START");
			try {
				//ObjectNavigator frame = new ObjectNavigator();
				//Utility.setInstancesOfObjects(frame.getChildCollectionWithContext());
				// frame.pack();
				DemoNavigatorCtrlFactory crtlMaker = new DemoNavigatorCtrlFactory() {

					@Override public DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] main, boolean defaultExampleCode1) {
						return makeDemoNavigatorCtrlReal(main, defaultExampleCode1);
					}

				};
				DemoBrowserUI.registerDemo(crtlMaker);
				//frame.setSize(800, 600);
				//org.appdapter.gui.pojo.Utility.centerWindow(frame);
				mainControl = (DemoNavigatorCtrl) DemoBrowserUI.makeDemoNavigatorCtrl(args, defaultExampleCode);
				mainControl.launchFrame("This is " + appName);

				//frame.show();
				//
				theLogger.info(appName + " is now running!");
			} catch (Exception err) {
				theLogger.error(appName + " could not be started", err);
			}
			theLogger.info(appName + ".ensureRunning()-END");
			flushIO();
		}
		try {
			if (bringToFront) {
				Utility.invokeAfterLoader(new Runnable() {
					@Override public void run() {
						mainControl.show();
					}
				});
			}
		} catch (Throwable t) {
			printStackTrace(t);
		}
	}

	private static void flushIO() {
		try {
			System.out.flush();
			System.err.flush();
		} catch (Throwable t) {

		}
	}

	public static void show() {
		try {
			ensureRunning(false);
			mainControl.show();
		} catch (InterruptedException e) {
			printStackTrace(e);
		}
	}

	// ==== Main method ==========================
	public static void main(String[] args) throws InterruptedException {
		testLoggingSetup();
		// we set 'defaultExampleCode' from main
		defaultExampleCode = true;
		ensureRunning(true, args);
		show();
		JFrame appFrame = Utility.getAppFrame();
		appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (defaultExampleCode) {
			// some more test code
			addMoreExamples();
		}
	}

	public static void addMoreExamples() {
		showObject(appName, mainControl, false, false);
		registerTriggerForObject(mainControl, "System.exit(0)", new Trigger() {
			@Override public void fire(Box targetBox) {
				System.exit(0);
			}
		});
		registerTriggerForClassInstances(mainControl.getClass(), "Count triggers", new Trigger() {
			@Override public void fire(Box targetBox) {
				String result = "" + targetBox + " has " + targetBox.getTriggers().size() + " trigger(s)";
				mainControl.showScreenBox("Count of triggers", result);
			}
		});
		registerTriggerForPredicate(new CallableWithParameters<Box, Boolean>() {

			@Override public Boolean call(Box box, Object... params) {
				int lastObj = params.length - 1;
				String named;
				if (box != null) {
					named = "" + box;
				} else {
					if (lastObj < 0) {
						return false;
					}
					named = "" + params[lastObj];
				}

				return Character.isUpperCase(named.charAt(0));
			}
		}, "I am part of the Proper crowd", new Trigger() {
			@Override public void fire(Box targetBox) {
				String result = "" + targetBox + " has " + targetBox.getTriggers().size() + " trigger(s)";
				mainControl.showScreenBox("Count of triggers", result);
			}
		});
	}

	static public class AsApplet extends JApplet {
		@Override public void init() {
			javax.swing.Box box = new javax.swing.Box(BoxLayout.Y_AXIS);
			try {
				getContentPane().setLayout(new BorderLayout());
				getContentPane().add("Center", box);
				box.add(new JLabel("Opening DemoNavigatorCtrl in a new window..."));
				DemoNavigatorCtrl dnc = makeDemoNavigatorCtrlReal(new String[0], defaultExampleCode);
				dnc.launchFrameBlocking("Appdapter Demo Browser as Applet");
				setVisible(false);
				setSize(0, 0);
			} catch (Exception err) {
				JTextArea text = new JTextArea();
				text.setEditable(false);
				text.setText("An error occurred!\nPlease email this to info@appdapter.org, thanks!\n\n" + err.toString());
				box.add(text);
			}
		}
	}

	public static interface RepoSubBoxFinder {
		public Box findGraphBox(RepoBox parentBox, String graphURI);
	}

	public static RepoSubBoxFinder theRSBF;

	public static DemoNavigatorCtrl makeDemoNavigatorCtrlReal(String[] args, boolean isExampleCode) {
		RepoSubBoxFinder rsbf = new RepoSubBoxFinder() {
			@Override public Box findGraphBox(RepoBox parentBox, String graphURI) {
				theLogger.info("finding graph box for " + graphURI + " in " + parentBox);
				MutableBox mb = new RepoModelBoxImpl();
				TriggerImpl dti = new SysTriggers.DumpTrigger();
				dti.setShortLabel("ping-" + graphURI);
				mb.attachTrigger(dti);

				Repo parentRepo = parentBox.getValue();

				return mb;
			}
		};
		DemoNavigatorCtrl dnc = (DemoNavigatorCtrl) makeDemoNavigatorCtrl(args, rsbf, isExampleCode);
		return dnc;
	}

	public static BaseDemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, RepoSubBoxFinder rsbf, boolean isExampleCode) {
		theRSBF = rsbf;
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		return makeDemoNavigatorCtrl(args, ScreenBoxImpl.class, DefaultMutableRepoBoxImpl.class, isExampleCode);
	}

	public static BaseDemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, Class<? extends ScreenBoxImpl> boxClass, Class<? extends RepoBoxImpl> repoBoxClass, boolean isExampleCode) {
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		ScreenBoxContextImpl bctx = makeBCI(boxClass, repoBoxClass, isExampleCode);
		TreeModel tm = bctx.ensureTreeModel();
		ScreenBoxTreeNodeImpl rootBTN = (ScreenBoxTreeNodeImpl) tm.getRoot();

		DisplayContextProvider dcp = bctx;

		BaseDemoNavigatorCtrl tn = new DemoNavigatorCtrl(bctx, tm, (ScreenBoxTreeNodeImpl) rootBTN, dcp);
		return tn;
	}

	/**
	 * <code>
	 * 	<BT extends ScreenBoxImpl<TriggerImpl<BT>>, RBT extends RepoBoxImpl<TriggerImpl<RBT>>> 
	 *		ScreenBoxContextImpl makeBCI(Class<BT> boxClass, Class<RBT> repoBoxClass) {.. }
	 * </code>
	 */
	public static <BTI extends TriggerImpl<BT>, BT extends ScreenBoxImpl<BTI>, RBTI extends TriggerImpl<RBT>, RBT extends RepoBoxImpl<RBTI>> ScreenBoxContextImpl makeBCI(Class<BT> boxClass,
			Class<RBT> repoBoxClass, boolean isExampleCode) {
		TriggerImpl<BT> regTrigProto = makeTriggerPrototype(boxClass);
		TriggerImpl<RBT> repoTrigProto = makeTriggerPrototype(repoBoxClass);
		return makeBoxContextImpl(boxClass, repoBoxClass, regTrigProto, repoTrigProto, isExampleCode);
	}

	@SuppressWarnings("unchecked") public static <BTS extends TriggerImpl<BT>, BT extends ScreenBoxImpl<BTS>, TBT extends TriggerImpl<BT>> TBT makeTriggerPrototype(Class<BT> boxClass) {
		// The trigger subtype does not matter - what matters is capturing BT into the type.
		return (TBT) new SysTriggers.QuitTrigger().makeTrigger(boxClass);
	}

	// static class ConcBootstrapTF extends BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
	// }  //   TT extends TriggerImpl<BT>

	/** Here is a humdinger of a static method, that constructs a demontration application tree
	 * 
	 * @param <BT>
	 * @param <RBT>
	 * @param regBoxClass
	 * @param repoBoxClass
	 * @param regTrigProto - defines the BT  trigger parameter type for screen boxes.  The regTrigProto instance data is unused.
	 * @param repoTrigProto - defines the RBT trigger parameter type for repo boxes.  The repoTrigProto instance data is unused.
	 * @return 
	 */
	public static <TBT extends TriggerImpl<BT>, BT extends ScreenBoxImpl<TBT>, TRBT extends TriggerImpl<RBT>, RBT extends RepoBoxImpl<TRBT>>

	ScreenBoxContextImpl makeBoxContextImpl(Class<BT> regBoxClass, Class<RBT> repoBoxClass, TriggerImpl<BT> regTrigProto, TriggerImpl<RBT> repoTrigProto, boolean isExampleCode) {
		try {

			BT rootBox = (BT) DemoServiceWrapFuncs.makeTestBoxImplWithValue((Class<BT>) regBoxClass, Class.class, "All Objects");

			ScreenBoxContextImpl bctx = new ScreenBoxContextImpl(rootBox);

			BootstrapTriggerFactory btf = new BootstrapTriggerFactory();
			btf.attachTrigger(rootBox, new SysTriggers.QuitTrigger(), "quit");

			TriggerImpl regTrigProtoE = regTrigProto;

			if (!isExampleCode)
				return bctx;

			BT repoBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImplWithObj(rootBox, (Class) regBoxClass, Repo.class, "repo");
			BT appBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImplWithObj(rootBox, (Class) regBoxClass, BasicDebugger.class, "app");
			BT sysBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImplWithObj(rootBox, (Class) regBoxClass, ScreenBoxImpl.class, "sys");
			BT clipboardBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImplWithObj(rootBox, (Class) regBoxClass, Utility.getClipboard(), "Clipboard");

			if (!isExampleCode)
				return bctx;

			RBT r1Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, (Class) repoBoxClass, regTrigProtoE, "h2.td_001");

			btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
			btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
			btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", Kind.REPO_MANAGER);

			RBT r2Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, (Class) repoBoxClass, regTrigProtoE, "repo_002");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpD");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpC");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpA");

			BT fishBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, regTrigProtoE, "fishy");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", Kind.MATRIX);

			btf.attachTrigger(fishBox, new SysTriggers.DumpTrigger(), "dumpF");

			BT pumappBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, regTrigProtoE, "pumapp");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", Kind.MATRIX);
			btf.attachTrigger(pumappBox, new SysTriggers.DumpTrigger(), "dumpP");

			BT buckTreeBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, regTrigProtoE, "bucksum");
			btf.attachTrigger(buckTreeBox, new BridgeTriggers.MountSubmenuFromTriplesTrigger(DemoResources.MENU_ASSEMBLY_PATH), "loadSubmenus");

			/*
						makeChildNode(appNode, "custy");
						makeChildNode(appNode, "rakedown");

						makeChildNode(sysNode, "log");
						makeChildNode(sysNode, "job");
						*/
			return bctx;
		} catch (Throwable t) {
			t.printStackTrace();
			theLogger.error("problem in tree init", t);
			return null;
		}

	}

	public static void close() {
		JFrame main = Utility.getAppFrame();
		DemoBrowser.mainControl = null;
		main.setVisible(false);
		main.dispose();

	}

	/*
	public <BT extends ScreenBoxImpl> BT makeRepoChildBoxImpl(Box parentBox, Class<BT> childBoxClass, TriggerImpl trigProto, String label, Repo.WithDirectory inner) {
		BT result = null;
		BoxContext ctx = parentBox.getBoxContext();
		result = makeRepoBoxImpl(childBoxClass, trigProto, label, inner);
		ctx.contextualizeAndAttachChildBox(parentBox, result);
		return result;
	}

	public <BT extends ScreenBoxImpl> BT makeRepoBoxImpl(Class<BT> boxClass, TriggerImpl trigProto, String label, Repo.WithDirectory inner) {
		MutableScreenBoxForImmutableRepo result = new MutableScreenBoxForImmutableRepo(label, inner);// CachingComponentAssembler.makeEmptyComponent(boxClass);
		result.setShortLabel(label);
		// set the child's BoxContext (redundant since the next line does it)
		BoxContext cctx = result.getBoxContext();
		if (cctx == null) {
			result.setContext(myBoxCtx);
		}

		result.setDescription("full description for " + boxClass.getName() + " with label: " + label);
		return (BT) (Object) result;
	}
	*/
}
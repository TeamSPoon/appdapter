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
package org.appdapter.core.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.core.debug.UIAnnotations.UIHidden;
import org.appdapter.core.debug.UIAnnotations.UtilClass;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;
import org.appdapter.demo.DemoResources;
import org.appdapter.fileconv.FileStreamUtils;
import org.appdapter.trigger.bind.jena.TriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.vocabulary.RDF;

// import com.hp.hpl.jena.query.DataSource;

/**
 * @author Dmiles
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
@UIHidden
public class RepoOper implements AnyOper, UtilClass {
	@UISalient
	static public interface ISeeToString {
		@Override
		@UISalient(MenuName = "Call ToString")
		public String toString();
	}

	@UISalient
	static public interface ReloadableDataset {

		@UISalient(MenuName = "Reload Repo")
		void reloadAllModels();

		@UISalient()
		void reloadSingleModel(String modelName);

		@UISalient(ToValueMethod = "toString")
		Dataset getMainQueryDataset();

		/**
		 * Causes a repo to replace its mainQueryDataset with the 'ds' param
		 * 
		 * To switch from a file repo to a database repo
		 * 
		 * ReloadableDataset myRepo = new URLRepoSpec("myturtle.ttl").makeRepo();
		 * 
		 * Dataset old = myRepo.mainQueryDataset();
		 * 
		 * Dataset newDs = SDB.store();
		 * 
		 * 
		 * 
		 * 
		 * @param ds
		 * @return
		 */
		@UISalient(ToValueMethod = "toString")
		void setMyMainQueryDataset(Dataset ds);
	}

	// static class ConcBootstrapTF extends
	// BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
	// } // TT extends TriggerImpl<BT>
	public static class ReloadAllModelsTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

		Repo.WithDirectory m_repo;

		// @TODO obviouly we should be using specs and not repos! but
		// With.Directory may as well be the spec for now.
		// Also consider we are using the actual Repo (not the Spec) due to the
		// fact we must have something to clear and update right?
		public ReloadAllModelsTrigger(Repo.WithDirectory repo) {
			m_repo = repo;
		}

		@Override
		public void fire(RB targetBox) {
			String resolvedQueryURL = DemoResources.QUERY_PATH;
			ClassLoader optCL = getClass().getClassLoader();
			if (targetBox != null) {
				optCL = targetBox.getClass().getClassLoader();
			}
			if (!(m_repo instanceof RepoOper.ReloadableDataset)) {
				theLogger.error("Repo not reloadable! " + targetBox);
			} else {
				RepoOper.ReloadableDataset reloadme = (RepoOper.ReloadableDataset) targetBox;
				reloadme.reloadAllModels();
			}
			String resultXML = targetBox.processQueryAtUrlAndProduceXml(resolvedQueryURL, optCL);
			logInfo("ResultXML\n-----------------------------------" + resultXML + "\n---------------------------------");
		}
	}

	static public class ReloadSingleModelTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

		final String graphURI;
		final ReloadableDataset m_repo;

		public ReloadSingleModelTrigger(String graphUri, ReloadableDataset repo) {
			this.graphURI = graphUri;
			m_repo = repo;
		}

		@Override
		public void fire(RB targetBox) {
			m_repo.reloadSingleModel(graphURI);
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(RepoOper.class);

	@UISalient
	public static boolean inPlaceReplacements = false;
	@UISalient
	public static boolean isMergeDefault = true;

	@UISalient
	public static void replaceModelElements(Model dest, Model src) {
		if (src == dest) {
			return;
		}
		dest.removeAll();
		dest.add(src);
		dest.setNsPrefixes(src.getNsPrefixMap());
		// dest.getGraph().getPrefixMapping().equals(obj)
		//if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
		///dest.setNsPrefix("#", src.getNsPrefixURI("#"));
	}

	@UISalient
	public static void replaceModelElements(Model dest, Model src, Resource unionOrReplace) {
		if (src == dest) {
			return;
		}
		boolean isReplace = isReplace(unionOrReplace);
		if (isReplace)
			dest.removeAll();
		dest.add(src);
		dest.setNsPrefixes(src.getNsPrefixMap());
		// dest.getGraph().getPrefixMapping().equals(obj)
		//if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
		///dest.setNsPrefix("#", src.getNsPrefixURI("#"));
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src, String onlyModel) {
		onlyModel = correctModelName(onlyModel);
		replaceDatasetElements(dest, src, onlyModel, null);
	}

	public static String correctModelName(String onlyModel) {
		return RepoDatasetFactory.correctModelName(onlyModel).getURI();
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src, String onlyModel, Resource unionOrReplace) {
		onlyModel = correctModelName(onlyModel);
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
			return;
		}
		boolean isReplace = isReplace(unionOrReplace);
		Dataset sdest = (Dataset) dest;
		boolean onSrc = true, onDest = true;
		if (!dest.containsNamedModel(onlyModel)) {
			onSrc = false;
			theLogger.warn("Orginal did not contain model" + onlyModel);

		}
		if (!src.containsNamedModel(onlyModel)) {
			onDest = false;
			theLogger.warn("New did not contain model " + onlyModel);
		}
		if (onSrc && onDest) {
			Model destModel = src.getNamedModel(onlyModel);
			Model srcModel = dest.getNamedModel(onlyModel);
			replaceModelElements(destModel, srcModel, unionOrReplace);
			theLogger.info("Replaced model " + onlyModel);
			return;
		}
		if (onSrc) {
			sdest.addNamedModel(onlyModel, src.getNamedModel(onlyModel));
			theLogger.info("Added model " + onlyModel);
			return;
		}
		if (onDest) {
			if (isReplace) {
				dest.getNamedModel(onlyModel).removeAll();
				theLogger.info("clearing model " + onlyModel);
			}
			return;
		}
	}

	public static void readDatasetFromURL(String srcPath, Dataset target, Resource unionOrReplace) throws IOException {
		final Model loaderModel = ModelFactory.createDefaultModel();
		final Dataset loaderDataset = DatasetFactory.createMem();
		Model m = loaderDataset.getDefaultModel();
		if (m == null) {
			m = ModelFactory.createDefaultModel();
			loaderDataset.setDefaultModel(m);
		}
		final Model[] currentModel = new Model[] { m, null, null };
		final String[] modelName = new String[] { "" };
		final Map<String, Model> constits = new HashMap();
		loaderModel.register(new StatementListener() {

			@Override
			public void addedStatement(Statement arg0) {
				System.out.println("Adding statement: " + arg0);
				String subjStr = "" + arg0.getSubject();
				if (subjStr.equals("self")) {
					// processing directive
					RDFNode r = arg0.getObject();
					if (r.isLiteral()) {
						// is a model start declaration;
						String baseURI = modelName[0] = r.asLiteral().getString();
						Model newModel = currentModel[0] = ModelFactory.createDefaultModel();
						currentModel[0].setNsPrefix("", baseURI);
					} else if (r.isResource()) {
						// is a model ending declaration (we dont clear)
						Resource rs = r.asResource();
						String type = rs.getLocalName();
						Model newModel = currentModel[0];
						newModel.setNsPrefixes(loaderModel.getNsPrefixMap());
						if (type.equals("DirectoryModel")) {
							currentModel[1] = currentModel[0];
						} else if (type.equals("RepoSheetModel")) {
							constits.put(modelName[0], currentModel[0]);
						} else if (type.equals("DatasetDefaultModel")) {
							currentModel[2] = currentModel[0];
						}
					}
				} else {
					currentModel[0].add(arg0);
				}
			}
		});
		FileStreamUtils fus = new ExtendedFileStreamUtils();
		InputStream fis = fus.openInputStream(srcPath, null);
		InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset().name());
		loaderModel.read(isr, null, "TTL");

		if (currentModel[2] != null)
			loaderDataset.setDefaultModel(currentModel[2]);

		for (Map.Entry<String, Model> entry : constits.entrySet()) {
			loaderDataset.addNamedModel(entry.getKey(), entry.getValue());
		}
		replaceDatasetElements(target, loaderDataset, null, unionOrReplace);
	}

	public static void replaceNamedModel(Dataset dest, String urlModel, Model model, Resource unionOrReplace) {
		urlModel = RepoOper.correctModelName(urlModel);
		Lock lock = dest.getLock();
		lock.enterCriticalSection(Lock.WRITE);
		Lock oldLock = null;
		model.enterCriticalSection(Lock.READ);
		try {
			long size = model.size();
			boolean isReplace = isReplace(unionOrReplace);
			boolean onDest = true;
			if (!dest.containsNamedModel(urlModel)) {
				onDest = false;
			}
			if (!onDest) {
				dest.addNamedModel(urlModel, model);
				theLogger.warn("Added new model " + urlModel + " size=" + size);
				return;
			}
			Model old = dest.getNamedModel(urlModel);
			if (old == model) {
				old = null;
				theLogger.warn("Nothing to do.. same model " + urlModel + " size=" + size);
				return;
			}
			oldLock = old.getLock();
			oldLock.enterCriticalSection(Lock.WRITE);
			long sizeBefore = old.size();
			if (RepoOper.inPlaceReplacements) {
				old.removeAll();
				theLogger.warn("In place (Replacing) old model " + urlModel + " size=" + sizeBefore + "-> " + old.size());
				isReplace = false;
			}
			if (!isReplace) {
				old.add(model);
				sizeBefore = old.size();
				old.getNsPrefixMap().putAll(model.getNsPrefixMap());
				long sizeNow = old.size();
				theLogger.warn("Merging into old model " + urlModel + " size(" + sizeBefore + "+" + model.size() + ")->" + sizeNow);
				return;
			}
			theLogger.warn("Replacing old model " + urlModel + " size=" + sizeBefore + "->" + model.size());
			dest.replaceNamedModel(urlModel, model);
		} finally {
			if (oldLock != null)
				oldLock.leaveCriticalSection();

			if (model != null) {
				model.leaveCriticalSection();
			}
			if (lock != null)
				lock.leaveCriticalSection();
		}
	}

	public static void clearAll(Dataset ds) {
		Model dModel = ds.getDefaultModel();
		removeAll(dModel);
		Iterator<String> sIterator = ds.listNames();
		while (sIterator.hasNext()) {
			String mName = sIterator.next();
			Model model = ds.getNamedModel(mName);
			removeAll(model);
		}
	}

	private static void removeAll(Model model) {
		if (model == null || model.size() == 0)
			return;
		model.removeAll();
	}

	private static boolean isReplace(Resource unionOrReplace) {
		boolean isReplace = !isMergeDefault;
		if (unionOrReplace != null) {
			theLogger.warn("Found union/replace = " + unionOrReplace);
			if (unionOrReplace.getLocalName().equals("Union"))
				isReplace = false;
		}
		return isReplace;
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src) {
		replaceDatasetElements(dest, src, (Resource) null);
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src, Resource unionOrReplace) {
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
			return;
		}
		Dataset sdest = (Dataset) dest;
		Model defDestModel = dest.getDefaultModel();
		Model defSrcModel = src.getDefaultModel();
		replaceModelElements(defDestModel, defSrcModel, unionOrReplace);
		HashSet<String> dnames = setOF(sdest.listNames());
		HashSet<String> snames = setOF(src.listNames());
		HashSet<String> replacedModels = new HashSet<String>();
		boolean isReplace = isReplace(unionOrReplace);

		for (String nym : snames) {
			Model getsrc = src.getNamedModel(nym);
			if (dest.containsNamedModel(nym)) {
				Model getdest = dest.getNamedModel(nym);
				replacedModels.add(nym);
				replaceModelElements(getdest, getsrc, unionOrReplace);
				dnames.remove(nym);
				continue;
			}
		}
		for (String nym : replacedModels) {
			snames.remove(nym);
		}

		if (dnames.size() == 0) {
			if (snames.size() == 0) {// perfect!
				return;
			} else {
				// add the new models to the datasource
				for (String nym : snames) {
					sdest.addNamedModel(nym, src.getNamedModel(nym));
				}
				// still good
				return;
			}
		} else {
			// dnames > 0
			if (snames.size() == 0) {
				// some graphs might need cleared?
				for (String nym : dnames) {
					if (isReplace) {
						sdest.getNamedModel(nym).removeAll();
						sdest.removeNamedModel(nym);
					}
				}
				return;
			} else {
				// New names to add AND graphs might need cleared
				for (String nym : dnames) {
					if (isReplace) {
						sdest.getNamedModel(nym).removeAll();
						sdest.removeNamedModel(nym);
					}
				}
				for (String nym : snames) {
					sdest.addNamedModel(nym, src.getNamedModel(nym));
				}
			}
		}
	}

	public static <E> HashSet<E> setOF(Enumeration<E> en) {
		HashSet<E> hs = new HashSet<E>();
		while (en.hasMoreElements()) {
			E e = (E) en.nextElement();
			hs.add(e);
		}
		return hs;
	}

	public static <E> HashSet<E> setOF(Iterator<E> en) {
		HashSet<E> hs = new HashSet<E>();
		while (en.hasNext()) {
			E e = (E) en.next();
			hs.add(e);
		}
		return hs;
	}

	public static void registerDatasetFactory(String datasetTypeName, UserDatasetFactory factory) {
		RepoDatasetFactory.registerDatasetFactory(datasetTypeName, factory);
	}

	public static void writeDataset(Dataset wDataset, PrintStream originalErrStream) {
		// TODO Auto-generated method stub

	}

	static private String bar = "########################################\n";

	public static void writeToTTL(Repo boundRepo, Writer ow) throws IOException {

		String repoName = "" + boundRepo;
		Dataset ds = boundRepo.getMainQueryDataset();
		String thiz = "<_:self>";
		Model dirModel = null;
		if (boundRepo instanceof Repo.WithDirectory) {
			dirModel = ((Repo.WithDirectory) boundRepo).getDirectoryModel();
		}
		if (true) {
			String rname = new SimpleDateFormat("yyyyMMddHH_mmss_SSS").format(new Date());
			String dir = "loaded_" + rname + "/";
			String csiURI = dirModel.getNsPrefixURI("csi");
			Node fileRepoName = dirModel.getResource(csiURI + "filerepo_" + rname).asNode();
			saveRepoAsManyTTLs(fileRepoName, dir, dirModel, ds, false);
			return;
		}

		ow.write("# reponame=" + repoName + "\n");
		ow.write("# time=" + new Date().toString() + "\n");
		/*if (dirModel == null) {
			dirModel = ModelFactory.createDefaultModel();
		}
		String modelName = addNamedModel("", dirModel);
		*/
		writeToTTL(repoName, thiz, dirModel, ds, ow);
	}
	public static void writeToTTL(Repo boundRepo) throws IOException {
		if (!(boundRepo instanceof Repo.WithDirectory)) {
            return;
		}
		Dataset ds = boundRepo.getMainQueryDataset();
        Model dirModel = ((Repo.WithDirectory) boundRepo).getDirectoryModel();
        String rname = new SimpleDateFormat("yyyyMMddHH_mmss_SSS").format(new Date());
        String dir = "loaded_" + rname + "/";
        String csiURI = dirModel.getNsPrefixURI("csi");
        Node fileRepoName = dirModel.getResource(csiURI + "filerepo_" + rname).asNode();
        saveRepoAsManyTTLs(fileRepoName, dir, dirModel, ds, false);
	}

	public static void saveRepoAsManyTTLs(Node fileRepoName, String dir, Model dirModel, Dataset ds, boolean dontChangeDir) throws IOException {
		new File(dir).mkdir();

		String ccrtNS = dirModel.getNsPrefixURI("ccrt");
		String frtURI = dirModel.getNsPrefixURI("frt");
		Node fileRepo = dirModel.getResource(ccrtNS + "FileRepo").asNode();
		Node fileModel = dirModel.getResource(ccrtNS + "FileModel").asNode();
		Set<Node> sheetTypes = new HashSet<Node>();
		sheetTypes.add(fileModel);
		sheetTypes.add(dirModel.getResource(ccrtNS + "GoogSheet").asNode());
		sheetTypes.add(dirModel.getResource(ccrtNS + "XlsxSheet").asNode());
		sheetTypes.add(dirModel.getResource(ccrtNS + "CsvFileSheet").asNode());
		Set<Node> sheetTypesToLocalize = new HashSet<Node>();
		sheetTypesToLocalize.addAll(sheetTypes);
		sheetTypesToLocalize.add(fileModel);
		Node repo = dirModel.createProperty(frtURI, "repo").asNode();
		Node rdftype = RDF.type.asNode();
		Node sourcePath = dirModel.createProperty(frtURI, "sourcePath").asNode();

		Iterator<Node> dni = ds.asDatasetGraph().listGraphNodes();
		Model defaultModel = ds.getDefaultModel();
		Node defaultURI = null;

		Graph dirqGraph = dirModel.getGraph();
		{
			// letting dir models advertise all namespaces
			File file = new File(dir, dontChangeDir ? "dir.ttl" : "dir.old");
			PrintWriter ow = new PrintWriter(file);
			ow.println("\n");
			dirModel.write(ow, "TTL");
			ow.println("\n");
			ow.println("# modelSize=" + dirModel.size() + "\n\n");
			ow.close();
		}
		Graph newGraph = GraphFactory.createGraphMem();

		newGraph.add(new Triple(fileRepoName, sourcePath, NodeFactory.createLiteral(dir)));
		newGraph.add(new Triple(fileRepoName, rdftype, fileRepo));
		while (dni.hasNext()) {
			Node gname = dni.next();
			String name = gname.getLocalName();
			String filename = name + ".ttl";
			PrintWriter ow = new PrintWriter(new File(dir, filename));
			Model m = ds.getNamedModel(gname.toString());
			for (Triple was : dirqGraph.find(gname, rdftype, Node.ANY).toList()) {
				if (!sheetTypes.contains(was.getObject())) {
					newGraph.add(was);
				}
				//NodeIterator foo = 
				// dirModel.listObjectsOfProperty(dirModel.createResource(gname.getURI()), dirModel.createProperty(sourcePath.getURI()));
				newGraph.add(new Triple(gname, repo, fileRepoName));
				newGraph.add(new Triple(gname, rdftype, fileModel));
				newGraph.add(new Triple(gname, sourcePath, NodeFactory.createLiteral(filename)));
			}
			ow.println("\n");
			writeModelOnlyReferncedNamespace(ow, m, dirModel);
			ow.println("\n");
			ow.println("# modelName=" + gname);
			ow.println("# modelSize=" + m.size() + "\n\n");
			if (m == defaultModel) {
				defaultURI = gname;
				ow.println("# isDefaultModel\n");
			}

			ow.close();
		}

		if (dirModel != null && !dontChangeDir) {
			File file = new File(dir, "dir.ttl");
			PrintWriter ow = new PrintWriter(file);
			ow.println("# load this with..  Repo repo = new UrlRepoSpec(\"" + file.toURL() + "\").makeRepo();\n");
			ow.println("\n");
			Model m = ModelFactory.createDefaultModel();
			m.setNsPrefixes(dirModel);
			m.add(ModelFactory.createModelForGraph(newGraph));
			m.write(ow, "TTL");
			ow.println("\n");
			ow.println("# modelSize=" + dirModel.size() + "\n\n");
			ow.println("# dirModel = " + dirModel.size());
			if (defaultURI != null) {
				ow.println("# defaultModel = " + defaultURI);
			}
			ow.close();
		}
	}

	private static void writeModelOnlyReferncedNamespace(PrintWriter ow, Model m, PrefixMapping pm) {
		Model m2 = ModelFactory.createDefaultModel();
		List<Statement> stmts = m.listStatements().toList();
		Set<String> usedNS = new HashSet<String>(m.listNameSpaces().toList());

		for (Statement stmt : stmts) {
			String ns = stmt.getSubject().getNameSpace();
			if (ns != null) {
				usedNS.add(ns);
			}
		}
		for (String ns : usedNS) {
			String prefix = m.getNsURIPrefix(ns);
			if (prefix == null)
				prefix = pm.getNsURIPrefix(ns);
			if (prefix == null) {
				continue;
			}
			m2.setNsPrefix(prefix, ns);
		}
		m2.add(m);
		m2.write(ow, "TTL");
	}

	public static void writeToTTL(String repoName, String thiz, Model dirModel, Dataset ds, Writer ow) throws IOException {

		if (dirModel != null) {
			ow.write("# dirModel = " + dirModel.size() + "\n");
			ow.flush();
			dirModel.write(ow, "TTL");
			ow.write("\n\n");
			ow.write(thiz + " a ccrt:DirectoryModel. \n");
			ow.write("\n\n");
			ow.flush();
		}
		Iterator dni = ds.listNames();
		Model defaultModel = ds.getDefaultModel();
		while (dni.hasNext()) {
			String name = (String) dni.next();
			ow.write("\n\n");
			ow.write(bar);
			ow.write("# modelName=" + name + "\n");
			ow.write(thiz + " ccrt:sheetName \"" + name + "\".\n");
			Model m = ds.getNamedModel(name);
			ow.write(getModelSource(m) + "\n");
			ow.write("# modelSize=" + m.size() + "\n");
			ow.write(thiz + " a ccrt:RepoSheetModel. \n");
			if (m == defaultModel) {
				ow.write(thiz + " a ccrt:DatasetDefaultModel. \n");
				defaultModel = null;
			}
			ow.write("\n\n");
			ow.flush();
		}
		if (defaultModel != null) {
			ow.write(bar);
			ow.write("# defaultModel..." + "\n");
			String name = getBaseURI(defaultModel, null);
			ow.write("# modelName=" + name + "\n");
			ow.write(thiz + " ccrt:sheetName \"" + name + "\".\n");
			ow.write(getModelSource(defaultModel) + "\n");
			ow.write("\n\n");
			ow.write("# modelSize=" + defaultModel.size() + "\n");
			ow.write(thiz + " a ccrt:DatasetDefaultModel. \n");
			ow.write("\n\n");
		}
		ow.flush();
	}

	public static String getBaseURI(Model defaultModel, String name) {
		String baseURI = defaultModel.getNsPrefixURI("");
		if (baseURI == null) {
			baseURI = name;
		}
		if (baseURI == null)
			baseURI = "http://modelToOntoModel/modelToOntoModel_model_" + System.identityHashCode(defaultModel) + "#";
		return baseURI;
	}

	public static String getModelSource(Model boundModel) {
		// Serialize model and update text area
		StringWriter writer = new StringWriter();
		//xferPrefixes(boundModel, null);
		Map<String, String> pmap = boundModel.getNsPrefixMap();
		boundModel.write(writer, "TTL");
		String turtle = writer.toString();
		Iterator it = pmap.keySet().iterator();
		while (it.hasNext()) {
			String prefix = (String) it.next();
			String uri = pmap.get(prefix);
			if (prefix.length() > 0) {
				String remove = "\\@prefix " + prefix + "\\:.*\\<" + uri + "\\> .\n";
				turtle = turtle.replaceAll(remove, "");
			}
		}
		return turtle;
	}

	static public Model loadTTLReturnDirModel(final Dataset targetDataset, InputStream fis) {
		final Model[] currentLModel = new Model[] { null };
		final Model[] currentDirModel = new Model[] { null };
		final Model[] currentDefaultModel = new Model[] { null };
		final String[] modelName = new String[] { null };
		final Model loaderModel = ModelFactory.createDefaultModel();
		final PrefixMapping nsMap = loaderModel;
		loaderModel.register(new StatementListener() {

			@Override
			public void addedStatement(Statement arg0) {

				System.out.println("Adding statement: " + arg0);
				String subjStr = "" + arg0.getSubject();
				if (subjStr.equals("self")) {
					// processing directive
					RDFNode r = arg0.getObject();
					if (r.isLiteral()) {
						// is a model start declaration;
						modelName[0] = r.asLiteral().getString();
						currentLModel[0] = RepoOper.findOrCreate(targetDataset, modelName[0], nsMap);
						return;
					} else if (r.isResource()) {
						// is a model ending declaration (we dont clear)
						Resource rs = r.asResource();
						String type = rs.getLocalName();
						if (type.equals("DirectoryModel")) {
							currentDirModel[0] = currentLModel[0];
							return;
						} else if (type.equals("RepoSheetModel")) {
							currentLModel[0] = RepoOper.findOrCreate(targetDataset, modelName[0], nsMap);
							return;
						} else if (type.equals("DatasetDefaultModel")) {
							currentDefaultModel[0] = currentLModel[0];
							return;
						}
					}
					throw new UnsupportedOperationException("Not sure how to load this: " + arg0);
				} else {
					//currentLModel[0] = RepoOper.findOrCreate(targetDataset, modelName[0], nsMap);
					currentLModel[0].add(arg0);
					return;
				}
			}
		});
		try {
			InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset().name());
			loaderModel.read(isr, null, "TTL");
		} catch (Throwable t) {
			Debuggable.printStackTrace(t);
		}

		if (currentDefaultModel[0] != null)
			targetDataset.setDefaultModel(currentDefaultModel[0]);
		return currentDirModel[0];
	}

	protected static Model findOrCreate(Dataset targetDataset, String baseURI, final PrefixMapping nsMap) {
		if (targetDataset.containsNamedModel(baseURI)) {
			return targetDataset.getNamedModel(baseURI);
		}
		Model newModel = ModelFactory.createDefaultModel();
		targetDataset.addNamedModel(baseURI, newModel);
		newModel.setNsPrefixes(nsMap);
		newModel.setNsPrefix("", baseURI);
		return newModel;
	}

}

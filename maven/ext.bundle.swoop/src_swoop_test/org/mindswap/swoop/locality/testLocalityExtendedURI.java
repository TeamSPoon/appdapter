package org.mindswap.swoop.locality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.debug.utils.Timer;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.refactoring.LocalityCheckerExtended;
import org.semanticweb.owl.model.OWLOntology;

public class testLocalityExtendedURI {

	SwoopModel swoopModel = new SwoopModel();
	boolean DEBUG = true;
	Map entTest = new HashMap();
	String NEWLINE = System.getProperty("line.separator");
	Timer testTimer;
	String logFile = "";
	List testOnt;

	public testLocalityExtendedURI() throws Exception {

		// load ontologies
		this.testOnt = new ArrayList();

		// read URIs
		String loc = "C:/ontologies/ontologies4.txt";
		File f = new File(loc);
		StringBuffer contents = new StringBuffer();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (DEBUG) {
					System.out.println("Reading Ontology " + line);
				}
				OWLOntology ont = swoopModel.loadOntology(new URI(line));
				testOnt.add(ont);
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					//flush and close both "input" and its underlying FileReader
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		System.out.println("DONE: Ontologies Loaded");

	}

	public void runTest() throws Exception {
		Iterator iter = testOnt.iterator();
		int loc = 0;
		int nloc = 0;
		int total = 0;
		int cycle = 0;
		int imported = 0;
		while (iter.hasNext()) {
			OWLOntology ontology = (OWLOntology) iter.next();
			System.out.println("-------------------------------------------");
			System.out.println("Testing Ontology " + ontology.getURI().toString());
			LocalityCheckerExtended checker = new LocalityCheckerExtended(ontology);
			Set foreign = new HashSet();
			foreign = swoopModel.getEntitySet(ontology, swoopModel.IMPORTED_ONT, swoopModel.ALL);
			Set trans = swoopModel.getEntitySet(ontology, swoopModel.IMPORTED_ONT, swoopModel.ALL);
			int importedEntities = foreign.size();

			if (DEBUG) {
				//System.out.println("Total number of entities in transitive closure: " + totalEntities);
				System.out.println("number of foreign entities: " + foreign.size());

				if (foreign.size() > 0) {
					imported = imported + 1;
				}

				//if(!ontology.getIncludedOntologies().isEmpty()){
				//imported = imported +1;
				//}
			}
			if (!checker.isHierarchical(ontology)) {
				System.out.println("The importing structure contains a CYCLE");
				cycle = cycle + 1;
			} else {
				if (checker.isLocal(ontology, foreign)) {
					System.out.println("The ontology is Local");
					loc = loc + 1;
				} else {
					System.out.println("The ontology is non-local");
					Set nlocal = checker.getNonLocalAxioms(foreign);
					nloc = nloc + 1;
					OWLOntology ont = checker.createNonLocalPart(nlocal);
					String result = checker.renderNonLocal(ont);
					String usedForeign = checker.renderForeignInNonLocal(ont, foreign);

					System.out.println(result);
					System.out.println("The ontology has " + nlocal.size() + " non-local axioms");
					System.out.println("The foreign entities in the non-local part are " + usedForeign);
				}
			}
			System.out.println("-------------------------------------------");

		}
		total = loc + nloc;
		System.out.println("Total number of ontologies: " + total);
		System.out.println("Total number of ontologies importing other ontologies: " + imported);
		System.out.println("Total number of LOCAL ontologies: " + loc);
		System.out.println("Total number of NON-LOCAL ontologies: " + nloc);
		System.out.println("Total number of NON-HIERARCHICAL integrations: " + cycle);

	}

	public void writeLogFile() throws Exception {
		FileWriter fw = new FileWriter(new File("debugEvalLog.txt"));
		fw.write(logFile);
		fw.close();
		System.out.println("Written log: debugEvalLog.txt");
	}

	public void cleanLog() throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(new File("LocalityTest.txt")));
		String line = null;
		String newLog = "";
		swoopModel.setShowQNames(false);
		while ((line = in.readLine()) != null) {
			if (line.indexOf("(") != -1) {
				String token1 = line.substring(line.indexOf("(") + 1, line.indexOf(","));
				String token2 = line.substring(line.indexOf(",") + 1, line.indexOf(")"));
				line = line.replaceAll(token1, swoopModel.shortForm(new URI(token1)));
				line = line.replaceAll(token2, swoopModel.shortForm(new URI(token2)));
			}
			newLog += line + NEWLINE;
		}
		logFile = newLog;
		System.out.print("Cleant file..");
		this.writeLogFile();
	}

	public static void main(String[] args) {
		try {
			testLocalityExtendedURI t = new testLocalityExtendedURI();
			t.runTest();

			//			System.out.println(t.logFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package org.mindswap.swoop.refactoring;

import java.net.URI;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLOntology;

/*
 * Created on May 10, 2004
 */

/**
 * @author Evren Sirin
 */
public class TestLocality {
	public static void main(String[] args) {
		try {
			run(args);
		} catch (Throwable e) {
			System.out.println("********* error **********");
			printStackTrace(e);
		} finally {
			System.out.println("finished");
		}
	}

	public static void printStackTrace(Throwable e) {
		StackTraceElement[] ste = e.getStackTrace();

		System.out.println(e);
		System.out.println("Stack length: " + ste.length);
		if (ste.length > 100) {
			for (int i = 0; i < 10; i++)
				System.out.println("   " + ste[i]);
			System.out.println("   ...");
			for (int i = ste.length - 60; i < ste.length; i++)
				System.out.println("   " + ste[i]);
		} else {
			for (int i = 0; i < ste.length; i++)
				System.out.println("   " + ste[i]);
		}
	}

	public static void run(String[] args) throws Exception {
		URI uri = (args.length > 0) ? new URI(args[0]) : //new URI("http://www.cs.man.ac.uk/~horrocks/OWL/Ontologies/tambis-full.owl");
														 //new URI("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl");
														 //new URI("http://www.purl.org/net/ontology/beer");
														 //new URI("http://www.mpi-sb.mpg.de/~ykazakov/default-GALEN.owl");   
				new URI("http://www.cs.man.ac.uk/~ykazakov/snomedct-20050209.owl");
		System.out.println("Checking Locality " + uri);

		SwoopModel swoopModel = new SwoopModel();

		/* 
		
		File ontFile1 = null; 
		String fileName1 = "C:/ontologies/testLocality/test1.owl";
		
		ontFile1 = new File(fileName1);
		String filePath = ontFile1.toURI().toString();
		URI uri1 = new URI(filePath);
		OWLOntology ontology = swoopModel.addOntology(uri1);
		*/

		OWLOntology ontology = swoopModel.addOntology(uri);
		LocalityChecker checker = new LocalityChecker(ontology);
		if (checker.isLocal(ontology))
			System.out.println("The ontology is Local");
		else {
			System.out.println("The ontology is non-local");
			Set nlocal = checker.getNonLocalAxioms();
			System.out.println("The ontology has" + nlocal.size() + "non-local axioms");

		}
	}
}

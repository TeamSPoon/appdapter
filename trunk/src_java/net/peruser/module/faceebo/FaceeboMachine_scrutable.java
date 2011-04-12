package net.peruser.module.faceebo;



import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


import net.peruser.core.name.Address;

import net.peruser.binding.jena.SPARQL_Utils;

import net.peruser.core.document.Doc;

import net.peruser.binding.dom4j.Dom4jDoc;


import net.peruser.core.environment.Environment;

import net.peruser.core.machine.AbstractMachine;

import org.apache.avalon.framework.parameters.Parameters;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class FaceeboMachine_scrutable extends AbstractMachine {
	private static Log 		theLog = LogFactory.getLog(FaceeboMachine_scrutable.class);
	// See http://excalibur.apache.org/apidocs/org/apache/avalon/framework/parameters/Parameters.html
	private		Parameters		myCrutchParameters;
	
	public void setCrutchParameters(Parameters p) {
		myCrutchParameters = p;
	}
	private String getCrutchParameterString(String name) throws Throwable{
		return myCrutchParameters.getParameter(name);
	}
	/**  
	 * 
	 */
	public void setup(String configPath, Environment env) throws Throwable {
		 super.setup(configPath, env);

	}
	/**
	 *
	 */

	public  Object process(Address instructAddr, Object input) throws Throwable {
		return processDoc(instructAddr, (Doc) input);
	}

		
	public  Doc processDoc(Address instructAddr, Doc input) throws Throwable {
		
		Environment env = getCurrentEnvironment();
		Doc resDoc = null;
		
		String queryFileURL = env.resolveFilePath(getCrutchParameterString("queryFile"));
		String modelURL = env.resolveFilePath(getCrutchParameterString("dataFile"));
		String modelFormat =  getCrutchParameterString("dataFormat");
		String modelBaseURI = getCrutchParameterString("dataBaseURI");
		
		// First, read whatever RDF data is in the input
		Model inputModel = produceModelFromInstructions(input);
		// Now add data from the model referred to by our sitemap params
		FileInputStream	modelInputStream = new FileInputStream(modelURL);
		inputModel.read(modelInputStream, modelBaseURI, modelFormat);
		
		String resultXML = SPARQL_Utils.executeQueryFromFile (queryFileURL, inputModel);
		// System.out.println("resultXML=" + resultXML);
		Document resultDoc4J = DocumentHelper.parseText(resultXML);
		
		resDoc = new Dom4jDoc (resultDoc4J);
		return resDoc;
	}
	
	protected static Model produceModelFromInstructions(Doc instructionDoc) throws Throwable {
		Model instructiveModel = ModelFactory.createDefaultModel();
		Document	d4jDoc = ((Dom4jDoc) instructionDoc).getDom4jDoc();
		Map namespaceMap = new HashMap();
		namespaceMap.put("pmd", "http://www.peruser.net/model_description");
		
		XPath modelDescXPath = d4jDoc.createXPath("//pmd:model_set/pmd:model");
		modelDescXPath.setNamespaceURIs(namespaceMap);
		List results = modelDescXPath.selectNodes(d4jDoc);
		for (Iterator iter = results.iterator(); iter.hasNext(); ) {
			Element modelE = (Element) iter.next();
			String format = modelE.valueOf("@format");
			theLog.info("Found model of format: " + format);
			if (format.equals("RDF/XML")) {
				Element modelRootElement = modelE.element("RDF");
				String modelXML = modelRootElement.asXML();
				theLog.debug("Model dump:\n===========================\n " + modelXML + "\n============================");
				StringReader	mxsr = new StringReader(modelXML);
				String modelBaseURI = null;
				instructiveModel.read(mxsr, modelBaseURI, format);
			}
		}
		return instructiveModel;
	}
}		

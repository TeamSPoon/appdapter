package net.peruser.binding.jena;

import net.peruser.core.name.ParsedPair;
import net.peruser.core.name.Abbreviator;
import net.peruser.core.name.Address;
import net.peruser.core.name.Abbreviator;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.rdf.model.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
public class JenaAbbreviator implements Abbreviator {
	private static Log 		theLog = LogFactory.getLog(JenaAbbreviator.class);
	private	static String	SHORT_FORM_SEPARATOR = ":";
	
	private		PrefixMapping	myJenaPrefixMapping;
	/**
	  */
	public JenaAbbreviator (PrefixMapping jpm) {
		setPrefixMapping(jpm);
	}
	
	/** Impl currently assumes that conf is a JenaModelAwareConfig - TODO fixme

	public Object lookupCompatibleValueAtAddress (Config conf, Address address) throws Throwable {
		JenaModelAwareConfig jmac = (JenaModelAwareConfig) conf;
		JenaAddress	jaddr = (JenaAddress) address;
		return jmac.resolveFrame(jaddr);
	}
	*/

	public  Address makeAddressFromLongForm(String longForm) throws Throwable {
		return new JenaAddress(longForm);
	}
	
	public  String resolveAddressToLongForm(Address a) throws Throwable {
		return a.getLongFormString();
	}

	public Address makeAddressFromShortForm(String shortForm) throws Throwable {
		String longForm = null;
		ParsedPair splitPair = ParsedPair.parsePair(shortForm, SHORT_FORM_SEPARATOR);
		if (splitPair != null) {
			String rawPrefix = splitPair.left;
			String ident = splitPair.right;
			String mappedPrefix = myJenaPrefixMapping.getNsPrefixURI(rawPrefix);
			if (mappedPrefix != null) {
				longForm = mappedPrefix + ident;
			} else {
				throw new Exception("Can't map prefix " + rawPrefix + " found in short-form address " + shortForm);
			}
		} else {
			throw new Exception("Can't parse short-form address " + shortForm);
		}
		theLog.debug("JenaAbbreviator resolved SHORT=" + shortForm + " to LONG=" + longForm);
		return makeAddressFromLongForm(longForm);
	}
	
	public String makeShortFormFromAddress(Address addr) throws Throwable {
		String longForm = addr.getLongFormString();
		// This will return a QName or null
		String qname = myJenaPrefixMapping.qnameFor(longForm);
		// Also see PrefixMapping.shortForm(), which does a best effort and returns unshortened if no prefix found.
		return qname;
	}
	/**
	  */
	public void setPrefixMapping (PrefixMapping jpm) {
		myJenaPrefixMapping = jpm;
	}	
	/*
	public String resolveAddressPair(String prefix, String ident) {
		String result = null;
		if (myPrefixMapping != null) {
			result = myJenaPrefixMapping.expandPrefix(myPrefix + ":" + myIdent);
		}
		return result;
	}
	*/
	
}

package net.peruser.core.handle;


import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

import net.peruser.core.name.Address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**   HandleDirectory implements a runtime directory of uniquely named handles to resources.
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class HandleDirectory { 
	
	private static HandleDirectory		theDefaultDirectory;
	
	private static Log 		theLog = LogFactory.getLog(HandleDirectory.class);	
	
	// Listed in order of attachment
	private	List<Handle>				myHandleList;
	// Keyed by "cute local name" strings.
	private	Map<String,Handle>			myHandlesByCuteName;
	// Keyed by "public name"
	private	Map<Address,Handle>		myHandlesByAddress;

	/**
	 */
	public HandleDirectory() {
		myHandlesByCuteName = new HashMap<String,Handle>();
		myHandlesByAddress = new HashMap<Address,Handle>();
		myHandleList = new ArrayList<Handle>(); 
	}
	
	/** Add a handle to the Directory. <br/> The "cute" and "address" names must both be unique within this kernel.  <br/>
	*/
	public void attachHandle(Handle h) throws Throwable {
		String cute = h.getCuteName();
		Address address = h.getAddress();
		
		if(myHandlesByCuteName.containsKey(cute)) {
			throw new Exception("JenaKernel-appendModel is illegal for already existing cuteName=" + cute);
		}
		if(myHandlesByAddress.containsKey(address)) {
			throw new Exception("JenaKernel-appendModel is illegal for already existing address=" + address);
		}

		myHandlesByCuteName.put(cute, h);
		myHandlesByAddress.put(address, h);
		myHandleList.add(h);
	}
	public void detachHandle(Handle h) throws Throwable {
		String cute = h.getCuteName();
		Address address = h.getAddress();
		
		myHandlesByCuteName.remove(cute);
		myHandlesByAddress.remove(address);
		myHandleList.remove(h);
	}
	
	public int getEntryCount() {
		return myHandleList.size();
	}
	/**
	 *
	 */
	public Handle getHandleForCuteName(String cuteName) {
		return myHandlesByCuteName.get(cuteName);
	}
	
 	/**
	 *
	 */
	public Handle getHandleForAddress(Address address) {	 
		return myHandlesByAddress.get(address);
	}
/**
 */
	public int countHandlesInClass(Class c) {
		List matchingHandleList = listHandlesInClass(c);
		return matchingHandleList.size();		
	}
/**
 */
 	public Iterator iterateHandlesInClass(Class c) {
		List matchingHandleList = listHandlesInClass(c);
		return matchingHandleList.iterator();
	}
/**
 */
 	public List listHandlesInClass(Class c) {
		// Make this a more efficient "filtering iterator" if needed.
		List matchingHandleList = new ArrayList();
		for (Handle h : myHandleList) {
			if (c.isAssignableFrom(h.getClass())) {
				matchingHandleList.add(h);
			} 
		}
		return matchingHandleList;
 	}
 
	/** Prints our registered handles, in order */
	
	public void dumpHandleList() throws Throwable {
		for (Handle h : myHandleList) {
			h.dumpDebug();
		}		
	}
	/**  Set the directory of last/first resort.
	  *
	  */
	public static void setDefaultDirectory(HandleDirectory hd) throws Throwable {
		theDefaultDirectory = hd;
	}
	/**  Get the directory of last/first resort.  If no default directory exists, an empty one is created and returned. 
	  *
	  */
	public static HandleDirectory getDefaultDirectory() throws Throwable {
		if (theDefaultDirectory == null) {
			theDefaultDirectory = new HandleDirectory();
		}
		return theDefaultDirectory;
	}
	
}


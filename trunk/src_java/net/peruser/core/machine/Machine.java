package net.peruser.core.machine;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;

// import net.peruser.core.document.Doc;


/**
 * A Machine is a configurable, stateful document processor.   It may control access to certain resources.
 * <br/>
 * Machines are the entry point to the Peruser system proper.  Everything that occurs outside of machines
 * is merely connection and transportation of data.  Decision making and application-specific processing is
 * all encapsulated within Machines.  Machines typically delegate most or all of this decision making to
 * other objects, but it is possible to write a standalone machine, if needed.
 * <br/>
 * A typical request processing application will instantiate one or more Machines on startup, 
 * calling setup on each machine, and then call process() once on some machine for each 
 * input request.  
 * <br/>
 * Concurrency issues and threading rules are left up to the implementation, but in typical unoptimized
 * usage, all methods on Machine will be synchronized.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface Machine {
	/**  
	 * Make this machine ready to process. 
	 * <br/>
	 * May be called multiple times, with implementation-defined effect.
	 *
	 * @param configPath  Points within some assumed context accessible through env.
	 * @param env         Provides all context needed for machine to connect to resources.
	 */
	public abstract void setup(String configPath, Environment env) throws Throwable;
	/**
	 * Process input through this machine and produce output.
	 * <br/>
	 *
	 * @param input  	An object containing input, some of which may be configuration override information 
	 * for this machine.  <BR/>These overrides may or may not persist in the machine beyond this process call.
	 * @return          An object containing output, typically containing application specific results, but
	 *                  which may also include some diagnostic information.
	 */
	 
	public abstract Object process(Address instructionRoot, Object input) throws Throwable;

}		

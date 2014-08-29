package at.ac.uibk.dps.biohadoop.algorithm;

import java.util.Map;

import at.ac.uibk.dps.biohadoop.solver.SolverId;

/**
 * An <tt>Algorithm</tt> defines e piece of work, that an algorithm author wants
 * to run on Biohadoop. Its only method <tt>compute</tt> is called by Biohadoop
 * after the initialization is done. Inside this method, pieces of work may be
 * distributed to workers
 * 
 * @author Christian Gapp
 */
public interface Algorithm {

	/**
	 * This method is called by Biohadoop after the initialisation. Inside the
	 * method, an algorithm writer may define the algorithm, that should be run
	 * by Biohadoop
	 * 
	 * @param solverId
	 *            represents the unique Id of this algorithm inside this running
	 *            Biohadoop instance. It can be used by the code, for example
	 *            when logging information
	 * @param properties
	 *            contains the configuration for this algorithm, as defined in
	 *            the configuration file
	 * @throws AlgorithmException
	 *             if there was some error during the execution of the algorithm
	 */
	public void compute(SolverId solverId, Map<String, String> properties)
			throws AlgorithmException;

}

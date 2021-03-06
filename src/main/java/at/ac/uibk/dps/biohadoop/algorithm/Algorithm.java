package at.ac.uibk.dps.biohadoop.algorithm;

import java.util.Map;

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
	 * This method is called by Biohadoop after the initialization. The
	 * algorithm should be defined inside this method.
	 * 
	 * @param algorithmId
	 *            represents the unique Id of this algorithm inside this running
	 *            Biohadoop instance
	 * @param properties
	 *            contains the configuration for this algorithm, as defined in
	 *            the configuration file
	 * @throws AlgorithmException
	 *             if there was was an error during the execution of the
	 *             algorithm
	 */
	public void run(AlgorithmId algorithmId, Map<String, String> properties)
			throws AlgorithmException;

}

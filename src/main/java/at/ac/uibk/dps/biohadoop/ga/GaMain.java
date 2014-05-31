package at.ac.uibk.dps.biohadoop.ga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.Application;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.ga.algorithm.FileInput;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Tsp;

public class GaMain {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaMain.class);
	
	private GaMain() {
	}
	
	public static void main(String[] args) {
		FileInput fileInput = new FileInput();
		try {
			Application application = new Application("GA-" + Thread.currentThread().getName());
			ApplicationManager applicationManager = ApplicationManager.getInstance();
			final ApplicationId applicationId = applicationManager.addApplication(application);
			
			Tsp tsp = fileInput.readFile("/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp");
			DistancesGlobal.setDistances(tsp.getDistances());
			LOG.info(tsp.toString());
			Ga ga = new Ga(applicationId);
			int[] path = ga.ga(tsp, 10, 10000);
			
			checkPathValid(path);
		} catch (IOException e) {
			LOG.error("Exception while running GaMain", e);
		} catch (InterruptedException e) {
			LOG.error("Exception while running GaMain", e);
		}
	}
	
	private static void checkPathValid(int[] path) {
		List<Integer> res = new ArrayList<Integer>();
		
		for (int p : path) {
			res.add(p);
		}
		
		Collections.sort(res);
		
		for (int i = 0; i < path.length; i++) {
			if (res.get(i) != i) {
				LOG.error("!!!! PATH NOT VALID !!!!");
				return;
			}
		}
		
		LOG.info("Path is valid");
		
	}
}

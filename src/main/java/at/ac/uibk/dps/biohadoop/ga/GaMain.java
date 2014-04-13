package at.ac.uibk.dps.biohadoop.ga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.FileInput;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Tsp;

public class GaMain {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaMain.class);
	
	private GaMain() {
	}
	
	public static void main(String[] args) {
		FileInput fileInput = new FileInput();
		try {
			Tsp tsp = fileInput.readFile("/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp");
			DistancesGlobal.setDistances(tsp.getDistances());
			LOGGER.info(tsp.toString());
			Ga ga = new Ga();
			int[] path = ga.ga(tsp, 10, 10000);
			
			checkPathValid(path);
		} catch (IOException e) {
			LOGGER.error("Exception while running GaMain", e);
		} catch (InterruptedException e) {
			LOGGER.error("Exception while running GaMain", e);
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
				LOGGER.error("!!!! PATH NOT VALID !!!!");
				return;
			}
		}
		
		LOGGER.info("Path is valid");
		
	}
}

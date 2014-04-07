package at.ac.uibk.dps.biohadoop.ga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GaMain {

	public static void main(String[] args) {
		FileInput fileInput = new FileInput();
		try {
			Tsp tsp = fileInput.readFile("/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp");
			System.out.println(tsp.toString());
			Ga ga = new Ga();
			int[] path = ga.ga(tsp, 100, 1000000);
			
			checkPathValid(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				System.out.println("!!!! PATH NOT VALID !!!!");
				return;
			}
		}
		
		System.out.println("Path is valid");
		
	}
}

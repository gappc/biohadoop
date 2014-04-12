package at.ac.uibk.dps.biohadoop.job;

import java.util.Random;

public class IdGenerator {

	private static final Random rand = new Random();
	
	public static long getId() {
		return rand.nextLong();
	}
}

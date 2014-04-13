package at.ac.uibk.dps.biohadoop.job;

import java.util.Random;

public class IdGenerator {

	private static final Random RAND = new Random();
	
	private IdGenerator() {
	}
	
	public static long getId() {
		return RAND.nextLong();
	}
}

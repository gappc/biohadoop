package at.ac.uibk.dps.biohadoop.standalone;

import java.util.Random;

public class InjectionBean {

	private int random = new Random().nextInt();
	
	public int getRandom() {
		return random;
	}

	public void setRandom(int random) {
		this.random = random;
	}
	
	
}

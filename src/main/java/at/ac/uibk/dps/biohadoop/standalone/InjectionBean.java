package at.ac.uibk.dps.biohadoop.standalone;

import java.util.Random;

public class InjectionBean {

	public int getRandom() {
		return new Random().nextInt();
	}
}

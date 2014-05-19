package at.ac.uibk.dps.biohadoop.nsgaii.master.local;

import at.ac.uibk.dps.biohadoop.nsgaii.worker.LocalNsgaIIWorker;

public class NsgaIILocalResource {

	public NsgaIILocalResource() {
		new Thread(new LocalNsgaIIWorker(),
				LocalNsgaIIWorker.class.getSimpleName()).start();
	}

}

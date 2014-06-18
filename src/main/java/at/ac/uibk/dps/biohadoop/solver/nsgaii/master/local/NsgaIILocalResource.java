package at.ac.uibk.dps.biohadoop.solver.nsgaii.master.local;

import at.ac.uibk.dps.biohadoop.solver.nsgaii.worker.LocalNsgaIIWorker;

public class NsgaIILocalResource {

	public NsgaIILocalResource() {
		new Thread(new LocalNsgaIIWorker(),
				LocalNsgaIIWorker.class.getSimpleName()).start();
	}

}

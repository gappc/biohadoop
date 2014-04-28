package at.ac.uibk.dps.biohadoop.ga.master.local;

import at.ac.uibk.dps.biohadoop.ga.worker.LocalGaWorker;

public class GaLocalResource {

	public GaLocalResource() {
		new Thread(new LocalGaWorker(), LocalGaWorker.class.getSimpleName())
				.start();
	}

}

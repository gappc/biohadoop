package at.ac.uibk.dps.biohadoop.moead.master.local;

import at.ac.uibk.dps.biohadoop.moead.worker.LocalMoeadWorker;

public class MoeadLocalResource {

	public MoeadLocalResource() {
		new Thread(new LocalMoeadWorker(), LocalMoeadWorker.class.getSimpleName())
				.start();
	}

}

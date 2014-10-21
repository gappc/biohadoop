package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.KryoWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.ConnectionRefusedException;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerException;

public class TestWorker {

	public static void main(String[] args) throws AdapterException, WorkerException, ConnectionRefusedException {
//		WebSocketWorker worker = new WebSocketWorker();
		KryoWorker worker = new KryoWorker();
		worker.start(args[0], Integer.parseInt(args[1]));
	}

}

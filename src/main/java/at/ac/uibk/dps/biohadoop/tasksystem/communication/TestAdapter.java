package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.WebSocketAdapter;

public class TestAdapter {

	public static void main(String[] args) throws AdapterException {
		WebSocketAdapter adapter = new WebSocketAdapter();
//		KryoAdapter adapter = new KryoAdapter();
		adapter.start();
	}

}
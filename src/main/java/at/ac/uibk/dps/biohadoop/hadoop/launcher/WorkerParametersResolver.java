package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;

import at.ac.uibk.dps.biohadoop.communication.worker.KryoWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.RestWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.SocketWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.SuperWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WebSocketWorker;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class WorkerParametersResolver {

	public static String getKryoWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(KryoWorker.class);
		if (workerAnnotation != null) {
			String prefix = ((KryoWorker) workerAnnotation).master()
					.getCanonicalName();
			String hostname = Environment.getPrefixed(prefix,
					Environment.KRYO_SOCKET_HOST);
			String port = Environment.getPrefixed(prefix,
					Environment.KRYO_SOCKET_PORT);
			return hostname + " " + port;
		}
		return null;
	}

	public static String getLocalWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(LocalWorker.class);
		if (workerAnnotation != null) {
			return "EMPTY";
		}
		return null;
	}

	public static String getRestWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(RestWorker.class);
		if (workerAnnotation != null) {
			return Environment.get(Environment.HTTP_HOST) + " "
					+ Environment.get(Environment.HTTP_PORT);
		}
		return null;
	}

	public static String getSocketWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(SocketWorker.class);
		if (workerAnnotation != null) {
			String prefix = ((SocketWorker) workerAnnotation)
					.master().getCanonicalName();
			String hostname = Environment.getPrefixed(prefix,
					Environment.SOCKET_HOST);
			String port = Environment.getPrefixed(prefix,
					Environment.SOCKET_PORT);
			return hostname + " " + port;
		}
		return null;
	}

	public static String getWebSocketWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(WebSocketWorker.class);
		if (workerAnnotation != null) {
			return Environment.get(Environment.HTTP_HOST) + " "
					+ Environment.get(Environment.HTTP_PORT);
		}
		return null;
	}
}

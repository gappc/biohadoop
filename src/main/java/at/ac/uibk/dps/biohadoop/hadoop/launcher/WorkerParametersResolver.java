package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;

import at.ac.uibk.dps.biohadoop.communication.worker.KryoWorkerAnnotation;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorkerAnnotation;
import at.ac.uibk.dps.biohadoop.communication.worker.RestWorkerAnnotation;
import at.ac.uibk.dps.biohadoop.communication.worker.SocketWorkerAnnotation;
import at.ac.uibk.dps.biohadoop.communication.worker.SuperWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WebSocketWorkerAnnotation;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class WorkerParametersResolver {

	public static String getKryoWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(KryoWorkerAnnotation.class);
		if (workerAnnotation != null) {
			String prefix = ((KryoWorkerAnnotation) workerAnnotation).master()
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
				.getAnnotation(LocalWorkerAnnotation.class);
		if (workerAnnotation != null) {
			return "EMPTY";
		}
		return null;
	}

	public static String getRestWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(RestWorkerAnnotation.class);
		if (workerAnnotation != null) {
			return Environment.get(Environment.HTTP_HOST) + " "
					+ Environment.get(Environment.HTTP_PORT);
		}
		return null;
	}

	public static String getSocketWorkerParameters(
			Class<? extends SuperWorker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(SocketWorkerAnnotation.class);
		if (workerAnnotation != null) {
			String prefix = ((SocketWorkerAnnotation) workerAnnotation)
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
				.getAnnotation(WebSocketWorkerAnnotation.class);
		if (workerAnnotation != null) {
			return Environment.get(Environment.HTTP_HOST) + " "
					+ Environment.get(Environment.HTTP_PORT);
		}
		return null;
	}
}

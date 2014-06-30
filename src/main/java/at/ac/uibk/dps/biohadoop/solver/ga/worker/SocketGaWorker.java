package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.connection.WorkerConnection;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaEndpointConfig;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class SocketGaWorker implements WorkerConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketGaWorker.class);

	private static String className = Helper.getClassname(SocketGaWorker.class);
	private double[][] distances;
	private int logSteps = 1000;

	public static void main(String[] args) throws Exception {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		 
        URL[] urls = ((URLClassLoader)cl).getURLs();
 
        StringBuilder sb = new StringBuilder();
        for(URL url: urls){
        	sb.append(url.getFile()).append(":");
        }
        LOG.info(sb.toString());
        
		LOG.info("############# {} started ##############",
				SocketGaWorker.class.getSimpleName());
		LOG.debug("args.length: {}", args.length);
		for (String s : args) {
			LOG.debug(s);
		}

		String host = args[0];
		int port = Integer.valueOf(args[1]);

		LOG.info("############# {} client calls master at: {}:{} #############",
				className, host, port);
		new SocketGaWorker(host, port);
		LOG.info("############# {} stopped #############", className);
	}

	public SocketGaWorker() {
	}
	
	@Override
	public String getWorkerParameters() {
		GaEndpointConfig masterConfig = new GaEndpointConfig();
		String hostname = Environment.getPrefixed(masterConfig.getPrefix(), Environment.SOCKET_HOST);
		String port = Environment.getPrefixed(masterConfig.getPrefix(), Environment.SOCKET_PORT);
		return hostname + " " + port;
	}

	// TODO remove "throws" and add proper error handling
	public SocketGaWorker(String hostname, int port) throws IOException,
			InterruptedException, EncodeException, ClassNotFoundException {
		Socket clientSocket = new Socket(hostname, port);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		doRegistration(os, hostname, port);
		handleRegistrationResponse(is, os);
		doWorkInit(os);
		handleWork(is, os);

		is.close();
		os.close();
		clientSocket.close();
	}

	private void doRegistration(ObjectOutputStream os, String hostname, int port)
			throws IOException {
		LOG.debug("{} starting registration to {}:{}", className, hostname,
				port);
		Message<Object> message = new Message<Object>(
				MessageType.REGISTRATION_REQUEST, null);
		send(os, message);
	}

	private void handleRegistrationResponse(ObjectInputStream is,
			ObjectOutputStream os) throws ClassNotFoundException, IOException {
		Message<Double[][]> message = receive(is);
		LOG.info("{} registration successful", className);

		Double[][] inputDistances = message.getPayload().getData();
		convertDistances(inputDistances);
	}

	private void doWorkInit(ObjectOutputStream os) throws IOException {
		LOG.debug("{} starting work");
		Message<Object> message = new Message<Object>(
				MessageType.WORK_INIT_REQUEST, null);
		send(os, message);
	}

	private void handleWork(ObjectInputStream is, ObjectOutputStream os)
			throws IOException, ClassNotFoundException {
		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		int counter = 0;
		while (true) {
			performanceLogger.step(LOG);
			counter++;
			if (counter % 1000 == 0) {
				os.reset();
				counter = 0;
			}

			Message<int[]> inputMessage = receive(is);

			LOG.debug("{} WORK_RESPONSE", className);

			if (inputMessage.getType() == MessageType.SHUTDOWN) {
				break;
			}

			Task<int[]> inputTask = inputMessage.getPayload();
			Task<Double> response = computeResult(inputTask);

			Message<Double> message = new Message<Double>(
					MessageType.WORK_REQUEST, response);
			send(os, message);
		}
	}

	private void convertDistances(Double[][] inputDistances) {
		int length1 = inputDistances.length;
		int length2 = length1 != 0 ? inputDistances[0].length : 0;
		distances = new double[length1][length2];
		for (int i = 0; i < length1; i++) {
			for (int j = 0; j < length2; j++) {
				distances[i][j] = inputDistances[i][j];
			}
		}
	}

	private Task<Double> computeResult(Task<int[]> task) {
		double fitness = GaFitness.computeFitness(distances, task.getData());
		Task<Double> response = new Task<Double>(task.getTaskId(), fitness);
		return response;
	}

	@SuppressWarnings("unchecked")
	private <T> Message<T> receive(ObjectInputStream is) throws IOException,
			ClassNotFoundException {
		return (Message<T>) is.readUnshared();
	}

	private void send(ObjectOutputStream os, Message<?> message)
			throws IOException {
		os.writeUnshared(message);
		os.flush();
	}

}
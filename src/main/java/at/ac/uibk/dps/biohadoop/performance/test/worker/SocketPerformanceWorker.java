package at.ac.uibk.dps.biohadoop.performance.test.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.performance.test.JobRequest;
import at.ac.uibk.dps.biohadoop.performance.test.JobResponse;
import at.ac.uibk.dps.biohadoop.performance.test.config.PerformanceConfig;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SocketPerformanceWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SocketPerformanceWorker.class);

	private int logSteps = 10000;

	public static void main(String[] args) throws Exception {
		LOGGER.info("############# {} started ##############",
				SocketPerformanceWorker.class.getSimpleName());
		LOGGER.info("args.length: {}", args.length);
		for (String s : args) {
			LOGGER.info(s);
		}

		String masterHostname = args[0];

		LOGGER.info("############# {} client calls master at: {} #############",
				SocketPerformanceWorker.class.getSimpleName(), masterHostname);
		
		PerformanceConfig config = getPerformanceConfig(args[1]);
		
		new SocketPerformanceWorker(masterHostname, 30001, config);
	}

	public SocketPerformanceWorker() {
	}

//	TODO use try-with-resource
	public SocketPerformanceWorker(String hostname, int port, PerformanceConfig config)
			throws DeploymentException, IOException, InterruptedException,
			EncodeException, ClassNotFoundException {
		
		Socket clientSocket = new Socket(hostname, port);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		JobResponse response = new JobResponse();
		os.writeObject(null);
		os.flush();
		
		long startTime = System.currentTimeMillis();
		int counter = 0;
		while (true) {
			if (counter % logSteps == 0) {
				long endTime = System.currentTimeMillis();
				LOGGER.info("{}ms for last {} computations, got {} from master",
						endTime - startTime, logSteps, response.getJob());
				startTime = System.currentTimeMillis();
				os.reset();
			}
			counter++;

			response = (JobResponse)is.readObject();
			
			if (response.getState() == JobResponse.State.DIE) {
				os.flush();
				break;
			}

			JobRequest request = new JobRequest(JobRequest.State.JOBDONE, (long) counter);
			os.writeObject(request);
			os.flush();
			Thread.sleep(config.getAlgorithmConfig().getClientSleepMillis());
		}
		is.close();
		os.close();
		clientSocket.close();

		LOGGER.info("############# {} stopped #############",
				SocketPerformanceWorker.class.getSimpleName());
	}
	
	private static PerformanceConfig getPerformanceConfig(String string) throws JsonParseException, JsonMappingException, IOException {
		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(HdfsUtil.openFile(yarnConfiguration, string), PerformanceConfig.class);
	}
}

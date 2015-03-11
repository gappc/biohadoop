package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.profile.Profilers;
import at.ac.uibk.dps.biohadoop.profile.cpu.CPUProfiler;
import at.ac.uibk.dps.biohadoop.profile.net.NetProfiler;

public class WorkerStarter {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerStarter.class);

	public static void main(String[] args) {
		CPUProfiler cpuProfiler = Profilers.runCPUProfiler();
		NetProfiler netProfiler = Profilers.runNetProfiler();
		
		LOG.info("############# {} starting ##############", args[0]);

		LOG.info("Program arguments: (args.length: {})", args.length);
		for (int i = 0; i < args.length; i++) {
			LOG.info("arg[{}] = {}", i, args[i]);
		}

		try {
			@SuppressWarnings("unchecked")
			Class<? extends Worker> workerClass = (Class<? extends Worker>) Class
					.forName(args[0]);
			Worker worker = workerClass.newInstance();
			worker.start(args[1], Integer.parseInt(args[2]));
		} catch(ConnectionRefusedException e) {
			LOG.error(
					"Error while connecting to Endpoint for Worker {}, exiting with status code 2",
					args[0], e);
			cpuProfiler.logCPUData();
			netProfiler.logNetData();
			System.exit(2);
		} catch (Exception e) {
			LOG.error(
					"Error while runnig Worker {}, exiting with status code 1",
					args[0], e);
			cpuProfiler.logCPUData();
			netProfiler.logNetData();
			System.exit(1);
		}

		cpuProfiler.logCPUData();
		netProfiler.logNetData();
		LOG.info("Worker finished");
		System.exit(0);
	}
}

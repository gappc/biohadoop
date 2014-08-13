package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.communication.worker.DefaultLocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerException;

public class DefaultLocalMasterEndpoint implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultLocalMasterEndpoint.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private DefaultLocalWorker<?, ?, ?> localWorker;

	@Override
	public void configure(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws MasterException {
		localWorker = new DefaultLocalWorker<>();
		try {
			String remoteExecutableClassName = "";
			if (remoteExecutable != null) {
				remoteExecutableClassName = remoteExecutable.getCanonicalName();
			}
			localWorker.configure(new String[] {
					DefaultLocalWorker.class.getCanonicalName(),
					remoteExecutableClassName, "", "0" });
		} catch (WorkerException e) {
			throw new MasterException("Could not configure local worker", e);
		}
	}

	@Override
	public void start() throws MasterException {
		executorService.submit(localWorker);
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		localWorker.stop();
		executorService.shutdown();
	}

}

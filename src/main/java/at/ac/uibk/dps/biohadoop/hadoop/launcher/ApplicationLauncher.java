package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.Application;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationState;
import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceManager;

public class ApplicationLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(ApplicationLauncher.class);

	private ApplicationLauncher() {
	}

	public static List<Future<ApplicationId>> launchApplication(
			BiohadoopConfiguration biohadoopConfig) {
		ExecutorService cachedPoolExecutor = Executors.newCachedThreadPool();

		List<Future<ApplicationId>> applications = new ArrayList<>();
		for (ApplicationConfiguration applicationConfig : biohadoopConfig
				.getApplicationConfigs()) {
			Callable<ApplicationId> callable = prepareApplication(applicationConfig);
			Future<ApplicationId> application = cachedPoolExecutor
					.submit(callable);
			applications.add(application);
		}

		// shutdown() can be called here safely, as it just ignores new threads
		// but completes the old ones
		cachedPoolExecutor.shutdown();

		return applications;
	}

	private static Callable<ApplicationId> prepareApplication(
			final ApplicationConfiguration applicationConfig) {
		final Application application = new Application(applicationConfig);
		application.registerApplicationHandler(PersistenceManager.getInstance());
		
		final ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		final ApplicationId applicationId = applicationManager
				.addApplication(application);

		Callable<ApplicationId> callable = generateCallable(applicationId,
				applicationConfig);
		return callable;
	}

	private static Callable<ApplicationId> generateCallable(
			final ApplicationId applicationId,
			final ApplicationConfiguration applicationConfig) {
		return new Callable<ApplicationId>() {

			@Override
			public ApplicationId call() throws Exception {
				LOG.info("Initialising application {} with applicationId {}",
						applicationConfig.getName(), applicationId);
				ApplicationManager.getInstance().setApplicationState(
						applicationId, ApplicationState.NEW);

				Object parameter = applicationConfig
						.getAlgorithmConfiguration().buildParameters();
				Algorithm<?, ?> algorithm = applicationConfig.getAlgorithm()
						.newInstance();
				((Algorithm<?, Object>) algorithm).compute(applicationId,
						parameter);

				LOG.info("Finished application {} with applicationId {}",
						applicationConfig.getName(), applicationId);
				ApplicationManager.getInstance().setApplicationState(
						applicationId, ApplicationState.FINISHED);
				return applicationId;
			}
		};
	}
}

package at.ac.uibk.dps.biohadoop.applicationmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.torename.ObjectCloner;

public class ApplicationManager {

	private static final ApplicationManager APPLICATION_MANAGER = new ApplicationManager();
	private Map<ApplicationId, Application> applications = new ConcurrentHashMap<>();
	// TODO use on all handlers the CopyOnWriteArrayList like here
	private List<ShutdownHandler> shutdownHandlers = new CopyOnWriteArrayList<>();
	private AtomicInteger counter = new AtomicInteger();

	private ApplicationManager() {
	}

	public static ApplicationManager getInstance() {
		return ApplicationManager.APPLICATION_MANAGER;
	}

	public ApplicationId addApplication(final Application application) {
		ApplicationId applicationId = ApplicationId.newInstance();
		applications.put(applicationId, application);
		counter.incrementAndGet();
		return applicationId;
	}

	public float getProgress(final ApplicationId applicationId) {
		Application application = applications.get(applicationId);
		return application.getProgress();
	}

	public float getOverallProgress() {
		float progress = 0;
		int applicationCount = applications.size();
		for (ApplicationId applicationId : applications.keySet()) {
			Application application = applications.get(applicationId);
			progress += application.getProgress() / applicationCount;
		}
		return progress > 1 ? 1 : progress;
	}

	public void setProgress(final ApplicationId applicationId,
			final float progress) {
		Application application = applications.get(applicationId);
		application.setProgress(progress);
	}

	public ApplicationState getApplicationState(
			final ApplicationId applicationId) {
		Application application = applications.get(applicationId);
		return application.getApplicationState();
	}

	public void setApplicationState(final ApplicationId applicationId,
			ApplicationState applicationState) {
		Application application = applications.get(applicationId);
		application.setApplicationState(applicationState);

		switch (applicationState) {
		case NEW:
			for (ApplicationHandler applicationHandler : application
					.getApplicationHandlers()) {
				applicationHandler.onNew(applicationId);
			}
			break;
		case FINISHED:
			counter.decrementAndGet();
			if (counter.compareAndSet(0, 0)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				invokeShutdownHandlers();
			}
			break;
		default:
			break;
		}
	}

	public void registerShutdownHandler(final ShutdownHandler shutdownHandler) {
		shutdownHandlers.add(shutdownHandler);
	}

	public void setApplicationData(final ApplicationId applicationId,
			final ApplicationData<?> applicationData) {
		Application application = applications.get(applicationId);
		ApplicationData<?> clone = ObjectCloner.deepCopy(applicationData,
				ApplicationData.class);
		application.setApplicationData(clone);
		for (ApplicationHandler applicationHandler : application
				.getApplicationHandlers()) {
			applicationHandler.onDataUpdate(applicationId);
		}
	}
	
	public void updateApplicationData(final ApplicationId applicationId,
			final ApplicationData<?> applicationData) {
		Application application = applications.get(applicationId);
		ApplicationData<?> clone = ObjectCloner.deepCopy(applicationData,
				ApplicationData.class);
		application.setApplicationData(clone);
	}

	public ApplicationData<?> getApplicationData(ApplicationId applicationId) {
		Application application = applications.get(applicationId);
		if (application == null) {
			return null;
		}
		return application.getApplicationData();
	}

	// TODO remove if only needed for DistributionManager.getRemoteApplication()
	public List<ApplicationId> getApplicationsList() {
		return new ArrayList<>(applications.keySet());
	}

	public ApplicationConfiguration getApplicationConfiguration(
			ApplicationId applicationId) {
		Application application = applications.get(applicationId);
		return application.getApplicationConfiguration();
	}

	private void invokeShutdownHandlers() {
		for (ShutdownHandler shutdownHandler : shutdownHandlers) {
			shutdownHandler.shutdown();
		}
		JobManager.getInstance().shutdown();
	}

}

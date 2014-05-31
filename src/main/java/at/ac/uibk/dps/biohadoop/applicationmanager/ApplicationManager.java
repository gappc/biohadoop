package at.ac.uibk.dps.biohadoop.applicationmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationManager {

	private static final ApplicationManager APPLICATION_MANAGER = new ApplicationManager();
	private Map<ApplicationId, Application> applications = new ConcurrentHashMap<>();
	private List<ShutdownHandler> shutdownHandlers = new ArrayList<>();
	private AtomicInteger counter = new AtomicInteger();

	public static ApplicationManager getInstance() {
		return ApplicationManager.APPLICATION_MANAGER;
	}

	public ApplicationId addApplication(Application application) {
		ApplicationId applicationId = ApplicationId.newInstance();
		applications.put(applicationId, application);
		counter.incrementAndGet();
		return applicationId;
	}

	public float getProgress(ApplicationId applicationId) {
		Application application = applications.get(applicationId);
		return application.getProgress();
	}

	public void setProgress(ApplicationId applicationId, float progress) {
		Application application = applications.get(applicationId);
		application.setProgress(progress);
	}

	public ApplicationState getApplicationState(ApplicationId applicationId) {
		Application application = applications.get(applicationId);
		return application.getApplicationState();
	}

	public void setApplicationState(ApplicationId applicationId,
			ApplicationState applicationState) {
		Application application = applications.get(applicationId);
		application.setApplicationState(applicationState);
		if (applicationState == ApplicationState.FINISHED) {
			counter.decrementAndGet();
			if (counter.compareAndSet(0, 0)) {
				invokeShutdownHandlers();
			}
		}
	}
	
	public void registerShutdownHandler(ShutdownHandler shutdownHandler) {
		shutdownHandlers.add(shutdownHandler);
	}
	
	private void invokeShutdownHandlers() {
		for (ShutdownHandler shutdownHandler : shutdownHandlers) {
			shutdownHandler.shutdown();
		}
	}

}

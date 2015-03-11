package at.ac.uibk.dps.biohadoop.tasksystem.queue;

/**
 * Defines methods to get the default task broker. It is strongly suggested to
 * use {@link #getTaskBroker()} if the task broker is needed inside Biohadoop.
 * 
 * @author Christian Gapp
 *
 */
public class TaskBrokerService {

	private static final TaskBroker QUEUE = new TaskBroker();

	private TaskBrokerService() {
		// Nothing to do
	}

	/**
	 * Returns the default task broker
	 * 
	 * @return the {@link TaskBroker}
	 */
	public static TaskBroker getTaskBroker() {
		return QUEUE;
	}

}

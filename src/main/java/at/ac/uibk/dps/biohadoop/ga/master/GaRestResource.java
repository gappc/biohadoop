package at.ac.uibk.dps.biohadoop.ga.master;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaRestResource {

	private JobManager jobManager = JobManager.getInstance();

//	@GET
//	@Path("init")
//	public double[][] init() throws InterruptedException {
//		return DistancesGlobal.getDistances();
//	}

	@POST
//	@Path("work")
	public Message writeResult(Message message) throws InterruptedException {
		MessageType messageType = null;
		Object response = null;
		if (message.getType() == MessageType.REGISTRATION_REQUEST) {
			messageType = MessageType.REGISTRATION_RESPONSE;
			response = null;
		}
		if (message.getType() == MessageType.WORK_INIT_REQUEST) {
			Task task = (Task) jobManager.getTaskForExecution(Ga.GA_WORK_QUEUE);
			messageType = MessageType.WORK_INIT_RESPONSE;
			response = new Object[] { DistancesGlobal.getDistances(), task };
		}
		if (message.getType() == MessageType.WORK_REQUEST) {
			ObjectMapper mapper = new ObjectMapper();
			GaResult gaResult = mapper.convertValue(message.getData(),
					GaResult.class);

			jobManager.writeResult(Ga.GA_RESULT_STORE, gaResult);
			
			if (jobManager.isStop()) {
				messageType = MessageType.SHUTDOWN;
			}
			else {
				messageType = MessageType.WORK_RESPONSE;
				response = jobManager.getTaskForExecution(Ga.GA_WORK_QUEUE);
				if (response instanceof StopTask) {
					messageType = MessageType.SHUTDOWN;
					response = null;
				}
			}
		}

		return new Message(messageType, response);
	}
}

package at.ac.uibk.dps.biohadoop.rs;

import java.util.concurrent.BlockingQueue;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.Ga;
import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;
import at.ac.uibk.dps.biohadoop.queue.MessagingFactory;
import at.ac.uibk.dps.biohadoop.queue.ResultStore;
import at.ac.uibk.dps.biohadoop.torename.DistancesGlobal;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaResource {
	
	private BlockingQueue<Object> workQueue = MessagingFactory.getWorkQueue(Ga.GA_WORK_QUEUE);
	private ResultStore resultStore = MessagingFactory.getResultStore(Ga.GA_RESULT_STORE);

	@GET
	@Path("init")
	public double[][] init() throws InterruptedException {
		return DistancesGlobal.getDistances();
	}
	
//	@GET
//	@Path("work")
//	public GaTask getWork() throws InterruptedException {
//		return (GaTask)workQueue.take();
//	}
	
	@POST
	@Path("work")
	public GaTask writeResult(GaResult result) throws InterruptedException {
		if (result.getSlot() != -1) {
			resultStore.store(result.getSlot(), result.getResult());
		}
		return (GaTask)workQueue.take();
	}
}

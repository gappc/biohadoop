package at.ac.uibk.dps.biohadoop.ga.master;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.torename.DistancesGlobal;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaRestResource {
	
	private JobManager jobManager = JobManager.getInstance();

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
			jobManager.writeResult(Ga.GA_RESULT_STORE, result);
		}
		return (GaTask)jobManager.getTaskForExecution(Ga.GA_WORK_QUEUE);
	}
}

package at.ac.uibk.dps.biohadoop.rs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;
import at.ac.uibk.dps.biohadoop.queue.ResultQueue;
import at.ac.uibk.dps.biohadoop.queue.SimpleQueue;
import at.ac.uibk.dps.biohadoop.torename.DistancesGlobal;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaResource {

	@GET
	@Path("init")
	public double[][] init() throws InterruptedException {
		return DistancesGlobal.getDistances();
	}
	
	@GET
	@Path("work")
	public GaTask getWork() throws InterruptedException {
		return (GaTask)SimpleQueue.take();
	}
	
	@POST
	public void writeResult(GaResult result) {
		System.out.println("Got result: " + result);
		ResultQueue.setResult(result.getSlot(), result.getResult());
	}
}

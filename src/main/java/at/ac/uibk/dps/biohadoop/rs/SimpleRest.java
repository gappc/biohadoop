package at.ac.uibk.dps.biohadoop.rs;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.entity.Task;
import at.ac.uibk.dps.biohadoop.standalone.InjectionBean;

@Path("/simple")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SimpleRest {
	
	@Inject
	InjectionBean bean;
	
	private int counter = 10;

//	@GET
//	public String getDate() {
//		return new Date().toString();
//	}
	
	@GET
	public Task getTask() {
		Task task = new Task();
		task.setId(counter > 0 ? counter-- : 0);
		return task;
	}
}

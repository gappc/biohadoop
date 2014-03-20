package at.ac.uibk.dps.biohadoop.rs;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/simple")
public class SimpleRest {

	@GET
	public String getDate() {
		return new Date().toString();
	}
}

package at.ac.uibk.dps.biohadoop.distributionmanager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;

@Path("/distribution")
@Produces(MediaType.APPLICATION_JSON)
public class DistributionResource {

	@GET
	@Path("{applicationId}")
	public Response getApplicationData(
			@PathParam("applicationId") ApplicationId applicationId) {
		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationData<?> applicationData = applicationManager
				.getApplicationData(applicationId);
		if (applicationData == null) {
			return Response.noContent().build();
		} else {
			return Response.ok(applicationData).build();
		}
	}
}

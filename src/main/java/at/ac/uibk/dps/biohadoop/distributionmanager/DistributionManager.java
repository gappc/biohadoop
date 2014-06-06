package at.ac.uibk.dps.biohadoop.distributionmanager;

import java.util.List;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;


public class DistributionManager {

	private static DistributionManager DISTRIBUTION_MANAGER = new DistributionManager();

	private String baseUrl = "http://kleintroppl:30000/rs/application/";
	
	private DistributionManager() {
	}

	public static DistributionManager getInstance() {
		return DistributionManager.DISTRIBUTION_MANAGER;
	}
	
	@SuppressWarnings("unchecked")
	public <T>ApplicationData<T> getRemoteApplicationData() {
//		TODO get remoteUrl from different source e.g. Apache ZooKeeper
		List<ApplicationId> applicationIds = ApplicationManager.getInstance().getApplicationsList();
		int pos = new Random().nextInt(applicationIds.size());
		String remoteUrl = baseUrl +  applicationIds.get(pos);
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(remoteUrl)
				.request(MediaType.APPLICATION_JSON).get();
		return response.readEntity(ApplicationData.class);
	}
}

package at.ac.uibk.dps.biohadoop.standalone;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class TestClient {

	public static void main(String[] args) {
		Client client = Client.create();
		WebResource resource = client.resource("http://localhost:30000/simple");
		String response = resource.accept(
		        MediaType.APPLICATION_JSON_TYPE,
		        MediaType.APPLICATION_XML_TYPE).
		        get(String.class);
		System.out.println(response);
	}
}

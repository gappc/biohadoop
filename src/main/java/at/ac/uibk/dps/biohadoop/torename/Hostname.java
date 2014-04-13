package at.ac.uibk.dps.biohadoop.torename;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Hostname {
	
	private Hostname() {
	}

	public static String getHostname() throws IOException {
		Process process = Runtime.getRuntime().exec("hostname");
		BufferedReader is = new BufferedReader(new 
	             InputStreamReader(process.getInputStream()));
		return is.readLine();
	}
}

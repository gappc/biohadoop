package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BiohadoopConfiguration {

	private String version;
	private List<String> includePaths = new ArrayList<>();
	private List<ApplicationConfiguration> applicationConfigs = new ArrayList<>();
	private Map<String, Integer> workers = new HashMap<>();
	private List<String> endPoints = new ArrayList<>();

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public void setIncludePaths(List<String> includePaths) {
		this.includePaths = includePaths;
	}

	public List<ApplicationConfiguration> getApplicationConfigs() {
		return applicationConfigs;
	}

	public void setApplicationConfigs(List<ApplicationConfiguration> applicationConfigs) {
		this.applicationConfigs = applicationConfigs;
	}

	public Map<String, Integer> getWorkers() {
		return workers;
	}

	public void setWorkers(Map<String, Integer> workers) {
		this.workers = workers;
	}

	public List<String> getEndPoints() {
		return endPoints;
	}

	public void setEndPoints(List<String> endPoints) {
		this.endPoints = endPoints;
	}

	public static BiohadoopConfiguration getBiohadoopConfiguration(
			YarnConfiguration yarnConfiguration, String filename)
			throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(
				HdfsUtil.openFile(yarnConfiguration, filename),
				BiohadoopConfiguration.class);
	}
}

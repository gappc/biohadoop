package at.ac.uibk.dps.biohadoop.torename;

import java.io.IOException;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BiohadoopConfigurationReader {
	
	private final static ObjectMapper objectMapper = new ObjectMapper();

	public static BiohadoopConfiguration readBiohadoopConfiguration(
			YarnConfiguration yarnConfiguration, String filename)
			throws IOException {
		return objectMapper.readValue(
				HdfsUtil.openFile(yarnConfiguration, filename),
				BiohadoopConfiguration.class);
	}
}

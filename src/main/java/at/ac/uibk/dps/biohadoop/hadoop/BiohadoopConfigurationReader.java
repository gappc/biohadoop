package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.IOException;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BiohadoopConfigurationReader {
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private BiohadoopConfigurationReader() {
	}
	
	public static BiohadoopConfiguration readBiohadoopConfiguration(
			YarnConfiguration yarnConfiguration, String filename)
			throws IOException {
		return OBJECT_MAPPER.readValue(
				HdfsUtil.openFile(yarnConfiguration, filename),
				BiohadoopConfiguration.class);
	}
}

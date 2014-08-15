package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BiohadoopConfigurationUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private BiohadoopConfigurationUtil() {
	}

	public static BiohadoopConfiguration read(
			YarnConfiguration yarnConfiguration, String filename)
			throws IOException {
		return OBJECT_MAPPER.readValue(
				HdfsUtil.openFile(yarnConfiguration, filename),
				BiohadoopConfiguration.class);
	}

	public static BiohadoopConfiguration readLocal(String filename)
			throws IOException {
		return OBJECT_MAPPER.readValue(new File(filename),
				BiohadoopConfiguration.class);
	}

	public static void save(YarnConfiguration yarnConfiguration,
			BiohadoopConfiguration biohadoopConfiguration, String path)
			throws IOException {
		try (OutputStream os = HdfsUtil.createFile(yarnConfiguration, path)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
			mapper.writeValue(os, biohadoopConfiguration);
		}
	}

	public static void saveLocal(BiohadoopConfiguration biohadoopConfiguration,
			String path) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		mapper.writeValue(new File(path), biohadoopConfiguration);
	}

}

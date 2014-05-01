package at.ac.uibk.dps.biohadoop.torename;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Launcher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LaunchBuilder {

	private static final Logger LOG = LoggerFactory
			.getLogger(LaunchBuilder.class);

	public static Launcher buildLauncher(YarnConfiguration conf,
			String configFilename) throws LaunchBuilderException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		String launcherClassName = null;
		try {
			JsonNode root = mapper.readTree(HdfsUtil.openFile(conf,
					configFilename));
			JsonNode launcherClass = root.findValue("launcherClass");
			launcherClassName = launcherClass.asText();
			Launcher launcher = (Launcher) Class.forName(launcherClassName)
					.newInstance();

			return launcher;
		} catch (Exception e) {
			LOG.error("Error while using file {} to launch application",
					launcherClassName, e);
			throw new LaunchBuilderException(e);
		}
	}
}

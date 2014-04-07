package at.ac.uibk.dps.biohadoop.torename;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

public class LocalResourceBuilder {

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Configuration configuration;
		private String path;
		private List<String> filenames = new ArrayList<String>();

		public Builder setConfiguration(Configuration configuration) {
			this.configuration = configuration;
			return this;
		}

		public Builder setPath(String path) {
			this.path = path;
			return this;
		}

		public Builder addFile(String filename) {
			filenames.add(filename);
			return this;
		}

		public Map<String, LocalResource> build() throws IOException {
			Map<String, LocalResource> resources = new HashMap<String, LocalResource>();
			for (String filename : filenames) {
				LocalResource resource = Records.newRecord(LocalResource.class);
				Path fsPath = new Path(path + filename);
				FileStatus jarStat = FileSystem.get(configuration)
						.getFileStatus(fsPath);
				resource.setResource(ConverterUtils.getYarnUrlFromPath(fsPath));
				resource.setSize(jarStat.getLen());
				resource.setTimestamp(jarStat.getModificationTime());
				resource.setType(LocalResourceType.FILE);
				resource.setVisibility(LocalResourceVisibility.PUBLIC);
				resources.put(filename, resource);
			}
			return resources;
		}
	}
	
	public static Map<String, LocalResource> getStandardResources(String libPath, Configuration conf) throws IOException {
		Map<String, LocalResource> resources = LocalResourceBuilder.builder()
				.setPath(libPath).setConfiguration(conf)
				.addFile("biohadoop-0.0.1-SNAPSHOT.jar")
				.addFile("async-http-servlet-3.0-3.0.6.Final.jar")
				.addFile("cdi-api-1.1.jar")
				.addFile("httpclient-4.3.3.jar")
				.addFile("httpcore-4.3.2.jar")
				.addFile("jackson-annotations-2.2.1.jar")
				.addFile("jackson-core-2.2.1.jar")
				.addFile("jackson-databind-2.2.1.jar")
				.addFile("jackson-jaxrs-base-2.2.1.jar")
				.addFile("jackson-jaxrs-json-provider-2.2.1.jar")
				.addFile("jackson-module-jaxb-annotations-2.2.1.jar")
				.addFile("javassist-3.12.1.GA.jar")
				.addFile("jaxrs-api-3.0.6.Final.jar")
				.addFile("jboss-annotations-api_1.1_spec-1.0.1.Final.jar")
				.addFile("jboss-annotations-api_1.2_spec-1.0.0.Alpha1.jar")
				.addFile("jboss-classfilewriter-1.0.4.Final.jar")
				.addFile("jboss-el-api_3.0_spec-1.0.0.Alpha1.jar")
				.addFile("jboss-interceptors-api_1.2_spec-1.0.0.Alpha3.jar")
				.addFile("jboss-logging-3.1.3.GA.jar")
				.addFile("jboss-servlet-api_3.1_spec-1.0.0.Final.jar")
				.addFile("jcip-annotations-1.0.jar")
				.addFile("resteasy-cdi-3.0.6.Final.jar")
				.addFile("resteasy-client-3.0.6.Final.jar")
				.addFile("resteasy-jackson2-provider-3.0.6.Final.jar")
				.addFile("resteasy-jaxrs-3.0.6.Final.jar")
				.addFile("resteasy-undertow-3.0.6.Final.jar")
				.addFile("scannotation-1.0.3.jar")
				.addFile("undertow-core-1.0.1.Final.jar")
				.addFile("undertow-servlet-1.0.1.Final.jar")
				.addFile("weld-api-2.1.Final.jar")
				.addFile("weld-core-impl-2.1.2.Final.jar")
				.addFile("weld-se-core-2.1.2.Final.jar")
				.addFile("weld-spi-2.1.Final.jar")
				.addFile("xnio-api-3.2.0.Final.jar")
				.addFile("xnio-nio-3.2.0.Final.jar")
				.build();
		return resources;
	}
}

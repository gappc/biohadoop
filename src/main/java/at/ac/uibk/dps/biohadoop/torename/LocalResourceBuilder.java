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
}

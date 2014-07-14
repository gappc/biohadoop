package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalResourceBuilder {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocalResourceBuilder.class);
	
	private LocalResourceBuilder() {
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Configuration configuration;
		private List<Path> files = new ArrayList<Path>();

		public Builder setConfiguration(Configuration configuration) {
			this.configuration = configuration;
			return this;
		}

		public Builder addFile(Path file) {
			files.add(file);
			return this;
		}

		public Map<String, LocalResource> build() throws IOException {
			Map<String, LocalResource> resources = new HashMap<String, LocalResource>();
			for (Path file : files) {
				String filename = getFilename(file);
				LocalResource resource = Records.newRecord(LocalResource.class);
				FileStatus jarStat = FileSystem.get(configuration)
						.getFileStatus(file);
				resource.setResource(ConverterUtils.getYarnUrlFromPath(file));
				resource.setSize(jarStat.getLen());
				resource.setTimestamp(jarStat.getModificationTime());
				resource.setType(LocalResourceType.FILE);
				resource.setVisibility(LocalResourceVisibility.PUBLIC);
				resources.put(filename, resource);
				LOG.debug("Adding {} as file {}", file, filename);
			}
			return resources;
		}
		
		private String getFilename(Path path) {
			String filename = path.toString();
			return filename.substring(filename.lastIndexOf("/") + 1);
		}
	}
	
	public static Map<String, LocalResource> getStandardResources(String libPath, Configuration conf) throws IOException {
		Builder builder = LocalResourceBuilder.builder().setConfiguration(conf);
		FileSystem fs = FileSystem.get(conf);
		for (RemoteIterator<LocatedFileStatus> it = fs.listFiles(new Path(libPath), false); it.hasNext(); ) {
			Path path = it.next().getPath();
			
			builder = builder.addFile(path);
		}
		return builder.build();
	}
}

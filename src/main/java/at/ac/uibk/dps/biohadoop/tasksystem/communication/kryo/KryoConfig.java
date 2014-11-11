package at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo;

import org.apache.commons.lang.math.NumberUtils;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class KryoConfig {

	public static final String KRYO_BUFFER_SIZE = "KRYO_BUFFER_SIZE";
	public static final String KRYO_MAX_BUFFER_SIZE = "KRYO_MAX_BUFFER_SIZE";
	public static final int KRYO_DEFAULT_BUFFER_SIZE = 1 * 1024;
	public static final int KRYO_DEFAULT_MAX_BUFFER_SIZE = 1 * 1024 * 1024;
	
	public static int getBufferSize() {
		String bufferSizeAsString = Environment.getBiohadoopConfiguration().getGlobalProperties().get(KryoConfig.KRYO_BUFFER_SIZE);
		
		int bufferSize = NumberUtils.toInt(bufferSizeAsString, -1);
		
		return bufferSize == -1 ? KryoConfig.KRYO_DEFAULT_BUFFER_SIZE : bufferSize;
	}
	
	public static int getMaxBufferSize() {
		String maxBufferSizeAsString = Environment.getBiohadoopConfiguration().getGlobalProperties().get(KryoConfig.KRYO_MAX_BUFFER_SIZE);
		
		int maxBufferSize = NumberUtils.toInt(maxBufferSizeAsString, -1);
		
		return maxBufferSize == -1 ? KryoConfig.KRYO_DEFAULT_MAX_BUFFER_SIZE : maxBufferSize;
	}
}

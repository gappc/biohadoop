package at.ac.uibk.dps.biohadoop.datastore;

import org.xnio.Option;

public interface DataClient {

	public <T> T getData(Option<T> option);
	
	public <T> T getData(Option<T> option, T defaultValue);

	public <T> void setData(Option<T> option, T data);

	public void setDefaultData(Object data, double fitness, int iteration);
	
	public void setDefaultData(Object data, double fitness, int maxIterations, int iteration);
	
}

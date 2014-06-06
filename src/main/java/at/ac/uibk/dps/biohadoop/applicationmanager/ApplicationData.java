package at.ac.uibk.dps.biohadoop.applicationmanager;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

public class ApplicationData<T> implements Serializable {
	
	private static final long serialVersionUID = -7628947693047727425L;
	
	private final T data;
	private final long timestamp;
	private final TimeZone timezone;
	
	public ApplicationData() {
		data = null;
		timestamp = 0;
		timezone = null;
	}
	
	public ApplicationData(T data) {
		super();
		this.data = data;
		this.timestamp = new Date().getTime();
		this.timezone = TimeZone.getDefault();
	}
	
	public ApplicationData(T data, long timestamp, TimeZone timezone) {
		super();
		this.data = data;
		this.timestamp = timestamp;
		this.timezone = timezone;
	}

	public T getData() {
		return data;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public TimeZone getTimezone() {
		return timezone;
	}
	
	@Override
	public String toString() {
		return data.toString();
	}
}

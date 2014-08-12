package at.ac.uibk.dps.biohadoop.communication.worker;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;

public class WorkerParameters {

	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private final String host;
	private final int port;

	private WorkerParameters(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			String host, int port) {
		this.remoteExecutable = remoteExecutable;
		this.host = host;
		this.port = port;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@SuppressWarnings("unchecked")
	public static WorkerParameters getParameters(String[] args)
			throws WorkerException {
		if (args == null) {
			throw new WorkerException("Parameters are null");
		}
		try {
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable = null;
			if (args[1].length() > 0) {
				remoteExecutable = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
						.forName(args[0]);
			}
			String host = args[1];
			int port = Integer.parseInt(args[2]);
			return new WorkerParameters(remoteExecutable, host, port);
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			for (String arg : args) {
				sb.append(arg).append(" ");
			}
			throw new WorkerException("Could not parse parameters "
					+ sb.toString(), e);
		}
	}
}

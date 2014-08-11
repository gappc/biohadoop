package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.DedicatedKryo;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;
import at.ac.uibk.dps.biohadoop.utils.ResourcePath;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

public class DefaultKryoServer implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultKryoServer.class);

	private final Server server = new Server(64 * 1024, 64 * 1024);

	private String path;
	private DefaultKryoMasterEndpoint kryoServerEndpoint;
	
	@Override
	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass) {
		path = DefaultTaskClient.QUEUE_NAME;
		if (remoteExecutableClass != null) {
			DedicatedKryo dedicated = remoteExecutableClass
					.getAnnotation(DedicatedKryo.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
				ResourcePath.addRestEntry(path, remoteExecutableClass);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		kryoServerEndpoint = new DefaultKryoMasterEndpoint(remoteExecutableClass, path);
	}
	
	@Override
	public void start() {
		LOG.info("Starting Kryo server");
		try {
			startServer();
			server.addListener(kryoServerEndpoint);
		} catch (Exception e) {
			LOG.error("Kryo Server fatal error", e);
		}
	}

	@Override
	public void stop() {
		LOG.info("KryoServer waiting to shut down");
		kryoServerEndpoint.stop();
		server.stop();
	}

	private void startServer() throws IOException {
		new Thread(server).start();

		String prefix = path;
		String host = HostInfo.getHostname();
		
		PortFinder.aquireBindingLock();
		int port = HostInfo.getPort(30000);
		server.bind(port);
		PortFinder.releaseBindingLock();

		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_HOST, host);
		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_PORT,
				Integer.toString(port));

		Kryo kryo = server.getKryo();
		KryoObjectRegistration.register(kryo);

		LOG.info("host: {} port: {} queue: {}", HostInfo.getHostname(), port, path);
	}

}

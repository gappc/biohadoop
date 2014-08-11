package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

public class KryoMasterServer implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(KryoMasterServer.class);

	private final Server server = new Server(64 * 1024, 64 * 1024);

	private Class<? extends RemoteExecutable<?, ?, ?>> masterClass;
	private KryoMasterEndpoint kryoServerEndpoint;
	
	@Override
	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> masterClass) {
		this.masterClass = masterClass;
		kryoServerEndpoint = new KryoMasterEndpoint(masterClass);
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

		String prefix = masterClass.getCanonicalName();
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

		LOG.info("KryoServer running at " + host + ":" + port);
	}

}

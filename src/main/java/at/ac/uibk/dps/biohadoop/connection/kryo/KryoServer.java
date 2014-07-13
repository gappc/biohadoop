package at.ac.uibk.dps.biohadoop.connection.kryo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public abstract class KryoServer implements MasterLifecycle, MasterEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(KryoServer.class);

	private final Server server = new Server(64 * 1024, 64 * 1024);

	private KryoServerListener kryoServerListener;

	@Override
	public void configure() {
		Log.set(Log.LEVEL_DEBUG);
		kryoServerListener = new KryoServerListener(this);
	}

	@Override
	public void start() {
		LOG.info("Starting Kryo server");
		try {
			startServer();
			server.addListener(kryoServerListener);
		} catch (Exception e) {
			LOG.error("Kryo Server fatal error", e);
		}
	}

	@Override
	public void stop() {
		LOG.info("KryoServer waiting to shut down");
		kryoServerListener.stop();
		server.stop();
	}

	private void startServer() throws IOException {
		new Thread(server).start();

		String prefix = getQueueName();
		String host = HostInfo.getHostname();
		int port = HostInfo.getPort(30000);

		server.bind(port);

		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_HOST, host);
		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_PORT,
				Integer.toString(port));

		Kryo kryo = server.getKryo();
		KryoObjectRegistration.register(kryo);

		LOG.info("KryoServer running at " + host + ":" + port);
	}
}

package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.connection.FileMasterConfiguration;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.server.UndertowServer;

public class EndpointLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointLauncher.class);

	public static void launchMasterEndpoints(final BiohadoopConfiguration config)
			throws Exception {
		ConnectionConfiguration connectionConfiguration = config
				.getConnectionConfiguration();

		for (FileMasterConfiguration master : connectionConfiguration
				.getMasters()) {
			LOG.info("Configuring endpoint {}", master);

			for (Class<? extends MasterConnection> endpointClass : master
					.getEndpoints()) {
				MasterConnection endpoint = endpointClass.newInstance();
				endpoint.configure();
			}
		}

		UndertowServer undertowServer = new UndertowServer();
		undertowServer.startServer();

		for (FileMasterConfiguration master : connectionConfiguration
				.getMasters()) {
			LOG.info("Starting endpoint {}", master);

			for (Class<? extends MasterConnection> endpointClass : master
					.getEndpoints()) {
				MasterConnection endpoint = endpointClass.newInstance();
				endpoint.start();
			}
		}

	}
}

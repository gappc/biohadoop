package at.ac.uibk.dps.biohadoop.server;

import java.io.IOException;

import io.undertow.Undertow;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.worker.MyApp;

public class UndertowServer {

	private UndertowJaxrsServer server = new UndertowJaxrsServer();
	
	public void startServer() throws IOException {
		String hostname = Hostname.getHostname();
		server = new UndertowJaxrsServer();
		server.start(Undertow.builder().addHttpListener(30000, hostname));
		server.deploy(MyApp.class);
		
//		UndertowJaxrsServer server = new UndertowJaxrsServer();
//		server.start(Undertow.builder().addHttpListener(30000, "localhost"));
//		server.deploy(MyApp.class);
//		Undertow server = Undertow.builder()
//                .addHttpListener(30000, "localhost")
//                .setHandler(new HttpHandler() {
//                    @Override
//                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
//                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
//                        exchange.getResponseSender().send("Hello World");
//                    }
//                }).build();
//        server.start();
	}
	
	public void stopServer() {
		server.stop();
	}
}

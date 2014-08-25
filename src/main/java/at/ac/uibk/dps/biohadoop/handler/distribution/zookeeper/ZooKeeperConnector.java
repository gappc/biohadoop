package at.ac.uibk.dps.biohadoop.handler.distribution.zookeeper;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperConnector implements Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(ZooKeeperConnector.class);
	
	private final CountDownLatch latch = new CountDownLatch(1);
	
	@Override
	public Object call() throws Exception {
		latch.await();
		return null;
	}
	
	public ZooKeeper initialize(String url) throws IOException {
		ZooKeeper zooKeeper = new ZooKeeper(url, 2000, new Watcher() {
			@Override
			public void process(WatchedEvent arg0) {
				if (arg0.getState() == KeeperState.SyncConnected) {
					LOG.info("Connection to ZooKeeper successful");
					latch.countDown();
				}
				else {
					LOG.warn("ZooKeeper state: {}", arg0.getState());
				}
			}
		});
		return zooKeeper;
	}
	
	public void unblock() {
		latch.countDown();
	}

}

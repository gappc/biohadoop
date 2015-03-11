package at.ac.uibk.dps.biohadoop.profile.net;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetProfiler implements Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(NetProfiler.class);
	private final AtomicBoolean run = new AtomicBoolean(true);
	private final List<NetData> loData = new CopyOnWriteArrayList<>();
	private final List<NetData> eth0Data = new CopyOnWriteArrayList<>();

	long loRxOld = 0;
	long loTxOld = 0;
	long ethRxOld = 0;
	long ethTxOld = 0;

	public NetProfiler() {
		try {
			List<String> data = Files.readAllLines(Paths.get("/proc/net/dev"),
					Charset.defaultCharset());
			for (String s : data) {
				if (s.contains("lo")) {
					String[] tokens = s.split("\\s+");
					loRxOld = Long.parseLong(tokens[2].trim());
					loTxOld = Long.parseLong(tokens[10].trim());
				} else if (s.contains("eth0")) {
					String[] tokens = s.split("\\s+");
					ethRxOld = Long.parseLong(tokens[2].trim());
					ethTxOld = Long.parseLong(tokens[10].trim());
				}
			}
		} catch (Exception e) {
			LOG.error("Could not measure current network performance", e);
		}
	}

	@Override
	public Object call() throws Exception {
		while (run.get()) {
			Thread.sleep(1000);
			try {
				List<String> data = Files.readAllLines(
						Paths.get("/proc/net/dev"), Charset.defaultCharset());
				for (String s : data) {
					// LOG.info(s);
					if (s.contains("lo")) {
						String[] tokens = s.split("\\s+");
						long rx = Long.parseLong(tokens[2].trim()) - loRxOld;
						long tx = Long.parseLong(tokens[10].trim()) - loTxOld;

						// Values are Mbits/sec; the overhead of 1Gb ethernet is estimated with 1/10, this factor is applied
						double loRxs = rx * 8.0 * (9.0 / 10.0) / 1e6;
						double loTxs = tx * 8.0 * (9.0 / 10.0) / 1e6;
						
						loRxOld = Long.parseLong(tokens[2].trim());
						loTxOld = Long.parseLong(tokens[10].trim());
						
						NetData netData = new NetData(loRxs, loTxs);
						loData.add(netData);
					} else if (s.contains("eth0")) {
						String[] tokens = s.split("\\s+");
						long rx = Long.parseLong(tokens[2].trim()) - ethRxOld;
						long tx = Long.parseLong(tokens[10].trim()) - ethTxOld;

						// Values are Mbits/sec; the overhead of 1Gb ethernet is estimated with 1/10, this factor is applied
						double ethRxs = rx * 8.0 * (9.0 / 10.0) / 1e6;
						double ethTxs = tx * 8.0 * (9.0 / 10.0) / 1e6;
						
						ethRxOld = Long.parseLong(tokens[2].trim());
						ethTxOld = Long.parseLong(tokens[10].trim());
						
						NetData netData = new NetData(ethRxs, ethTxs);
						eth0Data.add(netData);
						// System.out.println(netData);
					}
				}
			} catch (Exception e) {
				LOG.error("Could not measure current network performance", e);
			}
		}
		return null;
	}

	public void logNetData() {
		StringBuilder sb = new StringBuilder();
		sb.append("localhost NetData (rx, tx): ");
		for (NetData data : loData) {
			String s = String.format("%.3f %.3f|", data.getRx(), data.getTx());
			sb.append(s);
		}
		sb.deleteCharAt(sb.length() - 1);
		LOG.info(sb.toString());

		sb = new StringBuilder();
		sb.append("eth0 NetData (rx, tx): ");
		for (NetData data : eth0Data) {
			String s = String.format("%.3f %.3f|", data.getRx(), data.getTx());
			sb.append(s);
		}
		sb.deleteCharAt(sb.length() - 1);
		LOG.info(sb.toString());
	}

	public void stop() {
		run.set(false);
	}
}
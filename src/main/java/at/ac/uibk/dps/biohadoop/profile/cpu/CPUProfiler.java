package at.ac.uibk.dps.biohadoop.profile.cpu;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.OperatingSystemMXBean;

public class CPUProfiler implements Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(CPUProfiler.class);
	private final OperatingSystemMXBean osBean = ManagementFactory
			.getPlatformMXBean(OperatingSystemMXBean.class);
	private final AtomicBoolean run = new AtomicBoolean(true);
	private final List<CPUData> cpuData = new CopyOnWriteArrayList<>();

	@Override
	public Object call() throws Exception {
		while (run.get()) {
			Thread.sleep(1000);
			CPUData data = new CPUData(osBean.getProcessCpuLoad(),
					osBean.getSystemCpuLoad());
			cpuData.add(data);
		}
		return null;
	}

	public List<CPUData> getCPUData() {
		return cpuData;
	}

	public void logCPUData() {
		StringBuilder sb = new StringBuilder();
		sb.append("CPUData (processLoad, systemLoad): ");
		for (CPUData data : cpuData) {
			String s = String.format("%.3f %.3f|", data.getProcessLoad(), data.getSystemLoad());
			sb.append(s);
		}
		sb.deleteCharAt(sb.length() - 1);
		LOG.info(sb.toString());
	}

	public void stop() {
		run.set(false);
	}
}

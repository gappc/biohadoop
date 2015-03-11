package at.ac.uibk.dps.biohadoop.profile;

import at.ac.uibk.dps.biohadoop.profile.cpu.CPUProfiler;
import at.ac.uibk.dps.biohadoop.profile.net.NetProfiler;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.lang.management.OperatingSystemMXBean;

public class Profilers {

	private static final Logger LOG = LoggerFactory
			.getLogger(CPUProfiler.class);

	public static CPUProfiler runCPUProfiler() {
		CPUProfiler cpuProfiler = new CPUProfiler();
		ExecutorService es = Executors.newFixedThreadPool(1);
		es.submit(cpuProfiler);
		es.shutdown();
		return cpuProfiler;
	}
	
	public static NetProfiler runNetProfiler() {
		NetProfiler networkProfiler = new NetProfiler();
		ExecutorService es = Executors.newFixedThreadPool(1);
		es.submit(networkProfiler);
		es.shutdown();
		return networkProfiler;
	}
}

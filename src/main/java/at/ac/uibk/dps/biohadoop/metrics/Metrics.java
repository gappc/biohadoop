package at.ac.uibk.dps.biohadoop.metrics;

import java.util.Map;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class Metrics {

	private static final MetricRegistry METRICS = new MetricRegistry();

	private Metrics() {
		// Nothing to do
	}
	
	public static MetricRegistry getInstance() {
		return METRICS;
	}
	
	public static MetricsData getMetricsData() {
		Map<String, Counter> counters = METRICS.getCounters();
		@SuppressWarnings("rawtypes")
		Map<String, Gauge> gauges = METRICS.getGauges();
		Map<String, Histogram> histograms = METRICS.getHistograms();
		Map<String, Meter> meters = METRICS.getMeters();
		Map<String, Timer> timers = METRICS.getTimers();
		return new MetricsData(counters, gauges, histograms, meters, timers);
	}

}

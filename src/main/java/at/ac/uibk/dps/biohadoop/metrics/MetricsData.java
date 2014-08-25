package at.ac.uibk.dps.biohadoop.metrics;

import java.util.Map;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

public class MetricsData {

	private final Map<String, Counter> counters;
	private final Map<String, Gauge> gauges;
	private final Map<String, Histogram> histograms;
	private final Map<String, Meter> meters;
	private final Map<String, Timer> timers;

	public MetricsData(Map<String, Counter> counters,
			Map<String, Gauge> gauges, Map<String, Histogram> histograms,
			Map<String, Meter> meters, Map<String, Timer> timers) {
		this.counters = counters;
		this.gauges = gauges;
		this.histograms = histograms;
		this.meters = meters;
		this.timers = timers;
	}

	public Map<String, Counter> getCounters() {
		return counters;
	}

	public Map<String, Gauge> getGauges() {
		return gauges;
	}

	public Map<String, Histogram> getHistograms() {
		return histograms;
	}

	public Map<String, Meter> getMeters() {
		return meters;
	}

	public Map<String, Timer> getTimers() {
		return timers;
	}

}

package at.ac.uibk.dps.biohadoop.tasksystem.algorithm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import at.ac.uibk.dps.biohadoop.metrics.Metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class AlgorithmService {

	private static final Map<AlgorithmId, AlgorithmInfo> ALGORITHM_INFOS = new ConcurrentHashMap<>();
	private static final AtomicInteger COUNTER = new AtomicInteger();
	private static final Counter METRICS_COUNTER = Metrics.getInstance()
			.counter(MetricRegistry.name(AlgorithmService.class, "algorithms"));

	private AlgorithmService() {
	}

	public static AlgorithmId addAlgorithm(
			final AlgorithmConfiguration algorithmConfig) {
		AlgorithmId algorithmId = AlgorithmId.newInstance();
		ALGORITHM_INFOS.put(algorithmId, new AlgorithmInfo(algorithmConfig));
		COUNTER.incrementAndGet();
		METRICS_COUNTER.inc();
		return algorithmId;
	}

	public static float getProgress(final AlgorithmId algorithmId) {
		AlgorithmInfo algorithmInfo = ALGORITHM_INFOS.get(algorithmId);
		return algorithmInfo.getProgress();
	}

	public static void setProgress(final AlgorithmId algorithmId,
			final float progress) {
		AlgorithmInfo algorithmInfo = ALGORITHM_INFOS.get(algorithmId);
		algorithmInfo.setProgress(progress);
	}

	public static float getOverallProgress() {
		float progress = 0;
		int algorithmCount = ALGORITHM_INFOS.size();
		for (AlgorithmId algorithmId : ALGORITHM_INFOS.keySet()) {
			AlgorithmInfo algorithmInfo = ALGORITHM_INFOS.get(algorithmId);
			progress += algorithmInfo.getProgress() / algorithmCount;
		}
		return progress > 1 ? 1 : progress;
	}

	public static AlgorithmConfiguration getAlgorithmConfiguration(
			AlgorithmId algorithmId) {
		AlgorithmInfo algorithmInfo = ALGORITHM_INFOS.get(algorithmId);
		return algorithmInfo.getAlgorithmConfiguration();
	}

}

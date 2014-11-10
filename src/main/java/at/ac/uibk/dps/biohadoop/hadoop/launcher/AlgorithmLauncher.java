package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.Algorithm;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmService;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;

public class AlgorithmLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(AlgorithmLauncher.class);

	private AlgorithmLauncher() {
	}

	public static List<Future<AlgorithmId>> launchAlgorithm(
			BiohadoopConfiguration biohadoopConfig)
			throws AlgorithmLaunchException {
		ExecutorService cachedPoolExecutor = Executors.newCachedThreadPool();

		List<Future<AlgorithmId>> algorithmFutures = new ArrayList<>();

		for (AlgorithmConfiguration algorithmConfig : biohadoopConfig
				.getAlgorithmConfigurations()) {
			Callable<AlgorithmId> callable = getAlgorithmCallable(algorithmConfig);
			Future<AlgorithmId> algorithmFuture = cachedPoolExecutor.submit(callable);
			algorithmFutures.add(algorithmFuture);
		}

		// shutdown() can be called here safely, as it just ignores new threads
		// but completes the old ones
		cachedPoolExecutor.shutdown();

		return algorithmFutures;
	}

	private static Callable<AlgorithmId> getAlgorithmCallable(
			final AlgorithmConfiguration algorithmConfig)
			throws AlgorithmLaunchException {
		final AlgorithmId algorithmId = AlgorithmService.addAlgorithm(algorithmConfig);
		return generateCallable(algorithmId, algorithmConfig);
	}

	private static Callable<AlgorithmId> generateCallable(final AlgorithmId algorithmId,
			final AlgorithmConfiguration algorithmConfig) {
		return new Callable<AlgorithmId>() {

			@Override
			public AlgorithmId call() throws Exception {
				LOG.info("Initialising algorithm {} with algorithmId {}",
						algorithmConfig.getName(), algorithmId);

				Algorithm algorithm = (Algorithm) algorithmConfig
						.getAlgorithm().newInstance();

				algorithm.run(algorithmId, algorithmConfig.getProperties());

				LOG.info("Finished algorithm {} with algorithmId {}",
						algorithmConfig.getName(), algorithmId);
				return algorithmId;
			}
		};
	}
}

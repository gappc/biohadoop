package at.ac.uibk.dps.biohadoop.handler.persistence.file;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.datastore.DataProvider;
import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerBuilder;
import at.ac.uibk.dps.biohadoop.handler.HandlerConstants;
import at.ac.uibk.dps.biohadoop.handler.HandlerInitException;
import at.ac.uibk.dps.biohadoop.handler.UnknownHandlerException;
import at.ac.uibk.dps.biohadoop.handler.persistence.PersistenceSaveException;
import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileSaveHandler implements Handler {

	private static final Logger LOG = LoggerFactory
			.getLogger(FileSaveHandler.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	private SolverId solverId;
	private FileSaveConfiguration fileSaveConfiguration;

	@Override
	public void init(SolverId solverId) throws HandlerInitException {
		this.solverId = solverId;

		fileSaveConfiguration = getFileSaveConfiguration();
	}

	@Override
	public void update(String operation) {
		if (HandlerConstants.DEFAULT.equals(operation)) {
			LOG.debug("onDataUpdate for solver {}", solverId);

			int saveAfterEveryIteration = fileSaveConfiguration
					.getAfterIterations();

			SolverData<?> solverData = DataProvider.getSolverData(solverId);

			if (solverData.getIteration() % saveAfterEveryIteration == 0) {
				LOG.info("Persisting data for solver {}", solverId);
				try {
					save(solverId, fileSaveConfiguration, solverData);
				} catch (PersistenceSaveException e) {
					LOG.error("Handler could not save data", e);
				}
			}
		}
	}

	private void save(SolverId solverId,
			FileSaveConfiguration fileSaveConfiguration,
			SolverData<?> solverData) throws PersistenceSaveException {
		String path = fileSaveConfiguration.getPath();
		String savePath = FileHandlerUtils.getSavePath(solverId, path);

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.isDirectory(yarnConfiguration, savePath)) {
				boolean dirCreated = HdfsUtil.mkDir(yarnConfiguration, savePath);
				if (!dirCreated) {
					throw new PersistenceSaveException(
							"Could not create directory with path " + savePath);
				}
			}

			String fullPath = savePath + "/" + solverId.toString() + "_"
					+ solverData.getIteration() + "_"
					+ solverData.getTimestamp();

			if (HdfsUtil.exists(yarnConfiguration, fullPath)) {
				throw new PersistenceSaveException("File " + fullPath
						+ " already exists");
			}

			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			objectMapper.writeValue(bos, solverData);
		} catch (IOException e) {
			throw new PersistenceSaveException("Could not save solver "
					+ solverId + " to file: " + savePath, e);
		}
	}

	private FileSaveConfiguration getFileSaveConfiguration()
			throws HandlerInitException {
		try {
			return HandlerBuilder.getHandlerConfiguration(solverId,
					FileSaveConfiguration.class);
		} catch (UnknownHandlerException e) {
			LOG.error(
					"Could not get handler configuration for solver {} and handler {}",
					solverId, FileSaveConfiguration.class);
			throw new HandlerInitException(e);
		}
	}

}

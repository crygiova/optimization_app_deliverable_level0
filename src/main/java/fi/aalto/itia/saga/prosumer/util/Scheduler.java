package fi.aalto.itia.saga.prosumer.util;

import java.util.Date;
import java.util.Properties;

import fi.aalto.itia.saga.prosumer.util.io.MatlabIO;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentResponse;
import fi.aalto.itia.saga.util.Utility;

public class Scheduler {

	// TODO put this in a config file maybe
	private static final String fileDir;
	private static final String fileOutName;
	private static final String fileInName;
	private static int id;

	private static final String FILE_NAME_PROPERTIES = "scheduler.properties";
	private static final String FILE_DIR = "FILE_DIR";
	private static final String FILE_OUT_NAME = "FILE_OUT_NAME";
	private static final String FILE_IN_NAME = "FILE_IN_NAME";
	private static final String START_ID = "START_ID";

	static {
		Properties properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		fileDir = properties.getProperty(FILE_DIR);
		fileOutName = properties.getProperty(FILE_OUT_NAME);
		fileInName = properties.getProperty(FILE_IN_NAME);
		id = Integer.parseInt(properties.getProperty(START_ID));
	};

	// TODO make it return an object which contains the OPT result
	public static synchronized DayAheadContentResponse optimizeMatlab(int h, int r,
			double[] k, double s0, double sh, double pmax, double[] q,
			double w, double[] tUp, double[] tDw, double tsize,
			Date dayAheadMidnight) {
		DayAheadContentResponse opt = null;
		String write = MatlabIO.prepareStringOut(h, r, k, s0, sh, pmax, q, w,
				tUp, tDw, tsize, fileDir + fileInName, id);
		MatlabIO.writeOutputFile(write, fileDir + fileOutName);
		if (MatlabIO.watchOptResult(fileDir, fileInName))
			opt = MatlabIO.readOptResult(fileDir + fileInName,
					dayAheadMidnight);
		id++;
		return opt;
	}

}

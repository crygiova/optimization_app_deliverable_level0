package fi.aalto.itia.saga.prosumer.util;

import java.util.Date;

import fi.aalto.itia.saga.prosumer.util.io.MatlabIO;


public class Scheduler implements EnergyScheduler {

	//TODO put this in a config file maybe
	private static final String FILE_DIR = "C:/Users/giovanc1/MATLAB/source/";
	private static final String FILE_OUT_NAME = "source.txt";
	private static final String FILE_IN_NAME = "result.txt";
	private static int id = 0;


	// TODO make it return an object which contains the OPT result
	public static synchronized OptimizationResult optimizeMatlab(int h, int r,
			double[] k, double s0, double sh, double pmax, double[] q,
			double w, double[] tUp, double[] tDw, double tsize,
			Date dayAheadMidnight) {
		OptimizationResult opt = null;
		String write = MatlabIO.prepareStringOut(h, r, k, s0, sh, pmax, q, w,
				tUp, tDw, tsize, FILE_DIR + FILE_IN_NAME, id);
		MatlabIO.writeOutputFile(write, FILE_DIR + FILE_OUT_NAME);
		if (MatlabIO.watchOptResult(FILE_DIR, FILE_IN_NAME))
			opt = MatlabIO.readOptResult(FILE_DIR + FILE_IN_NAME, dayAheadMidnight);
		id++;
		return opt;
	}

	
}

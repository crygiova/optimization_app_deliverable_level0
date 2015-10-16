package fi.aalto.itia.saga.prosumer.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import fi.aalto.itia.saga.prosumer.util.io.MatlabIO;
import fi.aalto.itia.saga.prosumer.util.io.MqClientOpt;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentResponse;
import fi.aalto.itia.saga.simulation.messages.IntraContentResponse;
import fi.aalto.itia.saga.util.Utility;

/**
 * This class schedules the charging of the storage by calling the optimization
 * 
 * @author giovanc1
 *
 */
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
	private static final String DAY_AHEAD = "DA";
	private static final String INTRA_DAY = "INTRA";
	private static final String START_ID = "START_ID";

	private String queueNameReply;
	private MqClientOpt mqco;

	// loading properties
	static {
		Properties properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		fileDir = properties.getProperty(FILE_DIR);
		fileOutName = properties.getProperty(FILE_OUT_NAME);
		fileInName = properties.getProperty(FILE_IN_NAME);
		id = Integer.parseInt(properties.getProperty(START_ID));
	};

	/**
	 * @param queueNameReply
	 * @throws Exception
	 */
	public Scheduler(String queueNameReply) throws Exception {
		super();
		this.queueNameReply = queueNameReply;

	}

	public static synchronized DayAheadContentResponse optimizeMatlab(int h,
			int r, BigDecimal[] k, BigDecimal s0, BigDecimal sh,
			BigDecimal pmax, BigDecimal[] q, BigDecimal w, BigDecimal[] tUp,
			BigDecimal[] tDw, BigDecimal tsize, Date dayAheadMidnight) {
		DayAheadContentResponse opt = null;
		String write = MatlabIO.prepareDAStringOut(h, r, k, s0, sh, pmax, q, w,
				tUp, tDw, tsize, fileDir + fileInName, id);
		MatlabIO.writeOutputFile(write, fileDir + fileOutName);
		if (MatlabIO.watchOptResult(fileDir, fileInName))
			opt = MatlabIO
					.readOptResult(fileDir + fileInName, dayAheadMidnight);
		id++;
		return opt;
	}

	public synchronized IntraContentResponse optimizeIntraMqMlab(int h, int r,
			int t, BigDecimal s0, BigDecimal sh, BigDecimal pmax,
			BigDecimal[] ps, BigDecimal[] q1, BigDecimal dQ,
			Date dayAheadMidnight, BigDecimal[] dayAheadSchedule)
			throws Exception {
		IntraContentResponse opt = null;
		String write = MatlabIO.prepareIntraStringOut(h, r, t, s0, sh, pmax,
				ps, q1, dQ, dayAheadSchedule, INTRA_DAY, id);
		mqco = new MqClientOpt(this.queueNameReply);
		String response = mqco.call(write);
		mqco.close();
		// TODO
		opt = MatlabIO.readIntraOptResultFromString(response, dayAheadMidnight);
		id++;
		return opt;
	}

	public synchronized DayAheadContentResponse optimizeDAMqMlab(int h, int r,
			BigDecimal[] k, BigDecimal s0, BigDecimal sh, BigDecimal pmax,
			BigDecimal[] q, BigDecimal w, BigDecimal[] tUp, BigDecimal[] tDw,
			BigDecimal tsize, Date dayAheadMidnight) throws Exception {
		DayAheadContentResponse opt = null;
		String write = MatlabIO.prepareDAStringOut(h, r, k, s0, sh, pmax, q, w,
				tUp, tDw, tsize, DAY_AHEAD, id);
		mqco = new MqClientOpt(this.queueNameReply);
		String response = mqco.call(write);
		mqco.close();
		opt = MatlabIO.readDAOptResultFromString(response, dayAheadMidnight);
		id++;
		return opt;
	}

}

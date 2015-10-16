package fi.aalto.itia.saga.prosumer.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.List;

import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentResponse;
import fi.aalto.itia.saga.simulation.messages.IntraContentResponse;
import fi.aalto.itia.saga.util.MathUtility;

/**
 * CLass that allow the communication with the Matlab optimization module
 * 
 * @author giovanc1
 *
 */
public class MatlabIO {
	private static final String H = "H";
	private static final String EQ = "=";
	private static final String NL = "\n";
	private static final String R = "R";
	private static final String T = "T";
	private static final String K = "K";
	private static final String S_0 = "S_0";
	private static final String S_H = "S_H";
	private static final String P_MAX = "P_MAX";
	private static final String Q = "Q";
	private static final String W = "W";
	private static final String T_UP = "TUP";
	private static final String T_DOWN = "TDW";
	private static final String T_SIZE = "T_SIZE";
	private static final String OPT_TYPE = "OPT_TYPE";
	private static final String ID = "ID";
	private static final String COMMA = ",";
	private static final String S_COl = ";";
	private static final String P = "P";
	private static final String PS = "PS";
	//DayAhead Schedule
	private static final String DAS = "DAS";
	private static final String DQ = "DQ";
	private static final String DP = "dP";
	private static final String DPD = "dPd";
	private static final String DPU = "dPu";
	private static final String J = "J";

	/**
	 * This method prepare and returns the proper string for the communication
	 * with Matlab
	 * 
	 * @param h
	 * @param r
	 * @param k
	 * @param s0
	 * @param sh
	 * @param pmax
	 * @param q
	 * @param w
	 * @param tUp
	 * @param tDown
	 * @param tsize
	 * @param outDir
	 * @param id
	 * @return
	 */
	public static String prepareDAStringOut(int h, int r, BigDecimal[] k,
			BigDecimal s0, BigDecimal sh, BigDecimal pmax, BigDecimal[] q,
			BigDecimal w, BigDecimal[] tUp, BigDecimal[] tDown,
			BigDecimal tsize, String opType, int id) {
		String buffer = H + EQ + String.valueOf(h) + NL;
		buffer += R + EQ + String.valueOf(r) + NL;
		buffer += S_0 + EQ + String.valueOf(s0) + NL;
		buffer += S_H + EQ + String.valueOf(sh) + NL;
		buffer += P_MAX + EQ + String.valueOf(pmax) + NL;
		buffer += K + EQ + printArray(k) + NL;// K
		buffer += Q + EQ + printArray(q) + NL;// Q
		buffer += W + EQ + String.valueOf(w) + NL;
		buffer += T_UP + EQ + printArray(tUp) + NL;
		buffer += T_DOWN + EQ + printArray(tDown) + NL;
		buffer += T_SIZE + EQ + String.valueOf(tsize) + NL;
		buffer += OPT_TYPE + EQ + opType + NL;
		buffer += ID + EQ + String.valueOf(id);
		return buffer;
	}

	public static String prepareIntraStringOut(int h, int r, int t,
			BigDecimal s0, BigDecimal sh, BigDecimal pmax, BigDecimal[] ps,
			BigDecimal[] q1, BigDecimal dQ, BigDecimal[] dayAheadSchedule, String opType, int id) {
		String buffer = H + EQ + String.valueOf(h) + NL;
		buffer += R + EQ + String.valueOf(r) + NL;
		buffer += T + EQ + String.valueOf(t) + NL;
		buffer += S_0 + EQ + String.valueOf(s0) + NL;
		buffer += S_H + EQ + String.valueOf(sh) + NL;
		buffer += P_MAX + EQ + String.valueOf(pmax) + NL;
		buffer += PS + EQ + printArray(ps) + NL;
		buffer += DAS + EQ + printArray(dayAheadSchedule) + NL;
		buffer += Q + EQ + printArray(q1) + NL;
		buffer += DQ + EQ + String.valueOf(dQ) + NL;
		buffer += OPT_TYPE + EQ + opType + NL;
		buffer += ID + EQ + String.valueOf(id);
		return buffer;
	}

	/**
	 * This method writes a string to a txt file
	 * 
	 * @param toWrite
	 * @param fileName
	 */
	public static void writeOutputFile(String toWrite, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					fileName)));
			writer.write(toWrite);
			writer.close();
		} catch (IOException e) {
			// TODO add log
			e.printStackTrace();
		}
	}

	private static String printArray(BigDecimal[] k2) {
		String buffer = "";
		int i;
		for (i = 0; i < k2.length - 1; i++) {
			buffer += String.valueOf(k2[i]) + COMMA;
		}
		buffer += String.valueOf(k2[i]);
		return buffer;
	}

	/**
	 * Watcher method which waits response from Matlab
	 * 
	 * @param dirIn
	 * @param fileIn
	 * @return
	 */
	public static boolean watchOptResult(String dirIn, String fileIn) {
		Path myDir = Paths.get(dirIn);
		boolean outLoop = false;
		try {
			WatchService watcher = myDir.getFileSystem().newWatchService();
			// myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
			// StandardWatchEventKinds.ENTRY_DELETE,
			// StandardWatchEventKinds.ENTRY_MODIFY);
			myDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			WatchKey watckKey = watcher.take();
			// TODO should add some additional condition in case of some
			// mistake
			while (!outLoop) {
				List<WatchEvent<?>> events = watckKey.pollEvents();
				for (WatchEvent<?> event : events) {
					// if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)
					// {
					// System.out
					// .println("Created: " + event.context().toString());
					// }
					// if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
					// {
					// System.out.println("Delete: " +
					// event.context().toString());
					// }
					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						if (fileIn.compareToIgnoreCase(event.context()
								.toString()) == 0) {
							outLoop = true;
							// TODO sleep to wait the file to be completely
							// written
							Thread.sleep(10);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			return false;
		}
		return true;
	}

	/**
	 * Reads the Optimization response message of Matlab
	 * 
	 * @param fileName
	 * @param dayAheadMidnight
	 * @return
	 */
	public static DayAheadContentResponse readOptResult(String fileName,
			Date dayAheadMidnight) {
		BufferedReader br = null;
		DayAheadContentResponse opt = new DayAheadContentResponse();
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(fileName));
			while ((sCurrentLine = br.readLine()) != null) {
				// System.out.println(sCurrentLine);
				String keyValue[] = sCurrentLine.split(EQ);
				switch (keyValue[0]) {
				case ID:
					opt.setId(Integer.parseInt(keyValue[1]));
					break;
				case P:
					opt.setP(createPSequence(keyValue[1].split(COMMA),
							dayAheadMidnight));
					break;
				case DP:
					String[] split = keyValue[1].split(S_COl);
					opt.setDp(getArray(split[0]), getArray(split[1]));
					break;
				case J:
					opt.setJ(BigDecimal.valueOf(Double.parseDouble(keyValue[1])));
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return opt;
	}

	/**
	 * Reads the Optimization response message of Matlab from a string message
	 * without accessing a file in the filesystem
	 * 
	 * @param fileName
	 * @param dayAheadMidnight
	 * @return
	 */
	public static DayAheadContentResponse readDAOptResultFromString(
			String content, Date dayAheadMidnight) {
		BufferedReader br = null;
		DayAheadContentResponse opt = new DayAheadContentResponse();
		try {
			String sCurrentLine;
			br = new BufferedReader(new StringReader(content));
			while ((sCurrentLine = br.readLine()) != null) {
				// System.out.println(sCurrentLine);
				String keyValue[] = sCurrentLine.split(EQ);
				switch (keyValue[0]) {
				case ID:
					opt.setId(Integer.parseInt(keyValue[1]));
					break;
				case P:
					opt.setP(createPSequence(keyValue[1].split(COMMA),
							dayAheadMidnight));
					break;
				case DP:
					String[] split = keyValue[1].split(S_COl);
					opt.setDp(getArray(split[0]), getArray(split[1]));
					break;
				case J:
					opt.setJ(BigDecimal.valueOf(Double.parseDouble(keyValue[1])));
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return opt;
	}

	public static IntraContentResponse readIntraOptResultFromString(
			String content, Date dayAheadMidnight) {
		BufferedReader br = null;
		IntraContentResponse opt = new IntraContentResponse();
		try {
			String sCurrentLine;
			br = new BufferedReader(new StringReader(content));
			while ((sCurrentLine = br.readLine()) != null) {
				// System.out.println(sCurrentLine);
				String keyValue[] = sCurrentLine.split(EQ);
				switch (keyValue[0]) {
				case ID:
					opt.setId(Integer.parseInt(keyValue[1]));
					break;
				case DPD:
					opt.setdPd(getArray(keyValue[1]));
					break;
				case DPU:
					opt.setdPu(getArray(keyValue[1]));
					break;
				case PS:
					opt.setP(createPSequence(keyValue[1].split(COMMA),
							dayAheadMidnight));
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return opt;
	}

	private static TimeSequencePlan createPSequence(String[] values,
			Date dayAheadMidnight) {
		TimeSequencePlan tsp = new TimeSequencePlan(dayAheadMidnight);
		Date start = dayAheadMidnight;
		BigDecimal unit;
		for (int i = 0; i < values.length; i++) {
			unit = MathUtility.roundDoubleTo(Double.parseDouble(values[i]), 6);
			tsp.addTimeEnergyTuple(start, unit);
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return tsp;
	}

	private static BigDecimal[] getArray(String s) {
		String[] buffer = s.split(COMMA);
		BigDecimal array[] = new BigDecimal[buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			array[i] = MathUtility.roundDoubleTo(Double.parseDouble(buffer[i]),
					6);
		}
		return array;
	}
}

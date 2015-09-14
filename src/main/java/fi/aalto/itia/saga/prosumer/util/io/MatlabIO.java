package fi.aalto.itia.saga.prosumer.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.List;

import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.prosumer.util.OptimizationResult;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;

public class MatlabIO {
	private static final String H = "H";
	private static final String EQ = "=";
	private static final String NL = "\n";
	private static final String R = "R";
	private static final String K = "K";
	private static final String S_0 = "S_0";
	private static final String S_H = "S_H";
	private static final String P_MAX = "P_MAX";
	private static final String Q = "Q";
	private static final String W = "W";
	private static final String T_UP = "TUP";
	private static final String T_DOWN = "TDW";
	private static final String T_SIZE = "T_SIZE";
	private static final String OUT_DIR = "OUT_DIR";
	private static final String ID = "ID";
	private static final String COMMA = ",";
	private static final String S_COl = ";";
	private static final String P = "P";
	private static final String DP = "dP";
	private static final String J = "J";
	

	public static String prepareStringOut(int h, int r, double[] k, double s0,
			double sh, double pmax, double[] q, double w, double[] tUp,double[] tDown,
			double tsize, String outDir, int id) {
		String buffer = H + EQ + String.valueOf(h) + NL;
		buffer += R + EQ + String.valueOf(r) + NL;
		buffer += S_0 + EQ + String.valueOf(s0) + NL;
		buffer += S_H + EQ + String.valueOf(sh) + NL;
		buffer += P_MAX + EQ + String.valueOf(pmax) + NL;
		buffer += K + EQ + printArray(k) + NL;// K
		buffer += Q + EQ + printArray(q) + NL;// Q
		buffer += W + EQ + String.valueOf(w) + NL;
		buffer += T_UP + EQ + printArray(tUp)+ NL;
		buffer += T_DOWN + EQ + printArray(tDown)+ NL;
		buffer += T_SIZE + EQ + String.valueOf(tsize) + NL;
		buffer += OUT_DIR + EQ + outDir + NL;
		buffer += ID + EQ + String.valueOf(id);
		return buffer;
	}
	
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
	
	private static String printArray(double[] k2) {
		String buffer = "";
		int i;
		for (i = 0; i < k2.length - 1; i++) {
			buffer += String.valueOf(k2[i]) + COMMA;
		}
		buffer += String.valueOf(k2[i]);
		return buffer;
	}
	
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
	
	public static OptimizationResult readOptResult(String fileName,
			Date dayAheadMidnight) {
		BufferedReader br = null;
		OptimizationResult opt = new OptimizationResult();
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
					opt.setJ(Double.parseDouble(keyValue[1]));
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
		for (int i = 0; i < values.length; i++) {
			tsp.addTimeEnergyTuple(start, Double.parseDouble(values[i]));
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return tsp;
	}

	private static double[] getArray(String s) {
		String[] buffer = s.split(COMMA);
		double array[] = new double[buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			array[i] = Double.parseDouble(buffer[i]);
		}
		return array;
	}
}

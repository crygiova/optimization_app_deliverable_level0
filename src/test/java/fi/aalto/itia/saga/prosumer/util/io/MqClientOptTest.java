package fi.aalto.itia.saga.prosumer.util.io;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

public class MqClientOptTest {

	private final static String FILE_NAME = "C:/Users/giovanc1/MATLAB/source/sourceForRabbitMQ.txt";

	public MqClientOptTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testMqClient() throws Exception {
		BufferedReader br;
		String msg = "";
		try {
			br = new BufferedReader(new FileReader(FILE_NAME));
			String currentLine;
			boolean first = true;
			while ((currentLine = br.readLine()) != null) {
				if (first)
					first = false;
				else
					msg += "\n";
				msg += currentLine;
			}
		} catch (Exception e) {
			System.out.println("File " + FILE_NAME + " not exists");
		}
		MqClientOpt mqc = new MqClientOpt("t");
		String retuen = mqc.call(msg);
		mqc.close();
		System.out.println(retuen);
	}
}

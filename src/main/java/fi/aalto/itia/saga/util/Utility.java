package fi.aalto.itia.saga.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utility {

	public static Properties getProperties(String fileName) {
		Properties properties = new Properties();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try (InputStream resourceStream = classLoader
				.getResourceAsStream(fileName)) {
			properties.load(resourceStream);
		} catch (IOException e) {
			System.out.println("Property file not Found: "
					+ fileName);
		}
		return properties;
	}
}

package test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogTest {

	private final static Logger LOGGER = Logger.getLogger(LogTest.class
			.getName());

	public static void doSomeThingAndLog() {

		// ... more code

		// now we demo the logging

		// set the LogLevel to Severe, only severe Messages will be written

		LOGGER.setLevel(Level.SEVERE);

		LOGGER.severe("sev Log");

		LOGGER.warning("warn Log");

		LOGGER.info("Info Log");

		LOGGER.finest("Really not important");

		// set the LogLevel to Info, severe, warning and info will be written

		// finest is still not written

		LOGGER.setLevel(Level.INFO);

		LOGGER.severe("sev2 Log");

		LOGGER.warning("warn2 Log");

		LOGGER.info("Info2 Log");

		LOGGER.finest("Really not important");

	}

	public static void main(String[] args) {

		doSomeThingAndLog();

	}

}

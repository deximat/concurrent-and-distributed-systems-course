package com.codlex.distributed.systems.homework1.starter;

import org.apache.logging.log4j.LogManager;

public class Log4jConfigurator {

	public static void configure(String filename) {
		System.setProperty("logFilename", filename);
		org.apache.logging.log4j.core.LoggerContext ctx =
			    (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
			ctx.reconfigure();
	}
}

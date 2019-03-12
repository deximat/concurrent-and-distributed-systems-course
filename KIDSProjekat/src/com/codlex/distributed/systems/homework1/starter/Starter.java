package com.codlex.distributed.systems.homework1.starter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.codlex.distributed.systems.homework1.bootstrap.BootstrapNode;
import com.codlex.distributed.systems.homework1.peer.VideoStreamingGui;

public class Starter {

	private static final int NUMBER_OF_GUIS = 4;

	private static final List<Process> processes = new ArrayList<>();

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		deleteFolder(new File("videos"));
		// deleteFolder(new File("logs"));

		processes.add(JavaProcess.exec(BootstrapNode.class));
		for (int i = 1; i <= NUMBER_OF_GUIS; i++) {
			Integer port = 8100 + i;
			Integer streamingPort = 8000 + i * 2;
			processes.add(JavaProcess.exec(VideoStreamingGui.class, port.toString(), streamingPort.toString()));
		}


	}

}

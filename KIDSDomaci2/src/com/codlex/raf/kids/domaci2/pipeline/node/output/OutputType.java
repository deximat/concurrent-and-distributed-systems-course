package com.codlex.raf.kids.domaci2.pipeline.node.output;

import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.ConsoleOutput;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.GUIOutput;

public enum OutputType {

	Console, GUI;

	public Output produceOutput() {
		switch (this) {
		case Console:
			return new ConsoleOutput();
		case GUI:
			return new GUIOutput();

		}
		return null;

	}

}

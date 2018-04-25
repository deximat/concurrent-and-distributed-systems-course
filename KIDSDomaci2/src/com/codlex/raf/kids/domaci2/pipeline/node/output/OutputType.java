package com.codlex.raf.kids.domaci2.pipeline.node.output;

import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.ConsoleOutput;

public enum OutputType {

	Console;

	public Output produceOutput() {
		switch (this) {
		case Console:
			return new ConsoleOutput();

		}
		return null;

	}

}

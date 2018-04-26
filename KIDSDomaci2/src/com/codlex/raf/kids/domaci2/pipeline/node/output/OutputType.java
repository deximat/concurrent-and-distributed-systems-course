package com.codlex.raf.kids.domaci2.pipeline.node.output;

import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.ConsoleOutput;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.GUIOutput;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.PDFOutput;

public enum OutputType {

	Console, GUI, PDF;

	public Output produceOutput() {
		switch (this) {
		case Console:
			return new ConsoleOutput();
		case GUI:
			return new GUIOutput();
		case PDF:
			return new PDFOutput();
		}
		return null;

	}

}

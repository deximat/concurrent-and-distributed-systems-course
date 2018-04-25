package com.codlex.raf.kids.domaci2.pipeline.node.input;

import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.input.ConsoleInput;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.input.DatabaseInput;

public enum InputType {
	Database, Console;


	public Input produceInput(Worker worker) {
		switch (this) {
		case Console:
			return new ConsoleInput(worker);
		case Database:
			return new DatabaseInput(worker);
		}
		return null;
	}
}

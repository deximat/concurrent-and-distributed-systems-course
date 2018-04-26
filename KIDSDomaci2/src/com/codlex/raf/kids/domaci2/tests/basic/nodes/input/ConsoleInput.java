package com.codlex.raf.kids.domaci2.tests.basic.nodes.input;

import java.util.Scanner;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.input.BaseInput;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;

public class ConsoleInput extends BaseInput {

	private final Scanner scanner = new Scanner(System.in);

	private void readTask() {
		String command = this.scanner.nextLine();
		String[] stringNumbers = command.split(",");
		int[] numbers = new int[stringNumbers.length];

		int i = 0;
		for (String stringRep : stringNumbers) {
			numbers[i++] = Integer.parseInt(stringRep);
		}

		getWorker().give(PipelineCollection.ofInts(generateFullId(), numbers));

		// resubmit for next line
		execute(this::readTask);
	}

	public ConsoleInput(final Worker worker) {
		super(worker);
		execute(this::readTask);
		System.out.println("Reading input attached to: " + worker);
	}

}

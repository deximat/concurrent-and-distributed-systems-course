package com.codlex.raf.kids.domaci2.tests.basic;

import com.codlex.raf.kids.domaci2.pipeline.Pipeline;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.input.DatabaseInput;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.ConsoleOutput;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.worker.SumWorker;

public class PipelineTest {
	
	public static void main(String[] args) {
//		Worker sumWorker = new SumWorker();
//		FakeDatabaseInput input = new FakeDatabaseInput(sumWorker);
//		ConsoleOutput output = new ConsoleOutput();
//		sumWorker.addOutput(output);
//		Pipeline pipeline = new Pipeline();
//		pipeline.addLast(sumWorker);
		
		
//		Console input
//		Worker sumWorker = new SumWorker();
//		ConsoleInput input = new ConsoleInput(sumWorker);
//		ConsoleOutput output = new ConsoleOutput(1);
//		sumWorker.addOutput(output);
//		Pipeline pipeline = new Pipeline();
//		pipeline.addLast(sumWorker);
		
		// Database input
		Worker sumWorker = new SumWorker();
		DatabaseInput input = new DatabaseInput(sumWorker);
		input.setParam(DatabaseInput.Params.DATABASE_QUERY, "select experience as rating from public.user");
		System.out.println("Available params: " + input.getParams());
		ConsoleOutput output = new ConsoleOutput();
		sumWorker.addOutput(output);
		Pipeline pipeline = new Pipeline();
		pipeline.addLast(sumWorker);
		
		input.triggerRead();
	}
	
}

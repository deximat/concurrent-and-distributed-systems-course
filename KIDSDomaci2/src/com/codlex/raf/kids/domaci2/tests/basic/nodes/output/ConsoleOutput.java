package com.codlex.raf.kids.domaci2.tests.basic.nodes.output;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.output.BaseOutput;

public class ConsoleOutput extends BaseOutput {
	
	public ConsoleOutput() {
	}

	@Override
	public void accept(final PipelineCollection collection) {
		// TODO: make output's threads
		System.out.println("Blah: " + collection);
	}

	@Override
	protected void onFinish(PipelineCollection toProcess) {
		
	}
	
	
}

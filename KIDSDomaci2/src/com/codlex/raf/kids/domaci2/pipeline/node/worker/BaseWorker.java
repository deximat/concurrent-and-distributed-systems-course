package com.codlex.raf.kids.domaci2.pipeline.node.worker;

import java.util.ArrayList;
import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.base.BaseNode;
import com.codlex.raf.kids.domaci2.pipeline.node.output.Output;

import lombok.Setter;

public abstract class BaseWorker extends BaseNode implements Worker {

	@Setter
	private Worker next;
	
	private final List<Output> outputs = new ArrayList<>();

	@Override
	public final void give(final PipelineCollection toProcess) {
		processAll(toProcess);
	}

	protected final void onFinish(final PipelineCollection finalResult) {
		
		for (Output output : this.outputs) {
			output.accept(finalResult);
		}
		
		if (this.next != null) {
			this.next.give(finalResult);
		}
	}
	

	@Override
	public void addOutput(Output output) {
		this.outputs.add(output);
	}

}

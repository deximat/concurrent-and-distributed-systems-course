package com.codlex.raf.kids.domaci2.pipeline.node.input;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.base.BaseNode;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;

import lombok.Getter;

public abstract class BaseInput extends BaseNode implements Input {

	@Getter
	private final Worker worker;

	public BaseInput(final Worker worker) {
		this.worker = worker;
		this.worker.addInput(this);
	}

	@Override
	protected void onFinish(PipelineCollection toProcess) {
		this.worker.give(toProcess);
		super.onFinish(toProcess);
	}
}

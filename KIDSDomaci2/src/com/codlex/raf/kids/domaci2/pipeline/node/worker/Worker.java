package com.codlex.raf.kids.domaci2.pipeline.node.worker;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.base.Node;
import com.codlex.raf.kids.domaci2.pipeline.node.input.Input;
import com.codlex.raf.kids.domaci2.pipeline.node.output.Output;

public interface Worker extends Node {
	public void give(PipelineCollection toProcess);
	public void setNext(Worker worker);
	public void addOutput(Output output);
	public void addInput(Input databaseInput);
}

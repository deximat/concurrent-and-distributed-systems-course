package com.codlex.raf.kids.domaci2.pipeline.node.output;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.base.Node;

public interface Output extends Node {
	public void accept(PipelineCollection collection);
}

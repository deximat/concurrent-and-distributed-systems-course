package com.codlex.raf.kids.domaci2.tests.basic.nodes.worker;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.output.Output;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.BaseWorker;

public class SumWorker extends BaseWorker {
	
	final String keyToSum = "VALUE";
	public SumWorker() {
	}
	
	@Override
	protected PipelineCollection processBatch(final PipelineCollection toProcess) {
		int sum = 0;
		for (PipelineData data : toProcess) {
			sum += data.getIntValue(this.keyToSum);
		}
		return PipelineCollection.of(PipelineData.ofInt(sum));
	}
	
	@Override
	protected PipelineCollection mergeBatches(final List<PipelineCollection> batchResults) {
		int totalSum = 0;
		
		for (PipelineCollection collection : batchResults) {
			int batchResult = collection.first().getIntValue(PipelineData.defaultKey());
			totalSum += batchResult;
		}
		
		return PipelineCollection.of(PipelineData.ofInt(totalSum));
	}
	
}

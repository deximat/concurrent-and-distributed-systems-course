package com.codlex.raf.kids.domaci2.tests.basic.nodes.worker;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.BaseWorker;


public class SumWorker extends BaseWorker {

	private final static String FIELD_TO_SUM = "fieldToSum";

	public SumWorker() {
		setParam(FIELD_TO_SUM, "rating");
	}

	@Override
	protected PipelineCollection processBatch(final PipelineCollection toProcess) {
		int sum = 0;
		for (PipelineData data : toProcess) {
			sum += data.getIntValue(getParam(FIELD_TO_SUM));
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

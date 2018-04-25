package com.codlex.raf.kids.domaci2.tests.basic.nodes.worker;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.BaseWorker;


/**
 * Average - pronalazi prosečnu vrednost za neki ključ iz kolekcije koja mu je prosleđena i konstruiše jedan
 * PipelineData objekat koji sadrži tu vrednost, koji onda prosleđuje dalje. Podela posla kod niti ima iste
 * zahteve kao kod Range Splitter.
 */
public class AverageWorker extends BaseWorker {

	private static final String FIELD_TO_AVERAGE = "field to average";
	private static final String SUM_KEY = "sumKey";
	private static final String COUNT_KEY = "countKey";

	public AverageWorker() {
		setParam(FIELD_TO_AVERAGE, "rating");
	}

	@Override
	protected PipelineCollection processBatch(PipelineCollection toProcess) {
		int sum = 0;

		for (PipelineData data : toProcess) {
			sum += data.getIntValue(getParam(FIELD_TO_AVERAGE));
		}

		PipelineData result = PipelineData.create();

		result.setValue(SUM_KEY, sum);
		result.setValue(COUNT_KEY, toProcess.size());

		return PipelineCollection.of(result);
	}

	@Override
	protected PipelineCollection mergeBatches(List<PipelineCollection> toProcess) {
		double sum = 0;
		int count = 0;

		for (PipelineCollection batch : toProcess) {
			sum += batch.first().getIntValue(SUM_KEY);
			count += batch.first().getIntValue(COUNT_KEY);
		}

 		return PipelineCollection.of(PipelineData.ofDouble(sum / count));
	}

}

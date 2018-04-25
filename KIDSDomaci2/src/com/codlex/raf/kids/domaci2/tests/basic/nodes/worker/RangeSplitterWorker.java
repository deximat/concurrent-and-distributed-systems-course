package com.codlex.raf.kids.domaci2.tests.basic.nodes.worker;

import java.util.ArrayList;
import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.BaseWorker;
import com.google.common.collect.Range;

/*
 * Range Splitter - grupiše PipelineData objekte sa ulaza u manje kolekcije koje su grupisane
 * po opsezima vrednosti jednog ključa. Npr. može da se iskoristi da podeli objekte koji predstavljaju
 * korisnike sajta po starosnim kategorijama. Za svaku grupu koju napravi, konstruisaće novi PipelineCollection
 * i proslediti dalje u pipeline. Svaka nit treba da obrađuje deo ulaza do nekog fiksnog broja
 * PipelineData objekata. Treba da bude moguće i da više niti radi konkurentno na jednom velikom
 * PipelineCollection, kao i da niti mogu da obrađuju veliki broj manjih PipelineCollection.
 */
public class RangeSplitterWorker extends BaseWorker {

	private static final String FIELD_KEY = "key to split";
	private static final String RANGES_KEY = "ranges";

	public RangeSplitterWorker() {
		setParam(FIELD_KEY, "rating");
		setParam(RANGES_KEY, "100-200, 200-300");
	}

	List<Range<Integer>> getRanges() {
		String rawRangeString = getParam(RANGES_KEY);
		String[] rawRanges = rawRangeString.split(",");

		List<Range<Integer>> ranges = new ArrayList<Range<Integer>>();
		for (String rawRange : rawRanges) {
			String[] bounds = rawRange.split("-");
			ranges.add(Range.closed(Integer.parseInt(bounds[0].trim()), Integer.parseInt(bounds[1].trim())));
		}

		return ranges;
	}

	private Range<Integer> getRange(int value) {
		for (Range<Integer> range : getRanges()) {
			if (range.contains(value)) {
				return range;
			}
		}

		return null;
	}

	@Override
	protected PipelineCollection processBatch(PipelineCollection toProcess) {
		PipelineData result = PipelineData.create();
		for (PipelineData data : toProcess) {
			int value = data.getIntValue(getParam(FIELD_KEY));
			Range<Integer> range = getRange(value);
			if (range == null) {
				continue;
			}

			List<PipelineData> category = result.getValue(range.toString());
			if (category == null) {
				category = new ArrayList<>();
				result.setValue(range.toString(), category);
			}
			category.add(data);
		}
		return PipelineCollection.of(result);
	}

	@Override
	protected PipelineCollection mergeBatches(List<PipelineCollection> toProcess) {
		for (Range<Integer> range : getRanges()) {

			final PipelineCollection merged = PipelineCollection.create();

			for (PipelineCollection batch : toProcess) {
				List<PipelineData> unmerged = batch.first().getValue(range.toString());
				if (unmerged != null) {
					merged.addAll(unmerged);
				}
			}

			onFinish(merged);
		}

		return null;
	}
}

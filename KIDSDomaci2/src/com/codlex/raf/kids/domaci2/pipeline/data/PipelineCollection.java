package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

public interface PipelineCollection extends Iterable<PipelineData> {
	PipelineID getID();
	void setID(PipelineID id);
	void setPartsCount(int count);

	PipelineData peek(PipelineID id);
	PipelineData take();
	PipelineData first();
	void put(PipelineData data);

	static PipelineCollection of(PipelineID id, final PipelineData data) {
		final PipelineCollection collection = create(id);
		collection.put(data);
		return collection;
	}

	static PipelineCollection create(PipelineID id) {
		return new BasePipelineCollection(id);
	}

	int size();

	boolean isEmpty();

	static PipelineCollection empty(PipelineID id) {
		return create(id);
	}

	static PipelineCollection ofInts(final PipelineID id, int... ints) {
		PipelineCollection collection = create(id);
		for (Integer value : ints) {
			collection.put(PipelineData.ofInt(value));
		}
		return collection;
	}

	void addAll(List<PipelineData> unmerged);

	int getPartsCount();

	static PipelineCollection create(PipelineID id, int partsCount) {
		PipelineCollection collection = create(id);
		collection.setPartsCount(partsCount);
		return collection;
	}

}

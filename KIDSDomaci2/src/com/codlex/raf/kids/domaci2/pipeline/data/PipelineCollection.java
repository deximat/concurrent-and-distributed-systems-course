package com.codlex.raf.kids.domaci2.pipeline.data;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

public interface PipelineCollection extends Iterable<PipelineData> {
	PipelineID getID();
	PipelineData peek(PipelineID id);
	PipelineData take();
	PipelineData first();
	void put(PipelineData data);
	
	static PipelineCollection of(final PipelineData data) {
		final PipelineCollection collection = create();
		collection.put(data);
		return collection;
	}
	
	static PipelineCollection create() {
		return new BasePipelineCollection();
	}
	
	int size();
	
	boolean isEmpty();
	
	static PipelineCollection empty() {
		return create();
	}
	
	static PipelineCollection ofInts(int... ints) {
		PipelineCollection collection = create();
		for (Integer value : ints) {
			collection.put(PipelineData.ofInt(value));
		}
		return collection;
	}
	
}

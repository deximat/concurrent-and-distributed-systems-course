package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

import lombok.Getter;

public class CollectionCollector {

	private ConcurrentLinkedQueue<PipelineCollection> results = new ConcurrentLinkedQueue<>();
	final AtomicInteger processedCount = new AtomicInteger();

	@Getter
	private final PipelineID id;
	private final int size;

	public CollectionCollector(final PipelineID id, final int size) {
		this.id = id;
		this.size = size;
	}

	public boolean submitResultAndShouldMerge(final PipelineCollection result) {
		if (result != null) {
			this.results.add(result);
		}

		int processed = this.processedCount.incrementAndGet();
		return processed == this.size;
	}

	public List<PipelineCollection> getAllResults() {
		return new ArrayList<>(this.results);
	}

}

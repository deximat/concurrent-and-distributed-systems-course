package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.Getter;

public class CollectionBatcher {

	private final List<PipelineCollection> batches;

	public List<PipelineCollection> getBatches() {
		return this.batches;
	}

	public CollectionBatcher(final PipelineCollection collection, final int batchSize) {
		this.batches = split(collection, batchSize);
	}

	private List<PipelineCollection> split(PipelineCollection collection, int batchSize) {
		final List<PipelineCollection> collections = new ArrayList<>();

		int batchesCount = collection.size() / batchSize;
		if (batchesCount % batchSize != 0) {
			batchesCount++;
		}

		PipelineCollection batchCollection = PipelineCollection.create(collection.getID(), batchesCount);

		for (final PipelineData data : collection) {

			batchCollection.put(data);

			if (batchCollection.size() >= batchSize) {
				collections.add(batchCollection);
				batchCollection = PipelineCollection.create(collection.getID(), batchesCount);
			}

		}

		if (!batchCollection.isEmpty()) {
			collections.add(batchCollection);
		}

		return collections;
	}


}

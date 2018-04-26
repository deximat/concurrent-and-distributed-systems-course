package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class BasePipelineCollection implements PipelineCollection {

	@Getter
	@Setter
	private PipelineID ID;

	@Getter
	@Setter
	private int partsCount;

	private final List<PipelineData> backingCollection = new ArrayList<>();

	public BasePipelineCollection(PipelineID id) {
		this.ID = id;
	}

	@Override
	public Iterator<PipelineData> iterator() {
		return this.backingCollection.iterator();
	}

	@Override
	public PipelineData peek(PipelineID id) {
		return null;
	}

	@Override
	public PipelineData take() {
		return null;
	}

	@Override
	public PipelineData first() {
		return this.backingCollection.get(0);
	}

	@Override
	public void put(PipelineData data) {
		this.backingCollection.add(data);
	}

	@Override
	public int size() {
		return this.backingCollection.size();
	}

	@Override
	public boolean isEmpty() {
		return this.backingCollection.isEmpty();
	}

	@Override
	public void addAll(List<PipelineData> unmerged) {
		this.backingCollection.addAll(unmerged);
	}


}

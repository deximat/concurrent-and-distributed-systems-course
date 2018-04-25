package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

import lombok.ToString;

@ToString
public class BasicPipelineData implements PipelineData {
	
	private final Map<String, Object> backingMap = new HashMap<>();
	
	@Override
	public PipelineID getID() {
		return null;
	}

	@Override
	public Object getValue(String key) {
		return this.backingMap.get(key);
	}

	@Override
	public void setValue(String key, Object value) {
		this.backingMap.put(key, value);
	}

	@Override
	public List<String> keys() {
		return new ArrayList<>(this.backingMap.keySet());
	}

	@Override
	public int getIntValue(String key) {
		return (Integer) this.backingMap.get(key);
	}

}

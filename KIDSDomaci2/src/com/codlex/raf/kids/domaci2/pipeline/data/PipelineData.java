package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

public interface PipelineData {
	
	PipelineID getID();
	Object getValue(String key);
	void setValue (String key, Object value);
	List<String> keys();
	
	public static PipelineData ofInt(int value) {
		final BasicPipelineData data = new BasicPipelineData();
		data.setValue(defaultKey(), value);
		return data;
	}
	
	int getIntValue(String key);
	
	public static String defaultKey() {
		return "VALUE";
	}
	
	public static PipelineData create() {
		return new BasicPipelineData();
	}
}

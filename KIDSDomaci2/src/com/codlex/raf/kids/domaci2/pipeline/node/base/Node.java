package com.codlex.raf.kids.domaci2.pipeline.node.base;

import java.util.List;

public interface Node {
	public int getID();
	public List<String> getParams();
	public void setParam(String parameterName, Object value);
	public javafx.scene.Node produceView();
}

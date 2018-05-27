package com.codlex.distributed.systems.homework1.peer.dht.content;

public enum IdType {

	Keyword, Video, Node, Unknown;

	public String getKey() {
		return name().substring(0, 1) + "-";
	}
}

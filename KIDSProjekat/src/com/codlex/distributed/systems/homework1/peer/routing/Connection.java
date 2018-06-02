package com.codlex.distributed.systems.homework1.peer.routing;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(of = {"node"})
@ToString
public class Connection {

	@Getter
	private final NodeInfo node;
	private long lastSeen;

	public Connection(NodeInfo node) {
		this.node = node;
		this.lastSeen = System.currentTimeMillis();
	}

	public void touch() {
		this.lastSeen = System.currentTimeMillis();
	}
}

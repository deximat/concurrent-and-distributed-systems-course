package com.codlex.distributed.systems.homework1.peer.routing;

import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.google.common.collect.ComparisonChain;


public class Connection implements Comparable<Connection> {


	public class Contact {
		public Node getNode() {
			throw new RuntimeException("Not implemented yet.");
		};

	}

	private final NodeInfo node;
	private long lastSeen;

	// TODO: create something like soft/hard death
	private int staleCount;

	public Connection(NodeInfo node) {
		this.node = node;
		this.lastSeen = System.currentTimeMillis();
	}

	public NodeInfo getNode() {
		return this.node;
	}

	public void touch() {
		this.lastSeen = System.currentTimeMillis();
	}

	@Override
	public boolean equals(Object c) {
		if (c instanceof Contact) {
			return ((Contact) c).getNode().equals(this.getNode());
		}
		return false;
	}

	@Override
	public int compareTo(Connection o) {
		if (this.getNode().equals(o.getNode())) {
			return 0;
		}

		// TODO: check if this is ok ordering
		return ComparisonChain.start().compare(this.lastSeen, o.lastSeen).result();
	}

	@Override
	public int hashCode() {
		return getNode().hashCode();
	}

	@Override
	public String toString() {
		return this.node.getId().toString();
	}
}

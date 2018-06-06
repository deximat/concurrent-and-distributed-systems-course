package com.codlex.distributed.systems.homework1.peer.routing;

import java.util.ArrayList;
import java.util.List;

import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;

import lombok.Getter;

public class Bucket {

	private final int distance;

	@Getter
	private final List<Connection> connections = new ArrayList<>();

	private final Node localNode;


	public Bucket(Node localNode, int distance) {
		this.localNode = localNode;
		this.distance = distance;
	}

	public synchronized void insert(final NodeInfo node) {
		insert(new Connection(this.localNode, node));
	}

	private synchronized void insert(Connection connection) {
		// this will maintain list sorted in order of most recently contacted node at the end
		// keeping ones that live long, and ingnoring newcomers, since statistically it is better.
		this.connections.remove(connection);
		if (this.connections.size() < Settings.BucketSize) {
			this.connections.add(connection);
		}
	}

	public synchronized String toString() {
		final String SEPARATOR = ", ";
		StringBuilder builder = new StringBuilder();
		builder.append(this.distance);
		builder.append(SEPARATOR);
		builder.append(this.connections);
		return builder.toString();
	}

	public synchronized void remove(Connection connection) {
		this.connections.remove(connection);
	}

	public synchronized void onNodeFailed(final NodeInfo info) {
		for (Connection connection : connections) {
			if (connection.getNode().equals(info)) {
				connection.onFailed(this::remove);
			}
		}
	}
}

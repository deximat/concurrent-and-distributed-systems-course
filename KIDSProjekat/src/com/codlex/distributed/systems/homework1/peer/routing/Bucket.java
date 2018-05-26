package com.codlex.distributed.systems.homework1.peer.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;

public class Bucket {


	/*
	 * TODO: If the sending node already exists in the recipient’s k-bucket, the recipient moves it to the tail of the list.
If the node is not already in the appropriate k-bucket and the bucket has fewer than k entries, then the recipient just inserts the new sender at the tail of the list.
If the appropriate k-bucket is full, however, then the recipient pings the k-bucket’s least-recently seen node to decide what to do. If the least recently seen node fails to respond,
it is evicted from the k-bucket and the new sender inserted at the tail. Otherwise, if the least-recently seen node
 responds, it is moved to the tail of the list, and the new sender’s contact is discarded.
	 */
	private final TreeSet<Connection> connections = new TreeSet<>();
	private final int depth;

	public Bucket(int depth) {
		this.depth = depth;
	}

	public synchronized void insert(Connection connection) {
		if (this.connections.contains(connection)) {
			Connection savedConnection = removeFromConnections(connection.getNode());
			savedConnection.touch();
			this.connections.add(savedConnection);
		} else {
			if (this.connections.size() < Settings.K) {
				this.connections.add(connection);
			} else {
				this.connections.add(connection);
				// System.out.println("OMFG Connections full, what should I do?");
			}
		}
	}

	private synchronized Connection removeFromConnections(NodeInfo n) {
		for (Connection Connection : this.connections) {
			if (Connection.getNode().equals(n)) {
				this.connections.remove(Connection);
				return Connection;
			}
		}

		throw new NoSuchElementException();
	}

	public synchronized void insert(final NodeInfo n) {
		insert(new Connection(n));
	}

	public synchronized List<Connection> getConnections() {
		final List<Connection> connections = new ArrayList<>();

		for (final Connection connection : this.connections) {
			connections.add(connection);
		}

		return connections;
	}


	public synchronized String toString() {
		final String SEPARATOR = ", ";
		StringBuilder builder = new StringBuilder();
		builder.append(this.depth);
		builder.append(SEPARATOR);
		builder.append(this.connections);
		return builder.toString();
	}

	public String getValue(int col) {
		switch (col) {
		case 0:
			return Integer.toString(this.depth);
		case 1:
			return this.connections.toString();
		default:
			return "";
		}
	}
}

package com.codlex.distributed.systems.homework1.peer.routing;

import java.util.ArrayList;
import java.util.List;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;

import lombok.Getter;

public class Bucket {

	private final int distance;

	@Getter
	private final List<Connection> connections = new ArrayList<>();


	public Bucket(int distance) {
		this.distance = distance;
	}

	public synchronized void insert(final NodeInfo node) {
		insert(new Connection(node));
	}

	public synchronized void insert(Connection connection) {
		// this will maintain list sorted in order of most recently contacted node at the end
		// keeping ones that live long, and ingnoring newcomers, since statistically it is better.
		this.connections.remove(connection);
		if (this.connections.size() < Settings.BucketSize) {
			this.connections.add(connection);
		}

		/*
		 * TODO: [FAILURE] If the sending node already exists in the recipient’s k-bucket, the recipient moves it to the tail of the list.
	If the node is not already in the appropriate k-bucket and the bucket has fewer than k entries, then the recipient just inserts the new sender at the tail of the list.
	If the appropriate k-bucket is full, however, then the recipient pings the k-bucket’s least-recently seen node to decide what to do. If the least recently seen node fails to respond,
	it is evicted from the k-bucket and the new sender inserted at the tail. Otherwise, if the least-recently seen node
	 responds, it is moved to the tail of the list, and the new sender’s contact is discarded.
		 */
	}

	public synchronized String toString() {
		final String SEPARATOR = ", ";
		StringBuilder builder = new StringBuilder();
		builder.append(this.distance);
		builder.append(SEPARATOR);
		builder.append(this.connections);
		return builder.toString();
	}
}

package com.codlex.distributed.systems.homework1.peer.routing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.id.KeyComparator;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class RoutingTable {

	private final NodeInfo localNode;
	private final Bucket[] buckets;

	public RoutingTable(final NodeInfo localNode) {
		this.localNode = localNode;

		this.buckets = new Bucket[KademliaId.ID_LENGTH];
		for (int i = 0; i < KademliaId.ID_LENGTH; i++) {
			buckets[i] = new Bucket(i);
		}

		insert(localNode);
	}

	public synchronized final void insert(Connection c) {
		this.buckets[getBucketId(c.getNode().getId())].insert(c);
	}

	public synchronized final void insert(NodeInfo n) {
		this.buckets[getBucketId(n.getId())].insert(n);
	}

	public final int getBucketId(KademliaId nid) {
		int bucketId = this.localNode.getId().getDistance(nid) - 1;
		return bucketId < 0 ? 0 : bucketId;
	}

	public synchronized final List<NodeInfo> findClosest(KademliaId target, int numNodesRequired) {
		TreeSet<NodeInfo> sortedSet = new TreeSet<>(new KeyComparator(target));
		sortedSet.addAll(getAllNodes());

		List<NodeInfo> closest = new ArrayList<>();

		int count = 0;
		for (NodeInfo node : sortedSet) {
			closest.add(node);
			if (++count == numNodesRequired) {
				break;
			}
		}

		return closest;
	}

	public synchronized final List<NodeInfo> getAllNodes() {
		List<NodeInfo> nodes = new ArrayList<>();
		for (Bucket bucket : this.buckets) {
			for (Connection connection : bucket.getConnections()) {
				nodes.add(connection.getNode());
			}
		}
		return nodes;
	}

	public void dump(final String id) {
		try {
			System.out.println("########### Table:");
			System.out.println(toString());
			Files.write(toString(), new File("dumps/" + id + ".txt"), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		final String SEPARATOR = "\n";
		for (int i = 0; i < this.buckets.length; i++) {
			Bucket bucket = this.buckets[i];
			builder.append(bucket);
			builder.append(SEPARATOR);
		}
		return builder.toString();
	}

	public int getBucketsCount() {
		return this.buckets.length;
	}

	public Bucket getBucket(int row) {
		return this.buckets[row];
	}
}

package com.codlex.distributed.systems.homework1.peer.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.id.KeyComparator;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;

public class RoutingTable {

	private final Node localNode;
	private final List<Bucket> buckets;

	public RoutingTable(final Node localNode) {
		this.localNode = localNode;

		this.buckets = new ArrayList<Bucket>();
		for (int i = 0; i <= KademliaId.ID_LENGTH_BITS; i++) {
			this.buckets.add(new Bucket(this.localNode, i));
		}

		insert(localNode.getInfo());
	}

	public synchronized final void insert(final NodeInfo node) {
		if (node.equals(Settings.bootstrapNode)) {
			// don't save bootstrap node, he is not regular node
			return;
		}

		this.buckets.get(getBucketId(node.getId())).insert(node);
	}

	private int getBucketId(KademliaId nodeId) {
		return this.localNode.getInfo().getId().getDistance(nodeId);
	}

	public synchronized final List<NodeInfo> findClosest(KademliaId target, int count) {
		final List<NodeInfo> allNodes = getAllNodes();
		Collections.sort(allNodes, new KeyComparator(target));
		return allNodes.subList(0, Math.min(allNodes.size(), count));
	}

	public synchronized final List<NodeInfo> getAllNodes() {
		final List<NodeInfo> nodes = new ArrayList<>();

		for (final Bucket bucket : this.buckets) {

			for (Connection connection : bucket.getConnections()) {
				nodes.add(connection.getNode());
			}

		}

		return nodes;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		final String SEPARATOR = "\n";
		for (int i = 0; i < this.buckets.size(); i++) {
			Bucket bucket = this.buckets.get(i);
			builder.append(bucket);
			builder.append(SEPARATOR);
		}
		return builder.toString();
	}

	public void onNodeFailed(final NodeInfo info) {
		this.buckets.get(getBucketId(info.getId())).onNodeFailed(info);
	}
}

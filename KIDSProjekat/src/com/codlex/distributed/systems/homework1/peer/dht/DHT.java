package com.codlex.distributed.systems.homework1.peer.dht;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.google.common.base.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = {"table"}, includeFieldNames = false)
public class DHT {

	@Data
	@AllArgsConstructor
	@ToString(includeFieldNames = false)
	private class DHTEntry {
		private final KademliaId key;
		private final String value;
	}

	public DHT(Node localNode) {
		this.localNode = localNode;
	}

	private final Node localNode;

	private final Map<KademliaId, DHTEntry> table = new HashMap<>();

	public synchronized void store(KademliaId key, String value) {
		this.table.put(key, new DHTEntry(key, value));
	}

	public synchronized boolean compareAndStore(KademliaId key, String value, String expected) {
		DHTEntry entry = this.table.get(key);
		if (Objects.equal(entry.value, expected)) {
			this.table.put(key, new DHTEntry(key, value));
			return true;
		} else {
			return false;
		}
	}

	public synchronized String get(KademliaId key) {
		return this.table.get(key).value;
	}

	public void refresh() {
		for (DHTEntry entry : this.table.values()) {
			List<NodeInfo> closestNodes = this.localNode.getRoutingTable().findClosest(entry.getKey(), Settings.K);
			System.out.println("Closest nodes: " + closestNodes);
			for (NodeInfo node : closestNodes) {
				this.localNode.sendMessage(node, Messages.Store,
						new StoreValueRequest(entry.getKey(), entry.getValue()), (response) -> {
							log.debug("Stored value: {} at {}", entry.getValue(), node);
						}, StoreValueResponse.class);
			}

			if (!closestNodes.contains(this.localNode.getInfo())) {
				this.table.remove(entry.key);
			}
		}
	}
}

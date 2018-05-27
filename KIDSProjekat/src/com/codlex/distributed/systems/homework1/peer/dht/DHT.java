package com.codlex.distributed.systems.homework1.peer.dht;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.google.common.base.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = { "table" }, includeFieldNames = false)
public class DHT {

	@Data
	@AllArgsConstructor
	@ToString(includeFieldNames = false)
	public static class DHTEntry {
		private final KademliaId key;
		private final String value;
	}

	public DHT(Node localNode) {
		this.localNode = localNode;
	}

	private final Node localNode;

	@Getter
	private final ObservableMap<KademliaId, DHTEntry> table = FXCollections.synchronizedObservableMap(FXCollections.observableMap(new HashMap<>()));

	public void store(KademliaId key, String value) {
		store(key, value, (nodesStoredOn) -> {});
	}

	public void store(KademliaId key, String value, Consumer<List<NodeInfo>> onStoredCallback) {
		new NodeLookup(this.localNode, key).execute((closestNodes) -> {

			for (NodeInfo node : closestNodes) {
				this.localNode.sendMessage(node, Messages.Store,
						new StoreValueRequest(this.localNode.getInfo(), key, value), (response) -> {
							log.debug("Stored value: {} at {}", value, node);
						}, StoreValueResponse.class);
			}

			onStoredCallback.accept(closestNodes);
		});
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
		DHTEntry result = this.table.get(key);
		if (result != null) {
			return result.value;
		} else {
			return null;
		}
	}

	public void refresh() {
		log.debug("Started refresh of DHT.");
		for (DHTEntry entry : this.table.values()) {
			store(entry.getKey(), entry.getValue(), (closestNodes) -> {
				if (!closestNodes.contains(this.localNode.getInfo())) {
					this.table.remove(entry.key);
					log.debug("I'm no longer closest to {}, removing {}", entry.getKey(), entry.getValue());
				}
			});
		}
	}

	public void put(KademliaId key, String value) {
		this.table.put(key, new DHTEntry(key, value));
	}
}

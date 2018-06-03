package com.codlex.distributed.systems.homework1.peer.operations;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest.ValueContainer;

public class StoreOperation {

	private Node localNode;
	private Consumer<List<NodeInfo>> callback;
	private DHTEntry value;
	private List<NodeInfo> closestNodes;
	private int nextNodeToStore;
	private int transmiting;
	private int storedNodes;

	public StoreOperation(Node localNode, DHTEntry value, Consumer<List<NodeInfo>> onStoredCallback) {
		this.localNode = localNode;
		this.callback = onStoredCallback;
		this.value = value;
	}

	public void store() {
		new NodeLookup(this.localNode, value.getId(), this::onNodesObtained).execute();
	}


	private synchronized void onNodesObtained(List<NodeInfo> closestNodes) {
		this.closestNodes = closestNodes;
		this.nextNodeToStore = 0;
		checkIfFinishedAndStoreIfNot();
	}

	private synchronized void checkIfFinishedAndStoreIfNot() {

		if (this.storedNodes == this.closestNodes.size()) {
			this.callback.accept(this.closestNodes);
		}

		int concurrencyAvailable = Settings.ConcurrencyParam - this.transmiting;

		while (this.nextNodeToStore < this.closestNodes.size()
				&& concurrencyAvailable > 0) {
			NodeInfo node = this.closestNodes.get(this.nextNodeToStore);

			this.localNode.sendMessage(node, Messages.Store,
					new StoreValueRequest(this.localNode.getInfo(), ValueContainer.pack(value)), (response) -> {
						onStoreSuccess();
					}, (e) -> {
						onStoreFailed();
					}, StoreValueResponse.class);

			this.transmiting++;
			this.nextNodeToStore++;
			concurrencyAvailable--;
		}
	}

	private synchronized void onStoreFailed() {
		this.storedNodes++;
		this.transmiting--;
		checkIfFinishedAndStoreIfNot();
	}

	private synchronized void onStoreSuccess() {
		this.storedNodes++;
		this.transmiting--;
		checkIfFinishedAndStoreIfNot();
	}
}

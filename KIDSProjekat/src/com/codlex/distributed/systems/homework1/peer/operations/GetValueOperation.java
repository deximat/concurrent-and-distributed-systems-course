package com.codlex.distributed.systems.homework1.peer.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.id.KeyComparator;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesRequest;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetValueOperation {

	// TODO: handle failed
	private final Node localNode;

	// TODO: handle concurrency well.
	private final Set<NodeInfo> nodes = new HashSet<>();
	private final Set<NodeInfo> asked = new HashSet<>();
	private final KademliaId lookupId;

	private AtomicBoolean valueFound = new AtomicBoolean();

	public GetValueOperation(Node node, KademliaId lookupId) {
		this.localNode = node;
		this.lookupId = lookupId;
	}

	public void execute(BiConsumer<NodeInfo, DHTEntry> callback) {
		this.nodes.add(this.localNode.getInfo());

		// TODO: check if all nodes are contacted then?
		handleNodes(this.localNode.getRoutingTable().getAllNodes(), callback);


		// TODO: do with timeout effort
		// this.localNode.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());
	}

	// TODO: SEEMS LIKE INFINITE LOOP CHECK THIS (RECURSIVE)
	private void handleNodes(List<NodeInfo> nodes, BiConsumer<NodeInfo, DHTEntry> callback) {
		// System.out.println("Number of nodes received: " + nodes.size());
		synchronized (this.nodes) {
			for (final NodeInfo info : new ArrayList<>(nodes)) {

				if (this.valueFound.get()) {
					break;
				}

				this.nodes.add(info);
				if (!this.asked.contains(info)) {
					this.localNode.sendMessage(info, Messages.Get,
							new GetValueRequest(this.localNode.getInfo(), this.lookupId), (response) -> {

								if (this.valueFound.get()) {
									return;
								}

								if (response.getValue() != null) {
									log.debug("Got value: " + response.getValue());
									this.valueFound.set(true);
									callback.accept(info, response.getValue().get());
								}
								this.asked.add(info); // TODO: should we do this before sending message?
								this.localNode.getRoutingTable().insert(info);
								handleNodes(response.getNodes(), callback);

							}, GetValueResponse.class);
				}
			}

			this.asked.add(this.localNode.getInfo());

//			if (isFinished()) {
//				log.debug("Finished getting closest nodes to: {}, nodes: {}. ", this.lookupId, getClosestNodes());
//				callback.accept(null, null);
//			}
		}
	}

	private boolean isFinished() {
		final List<NodeInfo> nodes = getClosestNodes();
		for (NodeInfo info : nodes) {
			if (!this.asked.contains(info)) {
				return false;
			}
		}
		return nodes.size() >= Settings.K;
	}

	public List<NodeInfo> getClosestNodes() {
		synchronized (this.nodes) {
			return this.nodes.stream().sorted(new KeyComparator(this.lookupId)).limit(Settings.K)
					.collect(Collectors.toList());
		}
	}
}

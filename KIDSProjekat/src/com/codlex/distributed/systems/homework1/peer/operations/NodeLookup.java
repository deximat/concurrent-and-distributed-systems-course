package com.codlex.distributed.systems.homework1.peer.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.id.KeyComparator;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesRequest;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesResponse;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeLookup {

	// TODO: [FAILURES] handle failed
	private final Node localNode;
	// TODO: [FAILURES] handle concurrency well.
	private final Set<NodeInfo> nodes = new HashSet<>();
	private final Set<NodeInfo> asked = new HashSet<>();
	private final KademliaId lookupId;
	private final Consumer<List<NodeInfo>> callback;

	private ScheduledFuture<?> timeoutFuture;

	private static final ScheduledExecutorService SCHEDULER = (ScheduledExecutorService) Executors
			.newSingleThreadScheduledExecutor();

	public NodeLookup(Node localNode, KademliaId lookupId, Consumer<List<NodeInfo>> callback) {
		this.localNode = localNode;
		this.lookupId = lookupId;
		this.callback = callback;
	}

	private synchronized void processTimeout() {
		log.trace("Node Lookup timeouted");
		this.callback.accept(getClosestNodes());
	}

	public synchronized void execute() {

		// SIMPLE TIMEOUT
		this.timeoutFuture = SCHEDULER.schedule(this::processTimeout, 2, TimeUnit.SECONDS);

		this.nodes.add(this.localNode.getInfo());
		this.asked.add(this.localNode.getInfo());

		handleNodes(this.localNode.getRoutingTable().getAllNodes());

		// TODO: [FAILURES] do with timeout effort
		// this.localNode.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());
	}

	private void handleNodes(List<NodeInfo> nodes) {
		// if (nodes.isEmpty()) {
		// log.debug("No nodes to query, returning emptr. ", this.lookupId,
		// getClosestNodes());
		// this.timeoutFuture.cancel(false);
		// callback.accept(ImmutableList.of());
		// }

		for (final NodeInfo info : new ArrayList<>(nodes)) {
			this.nodes.add(info);
			if (!this.asked.contains(info)) {
				this.localNode.sendMessage(info, Messages.FindNodes,
						new FindNodesRequest(this.localNode.getInfo(), this.lookupId), (response) -> {
							this.asked.add(info); // TODO: [FAILURES] should we do this
													// before sending message?
							handleNodes(response.getNodes());
						}, FindNodesResponse.class);
			}
		}

		if (isFinished()) {
			log.trace("Finished getting closest nodes to: {}, nodes: {}. ", this.lookupId.toHexShort(),
					getClosestNodes());
			this.timeoutFuture.cancel(false);
			this.callback.accept(getClosestNodes());
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

	public synchronized List<NodeInfo> getClosestNodes() {
		return this.nodes.stream().sorted(new KeyComparator(this.lookupId)).limit(Settings.K)
				.collect(Collectors.toList());

	}
}

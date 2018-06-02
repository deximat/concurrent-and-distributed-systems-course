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

	private ScheduledFuture<?> timeoutFuture;

	private Consumer<List<NodeInfo>> callback;

	private static final ScheduledExecutorService SCHEDULER = (ScheduledExecutorService) Executors.newSingleThreadScheduledExecutor();

	public NodeLookup(Node localNode, KademliaId lookupId, Consumer<List<NodeInfo>> callback) {
		this.localNode = localNode;
		this.lookupId = lookupId;
		this.callback = callback;
	}

	private void processTimeout() {
		log.debug("Lookup timeouted.");
		this.callback.accept(ImmutableList.of());
	}

	public void execute() {

		this.timeoutFuture = SCHEDULER.schedule(this::processTimeout, 5, TimeUnit.SECONDS);
		log.debug("Scheduled timeout.");

		this.nodes.add(this.localNode.getInfo());
		this.asked.add(this.localNode.getInfo());

		handleNodes(this.localNode.getRoutingTable().getAllNodes(), callback);


		// TODO: [FAILURES] do with timeout effort
		// this.localNode.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());
	}

	private void handleNodes(List<NodeInfo> nodes, Consumer<List<NodeInfo>> callback) {
		synchronized (this.nodes) {

			for (final NodeInfo info : new ArrayList<>(nodes)) {
				this.nodes.add(info);
				if (!this.asked.contains(info)) {
					this.localNode.sendMessage(info, Messages.FindNodes,
							new FindNodesRequest(this.localNode.getInfo(), this.lookupId), (response) -> {
								this.asked.add(info); // TODO: should we do this before sending message?
								this.localNode.getRoutingTable().insert(info);
								handleNodes(response.getNodes(), callback);
							}, FindNodesResponse.class);
				}
			}


			if (isFinished()) {
				log.debug("Finished getting closest nodes to: {}, nodes: {}. ", this.lookupId, getClosestNodes());
				this.timeoutFuture.cancel(false);
				callback.accept(getClosestNodes());
			}
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

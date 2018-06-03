package com.codlex.distributed.systems.homework1.peer.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeLookup {

	enum NodeStatus {
		Unasked, Awaiting, Asked, Failed;
	}

	private final Node localNode;
	private final KademliaId lookupId;
	private final Consumer<List<NodeInfo>> callback;
	private final Map<NodeInfo, NodeStatus> statuses;
	private ScheduledFuture<?> timeoutFuture;
	private int k;
	private FindNodesRequest request;
	private boolean timeouted;

	private static final ScheduledExecutorService SCHEDULER = (ScheduledExecutorService) Executors
			.newSingleThreadScheduledExecutor();

	public NodeLookup(Node localNode, KademliaId lookupId, Consumer<List<NodeInfo>> callback) {
		this.localNode = localNode;
		this.lookupId = lookupId;
		this.callback = callback;
		this.statuses = new TreeMap<>(new KeyComparator(this.lookupId));
		this.k = Settings.K;
		this.request = new FindNodesRequest(this.localNode.getInfo(), this.lookupId);
	}

	private synchronized void processTimeout() {
		log.error("Node Lookup timeouted");
		this.callback.accept(getClosestNodesNotFailed(NodeStatus.Asked));
		this.timeouted = true;
	}

	public synchronized void execute() {
		// schedule timeout for whole operation
		this.timeoutFuture = SCHEDULER.schedule(this::processTimeout, 50000, TimeUnit.SECONDS);

		this.statuses.put(this.localNode.getInfo(), NodeStatus.Unasked);

		checkFinishAndProcess();
	}

	private List<NodeInfo> getClosestNodesNotFailed(NodeStatus desiredStatus) {
		List<NodeInfo> nodes = new ArrayList<>();
		int topNodes = 0;
		for (Entry<NodeInfo, NodeStatus> entry : this.statuses.entrySet()) {
			final NodeStatus status = entry.getValue();

			// skip failed, without counting them
			if (NodeStatus.Failed.equals(status)) {
				continue;
			}

			if (status.equals(desiredStatus)) {
				nodes.add(entry.getKey());
			}
			topNodes++;

			if (topNodes >= this.k) {
				break;
			}
		}
		return nodes;
	}

	private boolean isFinished() {
		boolean noAwaitingNodes = getClosestNodesNotFailed(NodeStatus.Awaiting).isEmpty();
		boolean noUnaskedInClosest = getClosestNodesNotFailed(NodeStatus.Unasked).isEmpty();
		return noUnaskedInClosest && noAwaitingNodes;
	}

	private synchronized void checkFinishAndProcess() {
		if (this.timeouted) {
			return;
		}

		if (isFinished()) {
			onFinish();
		} else {
			int availableConcurrency = Settings.ConcurrencyParam - getAwaitingCount();
			List<NodeInfo> unasked = getClosestNodesNotFailed(NodeStatus.Unasked);
			for (NodeInfo unaskedNode : unasked) {
				if (availableConcurrency <= 0) {
					break;
				}
				this.statuses.put(unaskedNode, NodeStatus.Awaiting);
				ask(unaskedNode, this::processResponse, this::processError);
			}
		}
	}

	private synchronized void processResponse(NodeInfo node, List<NodeInfo> closestNodes) {
		if (this.timeouted) {
			return;
		}
		this.statuses.put(node, NodeStatus.Asked);
		addNodes(closestNodes);
		checkFinishAndProcess();
	}

	private synchronized void processError(NodeInfo node, Throwable error) {
		if (this.timeouted) {
			return;
		}
		this.statuses.put(node, NodeStatus.Failed);
		checkFinishAndProcess();
	}

	private void ask(NodeInfo unaskedNode, BiConsumer<NodeInfo, List<NodeInfo>> nodesConsumer,
			BiConsumer<NodeInfo, Throwable> errorConsumer) {
		this.localNode.sendMessage(unaskedNode, Messages.FindNodes, this.request, (response) -> {
			nodesConsumer.accept(unaskedNode, response.getNodes());
		}, (e) -> {
			errorConsumer.accept(unaskedNode, e);
		}, FindNodesResponse.class);
	}

	private void onFinish() {
		this.timeoutFuture.cancel(false);
		List<NodeInfo> closestNodes = getClosestNodesNotFailed(NodeStatus.Asked);
		log.trace("Finished getting closest nodes to: {}, size = {} ", this.lookupId.toHexShort(), closestNodes.size());
		this.callback.accept(closestNodes);
	}

	private int getAwaitingCount() {
		return getClosestNodesNotFailed(NodeStatus.Awaiting).size();
	}

	private synchronized void addNodes(final List<NodeInfo> nodes) {
		for (NodeInfo node : nodes) {
			if (!this.statuses.containsKey(node)) {
				this.statuses.put(node, NodeStatus.Unasked);
			}
		}
	}
}

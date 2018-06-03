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

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.id.KeyComparator;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetValueOperation {

	enum NodeStatus {
		Unasked, Awaiting, Asked, Failed;
	}

	private final Node localNode;
	private final KademliaId lookupId;
	private final BiConsumer<NodeInfo, DHTEntry> callback;
	private final Map<NodeInfo, NodeStatus> statuses;
	private ScheduledFuture<?> timeoutFuture;
	private int k;
	private GetValueRequest request;
	private boolean timeouted;

	private NodeInfo valueNode;
	private DHTEntry value;

	private static final ScheduledExecutorService SCHEDULER = (ScheduledExecutorService) Executors
			.newSingleThreadScheduledExecutor();

	public GetValueOperation(Node localNode, KademliaId lookupId, boolean fullData, BiConsumer<NodeInfo, DHTEntry> callback) {
		this.localNode = localNode;
		this.lookupId = lookupId;
		this.callback = callback;
		this.statuses = new TreeMap<>(new KeyComparator(this.lookupId));
		this.k = Settings.K;
		this.request = new GetValueRequest(this.localNode.getInfo(), this.lookupId, fullData);
	}

	private synchronized void processTimeout() {
		log.error("Node Lookup timeouted");
		this.callback.accept(null, null);
		this.timeouted = true;
	}

	public synchronized void execute() {
		// schedule timeout for whole operation
		this.timeoutFuture = SCHEDULER.schedule(this::processTimeout, 5, TimeUnit.SECONDS);
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
		boolean hasValue = this.value != null;
		boolean noAwaitingNodes = getClosestNodesNotFailed(NodeStatus.Awaiting).isEmpty();
		boolean noUnaskedInClosest = getClosestNodesNotFailed(NodeStatus.Unasked).isEmpty();
		return hasValue || (noUnaskedInClosest && noAwaitingNodes);
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

	private synchronized void processResponse(NodeInfo node, GetValueResponse response) {
		if (this.timeouted) {
			return;
		}

		this.statuses.put(node, NodeStatus.Asked);
		if (response.getValue() != null) {
			this.value = response.getValue().get();
			this.valueNode = node;
		} else {
			addNodes(response.getNodes());
		}
		checkFinishAndProcess();
	}

	private synchronized void processError(NodeInfo node, Throwable error) {
		if (this.timeouted || this.value != null) {
			return;
		}
		this.statuses.put(node, NodeStatus.Failed);
		checkFinishAndProcess();
	}

	private void ask(NodeInfo unaskedNode, BiConsumer<NodeInfo, GetValueResponse> nodesConsumer,
			BiConsumer<NodeInfo, Throwable> errorConsumer) {
		this.localNode.sendMessage(unaskedNode, Messages.Get, this.request, (response) -> {
			nodesConsumer.accept(unaskedNode, response);
		}, (e) -> {
			errorConsumer.accept(unaskedNode, e);
		}, GetValueResponse.class);
	}

	private void onFinish() {
		this.timeoutFuture.cancel(false);
		log.trace("Finished getting value for {}", this.lookupId.toHexShort());
		this.callback.accept(this.valueNode, this.value);
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

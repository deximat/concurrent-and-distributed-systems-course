package com.codlex.distributed.systems.homework1.peer.routing;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.PingRequest;
import com.codlex.distributed.systems.homework1.peer.messages.PingResponse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(of = { "node" })
@ToString(exclude = {"localNode", "killFuture"})
@Slf4j
public class Connection {
	public static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	private final Node localNode;

	@Getter
	private final NodeInfo node;
	private long lastSeen;

	private ScheduledFuture<?> killFuture;

	public Connection(Node localNode, NodeInfo node) {
		this.localNode = localNode;
		this.node = node;
		this.lastSeen = System.currentTimeMillis();
	}

	public void touch() {
		this.lastSeen = System.currentTimeMillis();
		if (this.killFuture != null) {
			this.killFuture.cancel(false);
			this.killFuture = null;
		}
	}

	public void onFailed(final Consumer<Connection> remove) {
		if (this.killFuture == null) {
			// in Settings.HardTimeoutMillis check again
			this.killFuture = SCHEDULER.schedule(() -> {
				this.localNode.sendMessage(this.node, Messages.Ping, new PingRequest(this.localNode.getInfo()),
						(message) -> {
							// if everything went fine we shouldn't do anything, system will resolve itself
						}, (message) -> {
							// node failed again, so we will kill him
							remove.accept(this);
							this.killFuture = null;
							log.error("{} is not alive, unfortunately he is dead now.", this.node);
						}, PingResponse.class);
			}, Settings.HardTimeoutMillis, TimeUnit.MILLISECONDS);
		}
	}
}

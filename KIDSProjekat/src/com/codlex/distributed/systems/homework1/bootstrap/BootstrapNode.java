package com.codlex.distributed.systems.homework1.bootstrap;

import java.util.ArrayList;
import java.util.List;

import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinRequest;
import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinResponse;
import com.codlex.distributed.systems.homework1.core.handers.JsonHandler;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.OnNodeDeathRequest;
import com.codlex.distributed.systems.homework1.peer.messages.OnNodeDeathResponse;
import com.codlex.distributed.systems.homework1.starter.Log4jConfigurator;
import com.google.common.collect.ImmutableList;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootstrapNode {

	private class BootstrapNodesBucket {

		private List<NodeInfo> nodes = new ArrayList<>();

		public synchronized void remove(NodeInfo node) {
			this.nodes.remove(node);
			log.debug("Removed {}, bootstrap now has: {}", node, this.nodes);
		}

		public synchronized void insert(NodeInfo node) {
			this.nodes.remove(node);
			if (this.nodes.size() < Settings.K) {
				this.nodes.add(node);
				log.debug("Added {}, bootstrap now has: {}", node, this.nodes);
			}
		}

		public synchronized List<NodeInfo> get() {
			return ImmutableList.copyOf(this.nodes);
		}
	}

	private final BootstrapNodesBucket bootstrapNodes = new BootstrapNodesBucket();
	private final HttpServer server;

	private NodeInfo nodeInfo;

	public BootstrapNode(final NodeInfo info) {
		this.nodeInfo = info;
		this.server = createServer(info.port);
	}

	private HttpServer createServer(int port) {
		final Router router = Router.router(Vertx.vertx());
		router.route().handler(io.vertx.ext.web.handler.BodyHandler.create());
		router.route(HttpMethod.POST, Messages.Join.getAddress())
				.handler(new JsonHandler<JoinRequest, JoinResponse>(JoinRequest.class) {
					public JoinResponse callback(JoinRequest message) {
						BootstrapNode.this.bootstrapNodes.insert(message.getInfo());
						final List<NodeInfo> bootstrapNodes = BootstrapNode.this.bootstrapNodes.get();
						log.debug("{} hello there, bootstrap nodes for you: ", message.getInfo(), bootstrapNodes);
						return new JoinResponse(bootstrapNodes);
					}
				});

		router.route(HttpMethod.POST, Messages.OnNodeDeath.getAddress())
		.handler(new JsonHandler<OnNodeDeathRequest, OnNodeDeathResponse>(OnNodeDeathRequest.class) {
			public OnNodeDeathResponse callback(OnNodeDeathRequest message) {
				log.debug("{} notified that {} is dead.", message.getMe(), message.getDeadNode());
				BootstrapNode.this.bootstrapNodes.remove(message.getDeadNode());
				BootstrapNode.this.bootstrapNodes.insert(message.getMe());
				return new OnNodeDeathResponse();
			}
		});

		HttpServer server = Vertx.vertx().createHttpServer();
		server.requestHandler(router::accept);
		server.listen(port);
		return server;
	}

	public static void main(String[] args) {
		Log4jConfigurator.configure("bootstrap.log");
		log.debug("######### Starting bootstrap on port: {} ", Settings.bootstrapNode.port);
		new BootstrapNode(Settings.bootstrapNode);
	}

}

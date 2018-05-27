package com.codlex.distributed.systems.homework1.bootstrap;

import java.util.concurrent.atomic.AtomicReference;

import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinRequest;
import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinResponse;
import com.codlex.distributed.systems.homework1.core.handers.JsonHandler;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootstrapNode {

	private AtomicReference<NodeInfo> bootstrapNode = new AtomicReference<>();
	private HttpServer server;
	private NodeInfo nodeInfo;

	public BootstrapNode(final NodeInfo info) {
		this.nodeInfo = info;
		this.server = createServer(info.port);
		log.debug("Started bootstrap on port: {} ", info.port);
	}

	private HttpServer createServer(int port) {
		final Router router = Router.router(Vertx.vertx());
		router.route().handler(io.vertx.ext.web.handler.BodyHandler.create());
		router.route(HttpMethod.POST, "/join")
				.handler(new JsonHandler<JoinRequest, JoinResponse>(JoinRequest.class) {
					public JoinResponse callback(JoinRequest message) {
						log.debug("Join executed for " + message.getInfo() + " giving him bootstrap node: " + bootstrapNode.get());
						// set if null
						bootstrapNode.compareAndSet(null, message.getInfo());
						return new JoinResponse(BootstrapNode.this.bootstrapNode.get());
					}
				});

		HttpServer server = Vertx.vertx().createHttpServer();
		server.requestHandler(router::accept);
		server.listen(port);
		return server;
	}


	public static void main(String[] args) {
		new BootstrapNode(Settings.bootstrapNode);
	}

}
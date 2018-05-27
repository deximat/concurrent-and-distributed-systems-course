package com.codlex.distributed.systems.homework1.peer;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinRequest;
import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinResponse;
import com.codlex.distributed.systems.homework1.core.handers.JsonHandler;
import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.DHT;
import com.codlex.distributed.systems.homework1.peer.messages.ConnectMessageRequest;
import com.codlex.distributed.systems.homework1.peer.messages.ConnectMessageResponse;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesRequest;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesResponse;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.codlex.distributed.systems.homework1.peer.operations.GetValueOperation;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.codlex.distributed.systems.homework1.peer.routing.RoutingTable;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = { "info" })
public class Node {

	@Getter
	private final NodeInfo info;

	private final HttpServer server;
	private final HttpClient client;

	@Getter
	private RoutingTable routingTable;

	private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	private final AtomicReference<String> task = new AtomicReference<String>("IDLE");

	@Getter
	private final DHT dht = new DHT(this);

	private Region region;

	public Node(int port) {
//		try {
			this.info = new NodeInfo(new KademliaId(), "localhost", port);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}

		this.server = createServer();
		this.client = createClient();
		this.routingTable = new RoutingTable(this.info);

		// new NodeGui(this);

	}

	// TODO: call this
	public void onBootstrapFinished() {
		SCHEDULER.scheduleWithFixedDelay(this::refresh, 0, Settings.refreshInterval, TimeUnit.MILLISECONDS);
	}

	public <Response extends Serializable> void sendMessage(final NodeInfo info, Messages messageType,
			Serializable message, Consumer<Response> callback, Class<Response> responseClass) {
		log.debug("Sending message to: {} messsage: {}", info, messageType.getAddress());

		this.client.post(info.port, info.address, messageType.getAddress(), (response) -> {
			response.bodyHandler((body) -> {
				log.debug("Response received from: {} response for: {}", info, messageType.getAddress());

				callback.accept(new Gson().fromJson(body.toString(), responseClass));
			});
		}).end(new Gson().toJson(message));
	}

	private HttpClient createClient() {
		return Vertx.vertx().createHttpClient();
	}

	private HttpServer createServer() {
		final Router router = Router.router(Vertx.vertx());
		router.route().handler(io.vertx.ext.web.handler.BodyHandler.create());
		router.route(HttpMethod.POST, Messages.Connect.getAddress())
				.handler(new JsonHandler<ConnectMessageRequest, ConnectMessageResponse>(ConnectMessageRequest.class) {
					public ConnectMessageResponse callback(ConnectMessageRequest message) {
						log.debug("Received connect message from: {}", message.node);
						Node.this.routingTable.insert(message.node);
						return new ConnectMessageResponse(23);
					}
				});

		router.route(HttpMethod.POST, Messages.FindNodes.getAddress())
				.handler(new JsonHandler<FindNodesRequest, FindNodesResponse>(FindNodesRequest.class) {
					public FindNodesResponse callback(FindNodesRequest message) {
						Node.this.routingTable.insert(message.getNode());
						return new FindNodesResponse(
								Node.this.routingTable.findClosest(message.getLookupId(), Settings.K));
					}
				});


		router.route(HttpMethod.POST, Messages.Get.getAddress())
		.handler(new JsonHandler<GetValueRequest, GetValueResponse>(GetValueRequest.class) {
			public GetValueResponse callback(GetValueRequest message) {
				Node.this.routingTable.insert(message.getNode());
				String value = Node.this.dht.get(message.getLookupId());
				if (value != null) {
					return new GetValueResponse(ImmutableList.of(), value);
				} else {
					return new GetValueResponse(Node.this.routingTable.findClosest(message.getLookupId(), Settings.K), null);
				}
			}
		});

		router.route(HttpMethod.POST, Messages.Store.getAddress())
				.handler(new JsonHandler<StoreValueRequest, StoreValueResponse>(StoreValueRequest.class) {
					public StoreValueResponse callback(StoreValueRequest message) {
						Node.this.routingTable.insert(message.getNode());
						Node.this.dht.put(message.getKey(), message.getValue());
						return new StoreValueResponse();
					}
				});

		HttpServer server = Vertx.vertx().createHttpServer();
		server.requestHandler(router::accept);
		server.listen(this.info.port);
		System.out.println("Started server listening on: " + this.info);
		return server;
	}

	public final void bootstrap() {
		sendMessage(Settings.bootstrapNode, Messages.Join, new JoinRequest(this.info), (response) -> {
			System.out.println("Bootstrapning node " + this.info);
			System.out.println("b: " + response.getBootstrapNode());
			bootstrap(response.getBootstrapNode());
		}, JoinResponse.class);
	}

	public synchronized final void bootstrap(NodeInfo node) {
		if (node.equals(this.info)) {
			log.debug("{} has no need to bootstrap, I'm alone.", this.info);
			onBootstrapFinished();
			return;
		}

		log.debug(" Bootstraping of {} starting ", this.info);
		this.task.set("BOOTSTRAPING");

		sendMessage(node, Messages.Connect, new ConnectMessageRequest(this.info), (response) -> {
			this.routingTable.insert(node);

			final NodeLookup lookup = new NodeLookup(this, this.info.getId());
			lookup.execute((nodes) -> {
//				// TODO: check if this is needed
//				 refreshBuckets();

				log.debug("## Bootstraping of {} finished ", this.info);
				onBootstrapFinished();

				this.task.set("IDLE");
			});

			// this.routingTable.dump(this.info.getId().toString());
		}, ConnectMessageResponse.class);
	}

	private void refresh() {
		refreshBuckets();
		this.dht.refresh();
	}

	private void refreshBuckets() {
		// TODO: To avoid redundant store RPCs for the same content from different nodes,
		// a node only transfers a KV pair if its own ID is closer to the key than are the
		// IDs of other nodes.
//		for (int i = 1; i < KademliaId.ID_LENGTH; i++) {
//			final KademliaId current = this.info.getId().generateNodeIdByDistance(i);
//			new NodeLookup(this, current).execute();
//		}
	}

	public String toString() {
		return this.info.getId().toString();
	}

	public String getCurrentTask() {
		return this.task.get();
	}

	public void setRegion(Region region) {
		this.region = region;
	}


	public void findValue(KademliaId key, Consumer<String> callback) {
		new GetValueOperation(this, key).execute(callback);
	}

	public void search(String text, Consumer<List<String>> callback) {
		List<String> results = new ArrayList<>();
		String[] keywords = text.split(" ");
		final AtomicInteger expectedValues = new AtomicInteger(keywords.length);
		for (String keyword : keywords) {
			KademliaId key = new KademliaId(keyword);
			findValue(key, (value) -> {
				synchronized (results) {
					results.add(value);
					if (expectedValues.decrementAndGet() == 0) {
						callback.accept(results);
					}
				}
			});
		}
		callback.accept(results);
	}

	public void uploadVideo(String name, Consumer<Object> callback) {
		KademliaId videoId = new KademliaId(name);
		dht.store(videoId, name);

		// TODO: implement video object
		DELAYER.schedule(() -> {
			callback.accept("DONE, distance from this node: " + videoId.getDistance(this.info.getId()));
		}, 1000, TimeUnit.MILLISECONDS);
	}

	private static final ScheduledExecutorService DELAYER = Executors.newSingleThreadScheduledExecutor();
}

package com.codlex.distributed.systems.homework1.peer;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.text.similarity.LevenshteinDistance;

import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinRequest;
import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinResponse;
import com.codlex.distributed.systems.homework1.core.handers.JsonHandler;
import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.streaming.StreamingServer;
import com.codlex.distributed.systems.homework1.peer.dht.DHT;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;
import com.codlex.distributed.systems.homework1.peer.dht.content.Keyword;
import com.codlex.distributed.systems.homework1.peer.dht.content.Video;
import com.codlex.distributed.systems.homework1.peer.messages.ConnectMessageRequest;
import com.codlex.distributed.systems.homework1.peer.messages.ConnectMessageResponse;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesRequest;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesResponse;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest.ValueContainer;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.codlex.distributed.systems.homework1.peer.operations.GetValueOperation;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.codlex.distributed.systems.homework1.peer.routing.RoutingTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.Router;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = { "info" })
public class Node {

	@Getter
	private final IntegerProperty currentStreamers = new SimpleIntegerProperty();

	@Getter
	private NodeInfo info;

	private final HttpServer server;
	private final HttpClient client;

	@Getter
	private RoutingTable routingTable;

	private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	private final AtomicReference<String> task = new AtomicReference<String>("IDLE");

	@Getter
	private final DHT dht = new DHT(this);

	@Getter
	private Region region = Region.Serbia;

	private StreamingServer streamingServer;

	public Node(int port, int streamingPort) {
//		try {
			this.info = new NodeInfo(new KademliaId(IdType.Node, this.region), "localhost", port, streamingPort);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}

		log.debug("{}, streaming port: {}", port, streamingPort);
		this.server = createServer();
		this.client = createClient();
		this.routingTable = new RoutingTable(this.info);
		this.streamingServer = new StreamingServer(this, streamingPort);
	}

	// TODO: call this
	public void onBootstrapFinished() {
		SCHEDULER.scheduleWithFixedDelay(this::refresh, 0, Settings.refreshInterval, TimeUnit.MILLISECONDS);
	}

	public <Response extends Serializable> void sendMessage(final NodeInfo info, Messages messageType,
			Serializable message, Consumer<Response> callback, Class<Response> responseClass) {
		// log.debug("Sending message to: {} messsage: {}", info, messageType.getAddress());

		this.client.post(info.port, info.address, messageType.getAddress(), (response) -> {
			response.bodyHandler((body) -> {
				// log.debug("Response received from: {} response for: {}", info, messageType.getAddress());

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
//						log.debug("Received connect message from: {}", message.node);
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
				DHTEntry value = Node.this.dht.get(message.getLookupId());
				if (value != null) {
					return new GetValueResponse(ImmutableList.of(), ValueContainer.pack(value));
				} else {
					return new GetValueResponse(Node.this.routingTable.findClosest(message.getLookupId(), Settings.K), null);
				}
			}
		});

		router.route(HttpMethod.POST, Messages.Store.getAddress())
				.handler(new JsonHandler<StoreValueRequest, StoreValueResponse>(StoreValueRequest.class) {
					public StoreValueResponse callback(StoreValueRequest message) {
						Node.this.routingTable.insert(message.getNode());
						Node.this.dht.put(message.getValue());
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
		this.info = new NodeInfo(new KademliaId(IdType.Node, region), this.info.address, this.info.port, this.info.streamingPort);
	}


	public void findValue(KademliaId key, BiConsumer<NodeInfo, DHTEntry> callback) {
		new GetValueOperation(this, key).execute(callback);
	}

	public void search(String text, Consumer<List<String>> callback) {
		Set<String> results = new ConcurrentHashSet<>();
		String[] keywords = text.split(" ");

		final AtomicInteger expectedValues = new AtomicInteger(keywords.length);
		for (String keyword : keywords) {
			KademliaId key = new KademliaId(IdType.Keyword, this.region, keyword);
			findValue(key, (node, value) -> {
				synchronized (results) {

					log.debug("Found {} for {}", value, keyword);

					if (value != null) {
						// we know it is keyword since we looked for keyword
						Keyword keywordObject = (Keyword) value;
						results.addAll(keywordObject.getVideos());
					}

					if (expectedValues.decrementAndGet() == 0) {
						List<String> videos = new ArrayList<>(results);

						Collections.sort(videos, (a, b) -> {
							LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
							int aDist = distanceCalculator.apply(text, a);
							int bDist = distanceCalculator.apply(text, b);
							return Ints.compare(aDist, bDist);
						});

						callback.accept(videos);
					}
				}
			});
		}
	}

	public void uploadVideo(String name, Consumer<Object> callback) {
		// TODO: DO UPLOAD FOR ALL REGIONS
		for (String keyword : name.split(" ")) {
			Keyword keywordObject = new Keyword(new KademliaId(IdType.Keyword, this.region, keyword.trim()), ImmutableSet.of(name));
			dht.store(keywordObject);
		}

		KademliaId videoId = new KademliaId(IdType.Video, this.region, name);
		Video video = new Video(videoId, null);
		dht.store(video);

		// TODO: implement video object
		DELAYER.schedule(() -> {
			callback.accept("DONE, distance from this node: " + videoId.getDistance(this.info.getId()));
		}, 1000, TimeUnit.MILLISECONDS);
	}

	private static final ScheduledExecutorService DELAYER = Executors.newSingleThreadScheduledExecutor();

	public String getVideoForStreaming(final KademliaId id) {
//		Video video = (Video) this.dht.get(id);
//		video.incrementViews();
		Platform.runLater(() -> {
			log.debug("Incremending number of streamers.");
			this.currentStreamers.add(1);
		});

//		return video.getVideoData();

		return "/Users/dejanpe/ma.mp4";
	}

	public void onVideoStreamingEnd() {
		Platform.runLater(() -> {
			log.debug("Decrementing number of streamers.");
			this.currentStreamers.subtract(1);
		});
	}
}

package com.codlex.distributed.systems.homework1.peer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.text.similarity.LevenshteinDistance;

import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinRequest;
import com.codlex.distributed.systems.homework1.bootstrap.messages.JoinResponse;
import com.codlex.distributed.systems.homework1.core.GsonProvider;
import com.codlex.distributed.systems.homework1.core.handers.JsonHandler;
import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.core.streaming.StreamingServer;
import com.codlex.distributed.systems.homework1.peer.dht.DHT;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;
import com.codlex.distributed.systems.homework1.peer.dht.content.Keyword;
import com.codlex.distributed.systems.homework1.peer.dht.content.Video;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesRequest;
import com.codlex.distributed.systems.homework1.peer.messages.FindNodesResponse;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.GetValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.PingRequest;
import com.codlex.distributed.systems.homework1.peer.messages.PingResponse;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest.ValueContainer;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.codlex.distributed.systems.homework1.peer.messages.StreamingStartedRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StreamingStartedResponse;
import com.codlex.distributed.systems.homework1.peer.operations.GetValueOperation;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.codlex.distributed.systems.homework1.peer.operations.RefreshBucketOperation;
import com.codlex.distributed.systems.homework1.peer.operations.StoreOperation;
import com.codlex.distributed.systems.homework1.peer.routing.RoutingTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.Router;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = { "info" }, includeFieldNames = false)
public class Node {

	@Getter
	private NodeInfo info;

	private HttpServer server;
	private HttpClient client;

	@Getter
	private RoutingTable routingTable;

	public final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	@Getter
	private final StringProperty task = new SimpleStringProperty("NOT CONNECTED");

	@Getter
	private final DHT dht = new DHT(this);

	@Getter
	private Region region = Region.Europe;

	private StreamingServer streamingServer;

	private String videoDirectory;

	private int port;
	private int streamingPort;

	@Getter
	private AtomicBoolean inited = new AtomicBoolean();

	public Node(int port, int streamingPort) {
		this.port = port;
		this.streamingPort = streamingPort;
	}

	public void init() {
		this.inited.set(true);
		this.info = new NodeInfo(new KademliaId(IdType.Node, this.region), HostGetter.getUnsafe(), this.port,
				this.streamingPort);
		this.server = createServer();
		this.client = createClient();
		this.routingTable = new RoutingTable(this);
		this.streamingServer = new StreamingServer(this, streamingPort);
		createFolderForVideos();
	}

	private HttpClient createClient() {
		VertxOptions options = new VertxOptions();
		options.setEventLoopPoolSize(16);
		options.setWorkerPoolSize(16);
		Vertx vertx = Vertx.vertx(options);
		return vertx.createHttpClient();
	}

	public void onBootstrapFinished() {
		SCHEDULER.scheduleWithFixedDelay(this::refresh, 0, Settings.RefreshIntervalSeconds, TimeUnit.SECONDS);
	}

	public <Response extends Serializable> void sendMessage(final NodeInfo info, Messages messageType,
			Serializable message, Consumer<Response> callback, Consumer<Throwable> onError,
			Class<Response> responseClass) {
		// setTask("SENT " + messageType.getAddress());

		log.trace("{} -> {} says {}", this.info, info, messageType.getAddress());

		Buffer buffer = Buffer.factory.buffer(GsonProvider.get().toJson(message));
		this.client.post(info.port, info.address, messageType.getAddress(), (response) -> {
			response.bodyHandler((body) -> {
				log.trace("{} <- {} says {}", this.info, info, messageType.getAddress());
				this.routingTable.insert(info);
				callback.accept(GsonProvider.get().fromJson(body.toString(), responseClass));
				// setTask("CONNECTED IDLE");
			});
		}).setTimeout(messageType.getTimeout(message)).exceptionHandler((e) -> {
			this.routingTable.onNodeFailed(info);
			log.error("{} had problem with posting the request {} to {}: {}", this, messageType.getAddress(), info,
					e.getMessage());
			onError.accept(e);
		}).setChunked(true).end(buffer);

	}

	private void setTask(String task) {
		Platform.runLater(() -> {
			this.task.set(task);
		});
	}

	private HttpServer createServer() {

		VertxOptions options = new VertxOptions();
		options.setEventLoopPoolSize(16);
		options.setWorkerPoolSize(16);

		Vertx vertx = Vertx.vertx(options);

		final Router router = Router.router(vertx);
		router.route().handler(io.vertx.ext.web.handler.BodyHandler.create());

		router.route(HttpMethod.POST, Messages.FindNodes.getAddress())
				.handler(new JsonHandler<FindNodesRequest, FindNodesResponse>(FindNodesRequest.class) {
					public FindNodesResponse callback(FindNodesRequest message) {
						Node.this.routingTable.insert(message.getNode());
						return new FindNodesResponse(
								Node.this.routingTable.findClosest(message.getLookupId(), message.getCount()));
					}
				});

		router.route(HttpMethod.POST, Messages.Get.getAddress())
				.handler(new JsonHandler<GetValueRequest, GetValueResponse>(GetValueRequest.class) {
					public GetValueResponse callback(GetValueRequest message) {
						Node.this.routingTable.insert(message.getNode());
						DHTEntry value = Node.this.dht.get(message.getLookupId());
						log.debug("Looking up value for you: {}", value);
						if (value != null) {
							if (!message.isGetData()) {
								value = value.getWithoutData();
							}
							return new GetValueResponse(ImmutableList.of(), ValueContainer.pack(value));
						} else {
							return new GetValueResponse(
									Node.this.routingTable.findClosest(message.getLookupId(), Settings.K), null);
						}
					}
				});

		router.route(HttpMethod.POST, Messages.Store.getAddress())
				.handler(new JsonHandler<StoreValueRequest, StoreValueResponse>(StoreValueRequest.class) {
					public StoreValueResponse callback(StoreValueRequest message) {

						// if (message.getValue().get() instanceof Video) {
						// Video video = (Video) message.getValue().get();
						// log.debug("Received {} from {}, views: {}", video,
						// message.getNode(), video.getViews());
						// }

						Node.this.routingTable.insert(message.getNode());
						Node.this.dht.put(message.getValue());
						return new StoreValueResponse();
					}
				});

		router.route(HttpMethod.POST, Messages.StreamingStarted.getAddress()).handler(
				new JsonHandler<StreamingStartedRequest, StreamingStartedResponse>(StreamingStartedRequest.class) {
					public StreamingStartedResponse callback(StreamingStartedRequest message) {
						Node.this.routingTable.insert(message.getNode());
						Node.this.dht.onVideoStreamingStarted(message.getId());
						return new StreamingStartedResponse();
					}
				});

		router.route(HttpMethod.POST, Messages.Ping.getAddress())
				.handler(new JsonHandler<PingRequest, PingResponse>(PingRequest.class) {
					public PingResponse callback(PingRequest message) {
						Node.this.routingTable.insert(message.getNode());
						return new PingResponse();
					}
				});

		router.exceptionHandler((e) -> {
			log.error("", e);
		});

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept);
		server.listen(this.info.port);
		System.out.println("Started server listening on: " + this.info);
		return server;
	}

	public final void bootstrap(Runnable callback) {
		// JavaFX can't create socket, so just run on any thread.
		SCHEDULER.schedule(() -> {
			init();
			log.debug("{} contacting bootstrap server.", this.info);
			sendMessage(Settings.bootstrapNode, Messages.Join, new JoinRequest(this.info), (response) -> {
				bootstrap(response.getBootstrapNodes());
			}, (e) -> {
				System.exit(0);
			}, JoinResponse.class);
			callback.run();
		}, 0, TimeUnit.MILLISECONDS);
	}

	public synchronized final void bootstrap(List<NodeInfo> bootstrapNodes) {
		log.debug("{} started bootstraping on network", this.info);
		setTask("BOOTSTRAPING");

		// add all bootstrap servers to routing table
		for (NodeInfo node : bootstrapNodes) {
			this.routingTable.insert(node);
		}

		// self lookup
		new NodeLookup(this, this.info.getId(), Settings.K, (nodes) -> {
			new RefreshBucketOperation(this, () -> {
				log.debug("## Bootstraping of {} finished ", this.info);
				setTask("CONNECTED IDLE");
				onBootstrapFinished();
			}).execute();
		}).execute();

	}

	private void refresh() {
		new RefreshBucketOperation(this, () -> {
			this.dht.refresh();
		}).execute();
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	private void createFolderForVideos() {
		if (this.videoDirectory != null) {
			new File(this.videoDirectory).delete();
		}
		this.videoDirectory = "videos/" + this.info.id.toString() + "/";
		new File(this.videoDirectory).mkdirs();
	}

	public void findValue(KademliaId key, boolean getFullData, BiConsumer<NodeInfo, DHTEntry> callback) {
		new GetValueOperation(this, key, getFullData, callback).execute();
	}

	public void search(String text, Consumer<List<String>> callback) {
		Set<String> results = new ConcurrentHashSet<>();
		String[] keywords = text.split(" ");

		final AtomicInteger expectedValues = new AtomicInteger(keywords.length);
		for (String keyword : keywords) {
			KademliaId key = new KademliaId(IdType.Keyword, this.region, keyword);
			findValue(key, false, (node, value) -> {
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

	public void store(DHTEntry value) {
		new StoreOperation(this, value, (nodesStoredOn) -> {
		}).store();
	}

	public void uploadVideo(String name, String filePath, Consumer<Object> callback) {
		SCHEDULER.schedule(() -> {

			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(Paths.get(filePath));
			} catch (IOException e1) {
				log.error("Error while reading file.", e1);
			}

			// we need to make instance per region
			for (Region region : Region.realValues()) {

				KademliaId videoId = new KademliaId(IdType.Video, region, name);
				Video video = new Video(videoId, bytes);
				new StoreOperation(this, video, (nodesStoredOn) -> {
					callback.accept("DONE");
				}).store();

				for (String keyword : name.split(" ")) {
					Keyword keywordObject = new Keyword(new KademliaId(IdType.Keyword, region, keyword.trim()),
							ImmutableSet.of(name));
					store(keywordObject);
				}

				// store to all
				Keyword all = new Keyword(new KademliaId(IdType.Keyword, region, Settings.AllKeyword), ImmutableSet.of(name));
				store(all);
			}

		}, 0, TimeUnit.MILLISECONDS);
	}

	public File getVideoForStreaming(final KademliaId id) {
		return this.dht.getVideoForStreaming(id);
	}

	public String getVideoDirectory() {
		return this.videoDirectory;
	}

	public void onStartedStreaming(final NodeInfo node, final KademliaId videoId) {
		sendMessage(node, Messages.StreamingStarted, new StreamingStartedRequest(node, videoId), (e) -> {
		}, (e) -> {
		}, StreamingStartedRequest.class);
	}

	public void getAll(Consumer<List<String>> callback) {
		KademliaId key = new KademliaId(IdType.Keyword, this.region, Settings.AllKeyword);
		findValue(key, false, (node, keywordObject) -> {
			Keyword keyword = (Keyword) keywordObject;
			List<String> videos = new ArrayList<>(keyword.getVideos());
			Collections.sort(videos);
			callback.accept(videos);
		});
	}
}

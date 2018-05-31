package com.codlex.distributed.systems.homework1.peer.dht;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.dht.content.Keyword;
import com.codlex.distributed.systems.homework1.peer.dht.content.Video;
import com.codlex.distributed.systems.homework1.peer.messages.Messages;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueRequest.ValueContainer;
import com.codlex.distributed.systems.homework1.peer.messages.StoreValueResponse;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.google.common.base.Objects;
import com.google.common.io.Files;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = { "table" }, includeFieldNames = false)
public class DHT {

	public DHT(Node localNode) {
		this.localNode = localNode;
	}

	private final Node localNode;

	@Getter
	private final ObservableMap<KademliaId, DHTEntry> table = FXCollections.synchronizedObservableMap(FXCollections.observableMap(new HashMap<>()));

	public void store(DHTEntry value) {
		store(value, (nodesStoredOn) -> {});
	}

	public void store(DHTEntry value, Consumer<List<NodeInfo>> onStoredCallback) {
		new NodeLookup(this.localNode, value.getId()).execute((closestNodes) -> {
			for (NodeInfo node : closestNodes) {
				this.localNode.sendMessage(node, Messages.Store,
						new StoreValueRequest(this.localNode.getInfo(), ValueContainer.pack(value)), (response) -> {
							log.debug("Stored value: {} at {}", value, node);
						}, StoreValueResponse.class);
			}

			onStoredCallback.accept(closestNodes);
		});
	}

	public synchronized DHTEntry get(KademliaId key) {
		DHTEntry result = this.table.get(key);
		if (result != null) {
			return result;
		} else {
			return null;
		}
	}

	public void refresh() {
		log.debug("Started refresh of DHT.");
		for (DHTEntry entry : this.table.values()) {
			store(entry, (closestNodes) -> {
				if (!closestNodes.contains(this.localNode.getInfo())) {
					this.table.remove(entry.getId());
					log.debug("I'm no longer closest to {}, removing {}", entry.getId(), entry);
				}
			});
		}
	}

	public synchronized void put(ValueContainer value) {
		switch (value.getType()) {
		case Keyword:
			Keyword keyword = value.getKeywordValue();
			Keyword oldKeyword = (Keyword) this.table.get(keyword.getId());
			Keyword toStore = Keyword.merge(keyword, oldKeyword);

			this.table.remove(toStore.getId());
			this.table.put(toStore.getId(), toStore);
			break;
		case Video:
			Video video = value.getVideoValue();
			Video oldVideo = (Video) this.table.get(video.getId());
			Video toStoreVideo = Video.merge(video, oldVideo);
			this.table.remove(toStoreVideo.getId());
			this.table.put(toStoreVideo.getId(), toStoreVideo);
			// save file
			try {
				Files.write(video.getVideoData(), new File("videos/", video.getId().getData()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			throw new RuntimeException("Not implemented yet.");
		}

	}
}

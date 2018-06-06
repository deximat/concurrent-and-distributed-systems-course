package com.codlex.distributed.systems.homework1.peer.dht;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import com.codlex.distributed.systems.homework1.peer.operations.RefreshOperation;
import com.codlex.distributed.systems.homework1.peer.operations.StoreOperation;
import com.google.common.base.Objects;
import com.google.common.io.Files;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = { "table" })
public class DHT {

	public DHT(Node localNode) {
		this.localNode = localNode;
	}

	private final Node localNode;

	private final Map<KademliaId, DHTEntry> table = new HashMap<>();

	public synchronized DHTEntry get(KademliaId key) {
		DHTEntry result = this.table.get(key);
		if (result != null) {
			return result;
		} else {
			return null;
		}
	}

	public synchronized void remove(DHTEntry remove) {
		this.table.remove(remove.getId());
		if (remove instanceof Video) {
			((Video) remove).delete();
		}
		log.debug("Removing {} from {}", remove, this.localNode);
	}

	public synchronized void refresh() {
		new RefreshOperation(this.localNode, getTableAsList(), this::remove, () -> {
			log.debug("Refresh done!");
		}).execute();
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
			if (oldVideo != null) {
				oldVideo.delete();
			}
			this.table.remove(toStoreVideo.getId());
			this.table.put(toStoreVideo.getId(), toStoreVideo);
			toStoreVideo.save(this.localNode.getVideoDirectory());
			break;
		default:
			throw new RuntimeException("Not implemented yet.");
		}
	}

	public synchronized void onVideoStreamingStarted(KademliaId id) {
		Video video = (Video) get(id);
		video.incrementViews();
		log.debug("Viewing video: {}", video);
	}

	public synchronized File getVideoForStreaming(KademliaId id) {
		Video video = (Video) get(id);
		return video.getFile();
	}

	public synchronized List<DHTEntry> getTableAsList() {
		List<DHTEntry> entries = new ArrayList<>(this.table.values());
		Collections.sort(entries);
		return entries;
	}
}

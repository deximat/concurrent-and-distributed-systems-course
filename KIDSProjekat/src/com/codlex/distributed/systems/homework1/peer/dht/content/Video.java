package com.codlex.distributed.systems.homework1.peer.dht.content;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class Video extends DHTEntry {

	@EqualsAndHashCode
	@ToString
	public static class View {

		final String id = UUID.randomUUID().toString();
		final long time = System.currentTimeMillis();

		public boolean isExpired() {
			return System.currentTimeMillis() - this.time > Settings.ViewExpiryMillis;
		}
	}

	public Video(KademliaId id, byte[] videoData) {
		this(id, videoData, ImmutableSet.of());
	}

	private Video(KademliaId id, byte[] videoData, Set<View> views) {
		super(id);
		this.videoData = videoData;
		addAll(views);
	}

	private void addAll(final Set<View> views) {
		this.views.addAll(views);
		filterOutExpiredViews();
	}

	private void filterOutExpiredViews() {
		this.views.removeIf(View::isExpired);
	}

	@Getter
	private byte[] videoData;

	@Getter
	private Set<View> views = new HashSet<>();

	@Getter
	private transient File file;

	public synchronized void save(String videoDirectory) {
		this.file = new File(videoDirectory, id.toHex());
		try {
			Files.write(this.videoData, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void delete() {
		this.file.delete();
	}

	public DHTEntry getWithoutData() {
		return new Video(this.id, null);
	}

	public static Video merge(Video video1, Video video2) {
		if (video1 == null) {
			return video2;
		}

		if (video2 == null) {
			return video1;
		}



		final Set<View> views = new HashSet<>();
		views.addAll(video1.views);
		views.addAll(video2.views);
		return new Video(video1.id, video1.videoData, views);
	}

	public synchronized void incrementViews() {
		this.views.add(new View());
	}

	public String toString() {
		return new StringBuilder()
				.append("Video(id = ")
				.append(this.id.toHexShort())
				.append(", viewCount = ")
				.append(this.views.size())
				.append(")")
				.toString();
	}

	public synchronized int getViewCount() {
		return this.views.size();
	}

	protected synchronized int calculateDesiredRedundancy() {
		int viewCount = getViewCount();
		if (viewCount < Settings.ViewsToStartDynamicRedundancy) {
			return 0;
		}

		return viewCount / Settings.LinearDinamicRedundancyFactorPerView;
	}
}

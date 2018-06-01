package com.codlex.distributed.systems.homework1.peer.dht.content;

import java.io.File;
import java.io.IOException;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.google.common.io.Files;

import lombok.Getter;
import lombok.ToString;

public class Video extends DHTEntry {

	public Video(KademliaId id, byte[] videoData) {
		super(id);
		this.videoData = videoData;
	}

	@Getter
	private byte[] videoData;

	@Getter
	private transient File file;

	public void save(String videoDirectory) {
		file = new File(videoDirectory, id.toHex());
		try {
			Files.write(this.videoData, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void delete() {
		this.file.delete();
	}

	public DHTEntry getWithoutData() {
		return new Video(this.id, null);
	}

	public static Video merge(Video video1, Video video2) {
		return new Video(video1.id, video1.videoData);
	}

	public void incrementViews() {

	}

	public String toString() {
		return new StringBuilder()
				.append("Video(")
				.append(this.id.toString())
				.append(")")
				.toString();
	}
}

package com.codlex.distributed.systems.homework1.peer.dht.content;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

import lombok.ToString;

@ToString(of={"id", "videoData"})
public class Video extends DHTEntry {

	public Video(KademliaId id, byte[] videoData) {
		super(id);
		this.videoData = videoData;
	}

	private byte[] videoData;
	// ovde ce da idu i views

	public static Video merge(Video video1, Video video2) {
		return new Video(video1.id, video1.videoData);
	}
}

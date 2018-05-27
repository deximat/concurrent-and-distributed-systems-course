package com.codlex.distributed.systems.homework1.peer.dht.content;

import java.util.HashSet;
import java.util.Set;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of={"id", "videos"})
public class Keyword extends DHTEntry {

	public Keyword(KademliaId id, Set<String> videos) {
		super(id);
		this.videos = videos;
	}

	private Set<String> videos = new HashSet<>();

	public static final Keyword merge(Keyword keyword1, Keyword keyword2) {
		if (keyword1 == null) {
			return keyword2;
		}

		if (keyword2 == null) {
			return keyword1;
		}

		Set<String> allVideos = new HashSet<>();
		allVideos.addAll(keyword1.videos);
		allVideos.addAll(keyword2.videos);
		return new Keyword(keyword1.id, allVideos);
	}

}

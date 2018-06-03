package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;
import com.codlex.distributed.systems.homework1.peer.Region;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;
import com.codlex.distributed.systems.homework1.peer.dht.content.Keyword;
import com.codlex.distributed.systems.homework1.peer.dht.content.Video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class StoreValueRequest implements Serializable {

	@Getter
	@ToString
	public static class ValueContainer {
		private IdType type;
		private Keyword keywordValue;
		private Video videoValue;

		public DHTEntry get() {

			switch (this.type) {
			case Keyword:
				return keywordValue;
			case Video:
				return videoValue;
			default:
				new RuntimeException("Not imlemented yet.");
			}

			return null;
		}

		public static ValueContainer pack(DHTEntry entry) {
			ValueContainer container = new ValueContainer();
			container.type = entry.getId().getType();
			switch (entry.getId().getType()) {
			case Keyword:
				container.keywordValue = (Keyword) entry;
				break;
			case Video:
				container.videoValue = (Video) entry;
				break;
			default:
				new RuntimeException("Not imlemented yet. ");
			}
			return container;
		}
	}

	private NodeInfo node;
	private ValueContainer value;


	public static void main(String[] args) {
		System.out.println(new KademliaId(IdType.Video, Region.Europe, "ma.mp4"));
	}
}

package com.codlex.distributed.systems.homework1.core.id;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.RandomStringUtils;

import com.codlex.distributed.systems.homework1.peer.Region;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;

import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode
@Slf4j
public class KademliaId implements Serializable {

	public final static int ID_LENGTH = 120;
	public final static int ID_LENGTH_TYPE = 2;
	public final static int ID_LENGTH_REGION = 8;
	public final static int ID_LENGTH_DATA = ID_LENGTH - ID_LENGTH_REGION - ID_LENGTH_TYPE;

	private static final Charset CHARSET = Charset.forName("UTF-8");

	private static final int ID_LENGTH_BITS = ID_LENGTH * 8;

	private final byte[] bytes;

	public KademliaId(byte[] bytes) {
		this.bytes = bytes;
	}

	public KademliaId(IdType type, Region region, String data) {
		this.bytes = getBytes(type, region, data);
	}

	public KademliaId(IdType type, final Region region) {
		this(type, region, RandomStringUtils.random(ID_LENGTH_DATA, true, true));
	}

	public BigInteger toBigInt() {
		return new BigInteger(1, getBytes());
	}

	public byte[] getBytes() {
		return this.bytes;
	}


	private static String fillUp(String data) {
		final StringBuilder builder = new StringBuilder();
		builder.append(data);
		for (int i = 0; i < ID_LENGTH_DATA - data.length(); i++) {
			builder.append(" ");
		}
		return builder.toString();
	}

	private static byte[] getBytes(IdType type, final Region region, final String data) {
		StringBuilder builder = new StringBuilder();
		builder.append(type.getKey());
		builder.append(region.getCode());
		builder.append(fillUp(data));
		// log.debug(builder.toString() + " size: " + builder.length());
		return builder.toString().getBytes(CHARSET);
	}

	public KademliaId xor(KademliaId nid) {
		byte[] result = new byte[ID_LENGTH];
		byte[] nidBytes = nid.getBytes();

		for (int i = 0; i < ID_LENGTH; i++) {
			result[i] = (byte) (this.bytes[i] ^ nidBytes[i]);
		}

		return new KademliaId(result);
	}

	public KademliaId generateNodeIdByDistance(int distance) {
		byte[] result = new byte[ID_LENGTH];

		/*
		 * Since distance = ID_LENGTH - prefixLength, we need to fill that
		 * amount with 0's
		 */
		int numByteZeroes = ID_LENGTH - (distance / 8);
		int numBitZeroes = distance % 8;
		if (numBitZeroes > 0) {
			numByteZeroes--;
		}

		int allOnesFrom = numByteZeroes;

		for (int i = 0; i < numByteZeroes; i++) {
			result[i] = 0;
		}

		if (numBitZeroes != 0) {
			BitSet bits = new BitSet(8);
			bits.set(0, 8);
			for (int i = 0; i < numBitZeroes; i++) {
				bits.clear(i);
			}
			bits.flip(0, 8);

			if (numByteZeroes < result.length) {
				result[numByteZeroes] = bits.toByteArray()[0];
			}

			allOnesFrom++;
		}

		BitSet allOnes = new BitSet(8);
		allOnes.set(0, 8);
		for (int i = allOnesFrom; i < result.length; i++) {
			result[i] = allOnes.toByteArray()[0];
		}

		return this.xor(new KademliaId(result));
	}

	public int getFirstSetBitIndex() {
		int prefixLength = 0;

		for (byte b : getBytes()) {
			if (b == 0) {
				prefixLength += 8;
			} else {
				/* If the byte is not 0, we need to count how many MSBs are 0 */
				int count = 0;
				for (int i = 7; i >= 0; i--) {
					boolean a = (b & (1 << i)) == 0;
					if (a) {
						count++;
					} else {
						break; // Reset the count if we encounter a non-zero
								// number
					}
				}

				/* Add the count of MSB 0s to the prefix length */
				prefixLength += count;

				/* Break here since we've now covered the MSB 0s */
				break;
			}
		}
		return prefixLength;
	}

	public int getDistance(KademliaId to) {
		return ID_LENGTH_BITS - this.xor(to).getFirstSetBitIndex();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("KademliaId(type = ");
		builder.append(getType());
		builder.append(", region = ");
		builder.append(getRegion());
		builder.append(", data = ");
		// shorted data to be more readable
		builder.append(getData().trim().subSequence(0, Math.min(getData().length(), 5)));
		builder.append(")");
		return builder.toString();
	}

	public IdType getType() {

		byte[] type = Arrays.copyOfRange(this.bytes, 0, ID_LENGTH_TYPE);
		String typeString = new String(type);
		for (IdType typeEnum : IdType.values()) {
			if (typeEnum.getKey().equals(typeString)) {
				return typeEnum;
			}
		}

		return IdType.Unknown;
	}

	public String getData() {
		byte[] data = Arrays.copyOfRange(this.bytes, ID_LENGTH_REGION + ID_LENGTH_TYPE, ID_LENGTH);
		return new String(data).trim();
	}

	public Region getRegion() {
		byte[] region = Arrays.copyOfRange(this.bytes, ID_LENGTH_TYPE, ID_LENGTH_TYPE + ID_LENGTH_REGION);
		String regionString = new String(region);
		for (Region regionEnum : Region.values()) {
			if (regionEnum.getCode().equals(regionString)) {
				return regionEnum;
			}
		}

		return Region.Unknown;
	}

	public String toHex() {
		return new String(this.bytes);
	}

	public String toHexShort() {
		return new String(this.bytes).substring(0, 15).toUpperCase();
	}

	public static void main(String[] bla) {

		// test for correctness of id generation and distance
		for (int distance = 1; distance < ID_LENGTH_BITS; distance++) {
			val baseNode = new KademliaId(IdType.Node, Region.Serbia);
			val generatedNode = baseNode.generateNodeIdByDistance(distance);
			//System.out.println(baseNode.getDistance(generatedNode));
			if (baseNode.getDistance(generatedNode) != distance) {
				System.out.println("ERROR: " + baseNode.getDistance(generatedNode) + " expected: " + distance);
			}
		}


	}
}

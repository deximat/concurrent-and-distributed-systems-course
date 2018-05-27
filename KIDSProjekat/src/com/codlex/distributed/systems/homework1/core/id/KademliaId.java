package com.codlex.distributed.systems.homework1.core.id;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.RandomStringUtils;

import com.codlex.distributed.systems.homework1.peer.Region;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode
@Slf4j
public class KademliaId implements Serializable {

	public final static int ID_LENGTH = 400;
	public final static int ID_LENGTH_REGION = 8;
	public final static int ID_LENGTH_DATA = ID_LENGTH - ID_LENGTH_REGION;

	private final byte[] bytes;

	public KademliaId(byte[] bytes) {
		this.bytes = bytes;
	}

	public KademliaId(Region region, String data) {
		this.bytes = getBytes(region, data);
	}

	public KademliaId(final Region region) {
		this(region, RandomStringUtils.random(ID_LENGTH_DATA / 8, true, true));
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
		for (int i = 0; i < ID_LENGTH_DATA / 8 - data.length(); i++) {
			builder.append(" ");
		}
		return builder.toString();
	}

	private static byte[] getBytes(final Region region, final String data) {
		StringBuilder builder = new StringBuilder();
		builder.append(region.getCode());
		builder.append(fillUp(data));
		log.debug(builder.toString() + " size: " + builder.length());
		return builder.toString().getBytes(Charset.forName("UTF-8"));
	}

	public KademliaId xor(KademliaId nid) {
		byte[] result = new byte[ID_LENGTH / 8];
		byte[] nidBytes = nid.getBytes();

		for (int i = 0; i < ID_LENGTH / 8; i++) {
			result[i] = (byte) (this.bytes[i] ^ nidBytes[i]);
		}

		return new KademliaId(result);
	}

	public KademliaId generateNodeIdByDistance(int distance) {
		byte[] result = new byte[ID_LENGTH / 8];

		/*
		 * Since distance = ID_LENGTH - prefixLength, we need to fill that
		 * amount with 0's
		 */
		int numByteZeroes = (ID_LENGTH - distance) / 8;
		int numBitZeroes = 8 - (distance % 8);

		/* Filling byte zeroes */
		for (int i = 0; i < numByteZeroes; i++) {
			result[i] = 0;
		}

		/* Filling bit zeroes */
		BitSet bits = new BitSet(8);
		bits.set(0, 8);

		for (int i = 0; i < numBitZeroes; i++) {
			/* Shift 1 zero into the start of the value */
			bits.clear(i);
		}
		bits.flip(0, 8); // Flip the bits since they're in reverse order
		result[numByteZeroes] = (byte) bits.toByteArray()[0];

		/* Set the remaining bytes to Maximum value */
		for (int i = numByteZeroes + 1; i < result.length; i++) {
			result[i] = Byte.MAX_VALUE;
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
		return ID_LENGTH - this.xor(to).getFirstSetBitIndex();
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("KademliaId(region = ");
		builder.append(getRegion());
		builder.append(", data = ");
		builder.append(getData().trim());
		builder.append(")");
		return builder.toString();
	}

	private String getData() {
		byte[] data = Arrays.copyOfRange(this.bytes, ID_LENGTH_REGION, ID_LENGTH);
		return new String(data);
	}

	public Region getRegion() {
		byte[] region = Arrays.copyOfRange(this.bytes, 0, ID_LENGTH_REGION);
		String regionString = new String(region);
		for (Region regionEnum : Region.values()) {
			if (regionEnum.getCode().equals(regionString)) {
				return regionEnum;
			}
		}

		return Region.Unknown;
	}

	public static void main(String[] args) {
		System.out.println(new KademliaId(Region.America, "bld"));
	}
}

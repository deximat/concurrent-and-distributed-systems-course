//////// TODO: KILL CREATE YOUR OWN - THIS ONE IS FROM INTERNET //

package com.codlex.distributed.systems.homework1.core.id;

import java.math.BigInteger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.UUID;

public class KademliaId implements Serializable
{

    public final transient static int ID_LENGTH = 8*2;
    private byte[] keyBytes;

    public KademliaId(String data)
    {
        keyBytes = hexStringToByteArray(data);
        if (keyBytes.length != ID_LENGTH / 8)
        {
            throw new IllegalArgumentException("Specified Data need to be " + (ID_LENGTH / 8) + " characters long, but: " + keyBytes.length);
        }
    }

    /**
     * Generate a random key
     */
    public KademliaId()
    {
        keyBytes = new byte[ID_LENGTH / 8];
        new Random().nextBytes(keyBytes);
    }

    /**
     * Generate the NodeId from a given byte[]
     *
     * @param bytes
     */
    public KademliaId(byte[] bytes)
    {
        if (bytes.length != ID_LENGTH / 8)
        {
            throw new IllegalArgumentException("Specified Data need to be " + (ID_LENGTH / 8) + " characters long. Data Given: '" + new String(bytes) + "'");
        }
        this.keyBytes = bytes;
    }


    public byte[] getBytes()
    {
        return this.keyBytes;
    }

    /**
     * @return The BigInteger representation of the key
     */
    public BigInteger getInt()
    {
        return new BigInteger(1, this.getBytes());
    }

    /**
     * Compares a NodeId to this NodeId
     *
     * @param o The NodeId to compare to this NodeId
     *
     * @return boolean Whether the 2 NodeIds are equal
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof KademliaId)
        {
            KademliaId nid = (KademliaId) o;
            return this.hashCode() == nid.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Arrays.hashCode(this.keyBytes);
        return hash;
    }

    /**
     * Checks the distance between this and another NodeId
     *
     * @param nid
     *
     * @return The distance of this NodeId from the given NodeId
     */
    public KademliaId xor(KademliaId nid)
    {
        byte[] result = new byte[ID_LENGTH / 8];
        byte[] nidBytes = nid.getBytes();

        for (int i = 0; i < ID_LENGTH / 8; i++)
        {
            result[i] = (byte) (this.keyBytes[i] ^ nidBytes[i]);
        }

        KademliaId resNid = new KademliaId(result);

        return resNid;
    }

    /**
     * Generates a NodeId that is some distance away from this NodeId
     *
     * @param distance in number of bits
     *
     * @return NodeId The newly generated NodeId
     */
    public KademliaId generateNodeIdByDistance(int distance)
    {
        byte[] result = new byte[ID_LENGTH / 8];

        /* Since distance = ID_LENGTH - prefixLength, we need to fill that amount with 0's */
        int numByteZeroes = (ID_LENGTH - distance) / 8;
        int numBitZeroes = 8 - (distance % 8);

        /* Filling byte zeroes */
        for (int i = 0; i < numByteZeroes; i++)
        {
            result[i] = 0;
        }

        /* Filling bit zeroes */
        BitSet bits = new BitSet(8);
        bits.set(0, 8);

        for (int i = 0; i < numBitZeroes; i++)
        {
            /* Shift 1 zero into the start of the value */
            bits.clear(i);
        }
        bits.flip(0, 8);        // Flip the bits since they're in reverse order
        result[numByteZeroes] = (byte) bits.toByteArray()[0];

        /* Set the remaining bytes to Maximum value */
        for (int i = numByteZeroes + 1; i < result.length; i++)
        {
            result[i] = Byte.MAX_VALUE;
        }

        return this.xor(new KademliaId(result));
    }

    /**
     * Counts the number of leading 0's in this NodeId
     *
     * @return Integer The number of leading 0's
     */
    public int getFirstSetBitIndex()
    {
        int prefixLength = 0;

        for (byte b : this.keyBytes)
        {
            if (b == 0)
            {
                prefixLength += 8;
            }
            else
            {
                /* If the byte is not 0, we need to count how many MSBs are 0 */
                int count = 0;
                for (int i = 7; i >= 0; i--)
                {
                    boolean a = (b & (1 << i)) == 0;
                    if (a)
                    {
                        count++;
                    }
                    else
                    {
                        break;   // Reset the count if we encounter a non-zero number
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

    public int getDistance(KademliaId to)
    {
        return ID_LENGTH - this.xor(to).getFirstSetBitIndex();
    }


    public String hexRepresentation()
    {
        /* Returns the hex format of this NodeId */
        BigInteger bi = new BigInteger(1, this.keyBytes);
        return String.format("%0" + (this.keyBytes.length << 1) + "X", bi);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    public String toString()
    {
        return this.hexRepresentation();
    }

}

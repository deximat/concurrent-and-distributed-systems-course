package com.codlex.distributed.systems.homework1.core.id;

import java.math.BigInteger;
import java.util.Comparator;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

public class KeyComparator implements Comparator<NodeInfo>
{
    private final BigInteger key;

    public KeyComparator(KademliaId key)
    {
        this.key = key.toBigInt();
    }

    @Override
    public int compare(NodeInfo n1, NodeInfo n2)
    {
        BigInteger b1 = n1.getId().toBigInt();
        BigInteger b2 = n2.getId().toBigInt();

        b1 = b1.xor(key);
        b2 = b2.xor(key);

        // TODO: think if this should be abs
        return b1.abs().compareTo(b2.abs());
    }
}

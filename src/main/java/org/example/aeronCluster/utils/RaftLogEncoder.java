package org.example.aeronCluster.utils;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import java.util.Map;


public class RaftLogEncoder {
    public static int encoder(MutableDirectBuffer buffer, Long key, String value) {
        buffer.putLong(0, key);
        buffer.putInt(8, value.length());
        buffer.putBytes(12, value.getBytes());
        int length = 12 + value.length();
        return length;
    }

    public static String decoder(DirectBuffer buffer, int offset, Map<Long, String> clusterData) {
        long key = buffer.getLong(offset);
        int valueLength = buffer.getInt(offset + 8);

        byte[] valueBytesArray = new byte[valueLength];
        buffer.getBytes(offset + 12, valueBytesArray);
        String value = new String(valueBytesArray);
        clusterData.put(key, value);
        return "[key:" + key + ", valueLength:" + valueLength + ", value:" + value + "]";
    }


}

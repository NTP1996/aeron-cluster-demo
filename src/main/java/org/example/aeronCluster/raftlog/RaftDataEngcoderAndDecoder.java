package org.example.aeronCluster.raftlog;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import java.util.List;


public class RaftDataEngcoderAndDecoder {
    public static int encoder(MutableDirectBuffer buffer, Long key, String value) {
        buffer.putLong(0, key);
        buffer.putInt(8, value.length());
        buffer.putBytes(12, value.getBytes());
        int length = 12 + value.length();
        return length;
    }

    public static RaftData decoder(DirectBuffer buffer, int offset, List<RaftData> clusterData) {
        long key = buffer.getLong(offset);
        int valueLength = buffer.getInt(offset + 8);

        byte[] valueBytesArray = new byte[valueLength];
        buffer.getBytes(offset + 12, valueBytesArray);
        String value = new String(valueBytesArray);
        RaftData raftData = new RaftData(key, value);
        clusterData.add(new RaftData(key, value));
        return raftData;
    }


}

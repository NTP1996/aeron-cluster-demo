package org.example.aeronCluster.snapshot;

import org.example.aeronCluster.raftlog.RaftData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class Serializer {
    public static byte[] serializeToBytes(List<RaftData> list) {
        byte[] serializedBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(list);
            serializedBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializedBytes;
    }
}

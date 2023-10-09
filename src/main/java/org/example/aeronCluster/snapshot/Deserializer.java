package org.example.aeronCluster.snapshot;

import org.example.aeronCluster.raftlog.RaftData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Deserializer {
    @SuppressWarnings("unchecked")
    public static List<RaftData> deserializeFromBytes(byte[] serializedBytes) {
        List<RaftData> deserializedList = new ArrayList<>();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializedList = (List<RaftData>) ois.readObject();
            System.out.println("List deserialized from byte array successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserializedList;
    }
}

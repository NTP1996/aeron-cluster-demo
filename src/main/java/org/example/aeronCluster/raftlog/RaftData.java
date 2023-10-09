package org.example.aeronCluster.raftlog;

import lombok.Data;

@Data
public class RaftData {
    long key;
    String value;

    public RaftData(long key, String value) {
        this.key = key;
        this.value = value;
    }
}

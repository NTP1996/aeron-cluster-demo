package org.example.aeronCluster.raftlog;

import lombok.Data;

import java.io.Serializable;

@Data
public class RaftData implements Serializable {
    long key;
    String value;

    public RaftData(long key, String value) {
        this.key = key;
        this.value = value;
    }

}

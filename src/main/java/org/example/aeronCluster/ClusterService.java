package org.example.aeronCluster;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.example.aeronCluster.raftlog.RaftData;
import org.example.aeronCluster.raftlog.RaftDataEngcoderAndDecoder;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ClusterService implements ClusteredService {
    @Autowired
    ClusterClient client;
    @Autowired
    NacosService nacosService;

    List<RaftData> clusterRaftData = new ArrayList<>();

    @Override
    public void onStart(Cluster cluster, Image snapshotImage) {
        printInfo("onStart", "");
        if (snapshotImage != null) {
            printInfo("onStart-snapshot", "");
        }
    }

    @Override
    public void onSessionOpen(ClientSession session, long timestamp) {
        printInfo("onSessionOpen", session.toString());
    }

    @Override
    public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {
        printInfo("onSessionClose", closeReason.toString());
    }

    @Override
    public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
        RaftData decoder = RaftDataEngcoderAndDecoder.decoder(buffer, offset, clusterRaftData);
        nacosService.update(clusterRaftData.size());
        printInfo("[onSessionMessage]", "[key:" + decoder.getKey() + ", valueLength:" + decoder.getValue().length() + ", value:" + decoder.getValue() + "]");
    }

    @Override
    public void onTimerEvent(long correlationId, long timestamp) {
        printInfo("onTimerEvent", "");

    }

    @Override
    public void onTakeSnapshot(ExclusivePublication snapshotPublication) {
        printInfo("onTakeSnapshot", "");

    }

    @Override
    public void onRoleChange(Cluster.Role newRole) {
        printInfo("onRoleChange:", newRole.toString());
        AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(), (throwable -> {
            log.info(throwable.toString());
            throwable.printStackTrace();
        }), null, client);
        AgentRunner.startOnThread(agentRunner);
        nacosService.updateRole(true);
    }

    @Override
    public void onTerminate(Cluster cluster) {

        printInfo("onTerminate", "");

    }

    @Override
    public void onNewLeadershipTermEvent(long leadershipTermId, long logPosition, long timestamp, long termBaseLogPosition, int leaderMemberId, int logSessionId, TimeUnit timeUnit, int appVersion) {
        printInfo("onNewLeadershipTermEvent", "leadershipTermId = " + leadershipTermId + ", logPosition = " + logPosition + ", timestamp = " + timestamp + ", termBaseLogPosition = " + termBaseLogPosition + ", leaderMemberId = " + leaderMemberId + ", logSessionId = " + logSessionId + ", timeUnit = " + timeUnit + ", appVersion = " + appVersion);
    }

    @Override
    public int doBackgroundWork(long nowNs) {
        return 0;
    }

    public void printInfo(String method, String msg) {
        log.info("[" + method + "] " + msg);
    }

    public List<RaftData> getClusterData() {
        return clusterRaftData;
    }

}

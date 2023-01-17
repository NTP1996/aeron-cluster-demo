package org.example.aeronCluster;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.example.aeronCluster.utils.RaftLogEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ServiceImp implements ClusteredService {
    @Autowired
    ClientAgent client;


    Map<Long, String> clusterData = new HashMap<>();

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
        String msg = RaftLogEncoder.decoder(buffer, offset, clusterData);

        printInfo("[onSessionMessage]", msg);

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
            System.out.println(throwable.toString());
            throwable.printStackTrace();
        }), null, client);
        AgentRunner.startOnThread(agentRunner);
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
        System.out.println("[" + method + "] " + msg);
    }

    public Map<Long, String> getClusterData() {
        return clusterData;
    }

}

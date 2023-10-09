package org.example.aeronCluster;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.*;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.example.aeronCluster.utils.AeronCommon;
import org.example.aeronCluster.raftlog.RaftDataEngcoderAndDecoder;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Component
@Slf4j
public class ClusterClient implements Agent {
    @Autowired
    NacosService nacosService;

    AeronCluster aeronCluster;

    private final ExpandableDirectByteBuffer sendBuffer = new ExpandableDirectByteBuffer();

    private final IdleStrategy idleStrategy = new SleepingIdleStrategy();
    private boolean isInit = false;
    long keepAliveDeadlineMs = 0;

    // 3s 发送一次心跳
    final static long keepAliveInterval = 3000;

    public AeronCluster init() throws UnknownHostException {
        log.info("初始化 client");

        // get 3 Node ip
        String[] hostnames = nacosService.getHostnames();
        final String hostname = InetAddress.getLocalHost().getHostAddress();
        final String ingressEndpoints = AeronCommon.ingressEndpoints(Arrays.asList(hostnames));
        log.info("[ClientConfig] ingressEndpoints:" + ingressEndpoints);

        // tag::connect[]
        MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
//                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true));

        AeronCluster aeronCluster = AeronCluster.connect(
                new AeronCluster.Context()
                        .egressListener(new Client())
                        .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                        .ingressChannel("aeron:udp")
                        .egressChannel(AeronCommon.udpChannel(0, hostname, AeronCommon.CLIENT_RESPONSE_PORT_OFFSET))                                                // <3>
                        .ingressEndpoints(ingressEndpoints));
        isInit = true;
        log.info("[Client] Client init success");
        return aeronCluster;
    }

    public boolean send(Long key, String value) {
        int length = RaftDataEngcoderAndDecoder.encoder(sendBuffer, key, value);
        log.info("[ClientAgent] send :[key:" + key + ",value:" + value + "]");

        while (aeronCluster.offer(sendBuffer, 0, length) < 0)
        {
            idleStrategy.idle(aeronCluster.pollEgress());
        }
        return true;
    }

    @Override
    public void onStart() {
        keepAliveDeadlineMs = System.currentTimeMillis() + keepAliveInterval;
        log.info("[ClientAgent] onStart keepAliveDeadlineMs: " + keepAliveDeadlineMs);
    }

    @Override
    public int doWork() throws Exception {
        long currentTime = System.currentTimeMillis();
        int workCount = 0;
        if (this.isInit && keepAliveDeadlineMs < currentTime) {
            aeronCluster.sendKeepAlive();
            keepAliveDeadlineMs = System.currentTimeMillis() + keepAliveInterval;
            workCount++;
        }
        if (!this.isInit) {
            this.aeronCluster = this.init();
            workCount += 1;
        }
        if (aeronCluster.egressSubscription().isConnected()) {
            workCount += this.aeronCluster.pollEgress();
        }
        return workCount;
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "ClientAgent";
    }

    public boolean isInit() {
        return this.isInit;
    }

    private static class Client implements EgressListener {


        @Override
        public void onSessionEvent(long correlationId, long clusterSessionId, long leadershipTermId, int leaderMemberId, EventCode code, String detail) {
            log.info("[onSessionEvent] correlationId = " + correlationId + ", clusterSessionId = " + clusterSessionId + ", leadershipTermId = " + leadershipTermId + ", leaderMemberId = " + leaderMemberId + ", code = " + code + ", detail = " + detail);

        }

        @Override
        public void onNewLeader(long clusterSessionId, long leadershipTermId, int leaderMemberId, String ingressEndpoints) {
            log.info("[onNewLeader] clusterSessionId = " + clusterSessionId + ", leadershipTermId = " + leadershipTermId + ", leaderMemberId = " + leaderMemberId + ", ingressEndpoints = " + ingressEndpoints);
        }

        @Override
        public void onMessage(long clusterSessionId, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
            log.info("[onMessage] clusterSessionId = " + clusterSessionId + ", timestamp = " + timestamp + ", buffer = " + buffer + ", offset = " + offset + ", length = " + length + ", header = " + header);
        }
    }

}

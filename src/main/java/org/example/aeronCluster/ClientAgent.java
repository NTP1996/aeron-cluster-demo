package org.example.aeronCluster;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import org.agrona.*;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.example.aeronCluster.utils.AeronCommon;
import org.example.aeronCluster.utils.RaftLogEncoder;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Component
public class ClientAgent implements Agent {
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
        System.out.println("初始化 client");

        // get 3 Node ip
        String[] hostnames = nacosService.getHostnames();
        final String hostname = InetAddress.getLocalHost().getHostAddress();
        final String ingressEndpoints = AeronCommon.ingressEndpoints(Arrays.asList(hostnames));
        System.out.println("[ClientConfig] ingressEndpoints:" + ingressEndpoints);

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
        System.out.println("[Client] Client init success");
        return aeronCluster;
    }

    public boolean send(Long key, String value) {
        int length = RaftLogEncoder.encoder(sendBuffer, key, value);
        System.out.println("[ClientAgent] send :[key:" + key + ",value:" + value + "]");

        while (aeronCluster.offer(sendBuffer, 0, length) < 0)    // <2>
        {
            idleStrategy.idle(aeronCluster.pollEgress());
        }
        return true;
    }

    @Override
    public void onStart() {
        keepAliveDeadlineMs = System.currentTimeMillis() + keepAliveInterval;
        System.out.println("[ClientAgent] onStart keepAliveDeadlineMs: " + keepAliveDeadlineMs);
    }

    @Override
    public int doWork() throws Exception {
        long currentTime = System.currentTimeMillis();
        int workCount = 0;
        if (this.isInit && keepAliveDeadlineMs < currentTime) {
            aeronCluster.sendKeepAlive();
            keepAliveDeadlineMs = System.currentTimeMillis() + keepAliveInterval;
//            System.out.println("[ClientAgent] Send Alive keepAliveDeadlineMs:"+keepAliveDeadlineMs);
            workCount++;
        }
        if (!this.isInit) {
            // 链接失败报错
            this.aeronCluster = this.init();
            workCount += 1;
        }
        if (aeronCluster.egressSubscription().isConnected()) {
            workCount += this.aeronCluster.pollEgress();
        }
        //拉取返回值
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
            System.out.println("[onSessionEvent] correlationId = " + correlationId + ", clusterSessionId = " + clusterSessionId + ", leadershipTermId = " + leadershipTermId + ", leaderMemberId = " + leaderMemberId + ", code = " + code + ", detail = " + detail);

        }

        @Override
        public void onNewLeader(long clusterSessionId, long leadershipTermId, int leaderMemberId, String ingressEndpoints) {
            System.out.println("[onNewLeader] clusterSessionId = " + clusterSessionId + ", leadershipTermId = " + leadershipTermId + ", leaderMemberId = " + leaderMemberId + ", ingressEndpoints = " + ingressEndpoints);
        }

        @Override
        public void onMessage(long clusterSessionId, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
            System.out.println("[onMessage] clusterSessionId = " + clusterSessionId + ", timestamp = " + timestamp + ", buffer = " + buffer + ", offset = " + offset + ", length = " + length + ", header = " + header);
        }
    }

}

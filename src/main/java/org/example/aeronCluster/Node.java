package org.example.aeronCluster;

import com.alibaba.nacos.api.naming.pojo.Instance;
import io.aeron.CommonContext;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MinMulticastFlowControlSupplier;
import io.aeron.driver.ThreadingMode;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.NoOpLock;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.example.aeronCluster.utils.AeronCommon;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.aeron.samples.cluster.ClusterConfig.*;
import static java.lang.Integer.parseInt;
import static org.example.aeronCluster.utils.AeronCommon.LOG_CONTROL_PORT_OFFSET;
import static org.example.aeronCluster.utils.AeronCommon.udpChannel;

@Component
@Slf4j
public class Node {
    @Autowired
    ClusterService clusterService;
    @Autowired
    NacosService nacosService;

    static String baseVol = "cluster/";

    public void init() throws UnknownHostException {
        final int nodeId = parseInt(System.getProperty("nodeId", "1024"));
        List<Instance> allInstance;
        while (true) {
            allInstance = nacosService.getAllInstance();
            if (allInstance.size() == 3) {
                break;
            }
            System.out.println("等待3 个 Node 启动: size:" + allInstance.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        String[] hostnames = nacosService.getHostnames();
        final String hostname = InetAddress.getLocalHost().getHostAddress();

        AeronCommon.baseDir = new File(System.getProperty("user.dir"), baseVol+"node" + nodeId);
        AeronCommon.aeronDirName = CommonContext.getAeronDirectoryName() + "-" + nodeId + "-driver";
        AeronCommon.archiveDir = new File(AeronCommon.baseDir, "archive");
        AeronCommon.clusterDir = new File(AeronCommon.baseDir, "cluster");

        System.out.println("[config] hostname:" + hostname);
        System.out.println("[config] baseDir:" + AeronCommon.baseDir);
        System.out.println("[config] aeronDirName:" + AeronCommon.aeronDirName);
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        // end::main[]

        // tag::media_driver[]
        final MediaDriver.Context mediaDriverContext = new MediaDriver.Context()
                .aeronDirectoryName(AeronCommon.aeronDirName)
                .threadingMode(ThreadingMode.SHARED)
                .termBufferSparseFile(true)
                .multicastFlowControlSupplier(new MinMulticastFlowControlSupplier())
                .terminationHook(barrier::signal)
                .errorHandler(AeronCommon.errorHandler("Media Driver"));
        // end::media_driver[]

        final AeronArchive.Context replicationArchiveContext = new AeronArchive.Context()
                .controlResponseChannel("aeron:udp?endpoint=" + hostname + ":0");

        // tag::archive[]
        final Archive.Context archiveContext = new Archive.Context()
                .aeronDirectoryName(AeronCommon.aeronDirName)
                .segmentFileLength(1024 * 1024)
                .archiveDir(AeronCommon.archiveDir)
                .controlChannel(udpChannel(nodeId, hostname, ARCHIVE_CONTROL_PORT_OFFSET))
                .localControlChannel("aeron:ipc?term-length=64k")
                .archiveClientContext(replicationArchiveContext)
                .replicationChannel(AeronCommon.logReplicationChannel(hostname))
                .recordingEventsEnabled(false)
                .threadingMode(ArchiveThreadingMode.SHARED);
        // end::archive[]
//
        // tag::archive_client[]
        final AeronArchive.Context aeronArchiveContext = new AeronArchive.Context()
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(50))
                .lock(NoOpLock.INSTANCE)
                .controlRequestChannel(archiveContext.localControlChannel())
                .controlResponseChannel(archiveContext.localControlChannel())
                .aeronDirectoryName(AeronCommon.aeronDirName);
        // end::archive_client[]
//
        System.out.println("[config]clusterMembers: " + AeronCommon.clusterMembers(Arrays.asList(hostnames)));
        // tag::consensus_module[]
        final ConsensusModule.Context consensusModuleContext = new ConsensusModule.Context()
                .errorHandler(AeronCommon.errorHandler("Consensus Module"))
                .clusterMemberId(nodeId)
                .clusterMembers(AeronCommon.clusterMembers(Arrays.asList(hostnames)))
                .clusterDir(AeronCommon.clusterDir)
                .replicationChannel(AeronCommon.logReplicationChannel(hostname))
                .logChannel(AeronCommon.logControlChannel(nodeId, hostname, LOG_CONTROL_PORT_OFFSET))
                .consensusChannel(AeronCommon.consensusChannal(hostname, MEMBER_FACING_PORT_OFFSET))
                .ingressChannel(AeronCommon.ingressChannel(hostname, CLIENT_FACING_PORT_OFFSET))
                .archiveContext(aeronArchiveContext.clone())
                .isIpcIngressAllowed(true);
        // end::consensus_module[]

        // tag::clustered_service[]
        final ClusteredServiceContainer.Context clusteredServiceContext =
                new ClusteredServiceContainer.Context()
                        .aeronDirectoryName(AeronCommon.aeronDirName)
                        .archiveContext(aeronArchiveContext.clone())
                        .clusterDir(AeronCommon.clusterDir)
                        .clusteredService(clusterService)
                        .errorHandler(AeronCommon.errorHandler("Clustered ServiceImp"));
        // end::clustered_service[]
//
        // tag::running[]

        ClusteredMediaDriver clusteredMediaDriver = ClusteredMediaDriver.launch(
                mediaDriverContext, archiveContext, consensusModuleContext);
        ClusteredServiceContainer container = ClusteredServiceContainer.launch(
                clusteredServiceContext);
        // end::running[]
        System.out.println("[" + nodeId + "] Started Cluster Node on " + hostname + "...");
    }

}

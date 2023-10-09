package org.example.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@Service
@Slf4j
public class NacosService {
    private NamingService getNamingService() {

        NamingService nacos = null;
        try {
            nacos = NamingFactory.createNamingService("nacos");
        } catch (NacosException e) {
            log.error("can not get Naming service");
        }
        return nacos;
    }

    public List<Instance> getAllInstance() {
        List<Instance> allInstances = Lists.newArrayList();
        try {
            NamingService nacosNamingService = this.getNamingService();
            allInstances = nacosNamingService.getAllInstances("node");
        } catch (NacosException e) {
            log.info("NacosException:" + e);
        }
        return allInstances;
    }

    public String[] getHostnames() {
        List<Instance> allInstance = this.getAllInstance();
        String[] hostnames = new String[3];
        for (int i = 0; i < 3; i++) {
            Instance instance = allInstance.get(i);
            int nodeId = Integer.parseInt(instance.getMetadata().get("nodeId"));
            hostnames[nodeId] = instance.getIp();
        }
        return hostnames;
    }

    public Instance getSelf() {
        final int nodeId = parseInt(System.getProperty("nodeId", "1024"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                return instance;
            }
        }
        return null;
    }

    public void update(long size)  {
        final int nodeId = parseInt(System.getProperty("nodeId", "1024"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                Map<String, String> metadata = instance.getMetadata();
                metadata.put("raftDataSize", String.valueOf(size));
                NamingService namingService = this.getNamingService();
                try {
                    namingService.registerInstance("node", instance);
                } catch (NacosException e) {
                    log.error("update error");
                }
            }
        }
    }

    public void updateRole(boolean isLeader)  {
        final int nodeId = parseInt(System.getProperty("nodeId", "1024"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                Map<String, String> metadata = instance.getMetadata();
                metadata.put("isLeader", String.valueOf(isLeader));
                NamingService namingService = this.getNamingService();
                try {
                    namingService.registerInstance("node", instance);
                } catch (NacosException e) {
                    log.error("update error");
                }
            }
        }
    }



    @Bean
    public NacosDiscoveryProperties nacosDiscoveryProperties() {
        NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        metadata.put("nodeId", System.getProperty("nodeId", "1024"));
        return nacosDiscoveryProperties;
    }

}

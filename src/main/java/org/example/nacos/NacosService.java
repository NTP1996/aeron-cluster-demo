package org.example.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@Service
public class NacosService {

    public List<Instance> getAllInstance() {
        List<Instance> allInstances = Lists.newArrayList();
        try {
            NamingService nacosNamingService = NamingFactory.createNamingService("nacos");
            allInstances = nacosNamingService.getAllInstances("node");
        } catch (NacosException e) {
            System.out.println("NacosException:" + e);
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
        final int nodeId = parseInt(System.getProperty("aeron.cluster.tutorial.nodeId"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                return instance;
            }
        }
        return null;
    }

    @Bean
    public NacosDiscoveryProperties nacosDiscoveryProperties() {
        NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        metadata.put("nodeId", System.getProperty("aeron.cluster.tutorial.nodeId"));
        return nacosDiscoveryProperties;
    }

}

package org.example;

import org.example.aeronCluster.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import static java.lang.Integer.parseInt;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class Main {
    public static void main(String[] args) {
        // ！！！  使用 quickDocker.sh 启动应用
        int nodeId = parseInt(System.getProperty("nodeId", "110"));               // <1>
        System.out.println("nodeId:"+nodeId);
        SpringApplication.run(Main.class,args);
    }
}
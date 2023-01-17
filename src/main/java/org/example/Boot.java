package org.example;

import org.example.aeronCluster.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class Boot implements ApplicationRunner {
    @Autowired
    Node node;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        node.init();
    }
}

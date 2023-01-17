package org.example.controller;

import org.example.aeronCluster.ClientAgent;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.lang.Integer.parseInt;

@Controller
@ResponseBody
public class InfoController {
    @Autowired
    NacosService nacosService;

    @Autowired
    ClientAgent client;
    @GetMapping("/")
    public String index() {
        final int nodeId = parseInt(System.getProperty("aeron.cluster.tutorial.nodeId","1024"));
        System.out.println("NodeId:"+nodeId);
        return "this is node:"+nodeId;
    }
    @GetMapping("/info")
    public String info() {
        final int nodeId = parseInt(System.getProperty("aeron.cluster.tutorial.nodeId","1024"));
        System.out.println("NodeId:"+nodeId);
        return "this is node:"+nodeId;
    }

    @GetMapping("/put")
    public String put(@RequestParam Long key,@RequestParam String value){
        if(!client.isInit()){
            return "client is not init";
        }
        client.send(key,value);
        return "key:"+key+", value:"+value;
    }

    @GetMapping("/instance")
    public String instance(){
        return String.valueOf(nacosService.getSelf());
    }
}

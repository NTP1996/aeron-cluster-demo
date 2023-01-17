package org.example.controller;

import org.example.aeronCluster.ClientAgent;
import org.example.aeronCluster.Node;
import org.example.aeronCluster.ServiceImp;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import static java.lang.Integer.parseInt;

@Controller
@ResponseBody
public class InfoController {
    @Autowired
    NacosService nacosService;

    @Autowired
    ServiceImp serviceImp;

    @Autowired
    ClientAgent client;
    @GetMapping("/")
    public String index() {
        final int nodeId = parseInt(System.getProperty("nodeId","1024"));
        System.out.println("NodeId:"+nodeId);
        String info;
        if (!client.isInit()){
            info = "此节点是 follower 节点。\n /nodeData 获取节点数据。";
        }else{
            info = "此节点是 leader 节点。\n /nodeData 获取节点数据。\n/put 向aeron cluster 发送数据。例如：.../put?key=123&value=123String";
        }
        return "this is node:"+nodeId+", "+info;
    }

    @GetMapping("/put")
    public String put(@RequestParam Long key,@RequestParam String value){
        if(!client.isInit()){
            return "此节点没有client，访问 leader 发送数据";
        }
        client.send(key,value);
        return"success: key:"+key+", value:"+value;
    }

    @GetMapping("/instance")
    public String instance(){
        return String.valueOf(nacosService.getSelf());
    }
    @GetMapping("/nodeData")
    public String nodeData(){
        Map<Long, String> clusterData = serviceImp.getClusterData();
        StringBuffer sb = new StringBuffer();
        clusterData.forEach((k,v)->{
            sb.append("["+k+","+v+"]\n");
        });
        return "节点数据:"+sb.toString();
    }
}

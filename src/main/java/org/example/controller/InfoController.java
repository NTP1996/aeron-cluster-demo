package org.example.controller;

import com.sun.net.httpserver.Authenticator;
import org.example.aeronCluster.ClusterClient;
import org.example.aeronCluster.raftlog.RaftData;
import org.example.aeronCluster.ClusterService;
import org.example.aeronCluster.snapshot.SnapshotTrigger;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static java.lang.Integer.parseInt;

@Controller
@ResponseBody
public class InfoController {
    @Autowired
    NacosService nacosService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    SnapshotTrigger snapshotTrigger;
    @Autowired
    ClusterClient client;
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
        List<RaftData> clusterData = clusterService.getClusterData();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < clusterData.size(); i++) {
            RaftData raftData = clusterData.get(i);
            sb.append("[").append(i).append("] ").append(raftData).append("\n");
        }
        return "节点数据:\n"+sb.toString();
    }

    @GetMapping("/takeSnapshot")
    public String TakeSnapshot(){
        String snapshotResult;
        if(snapshotTrigger.trigger()){
            snapshotResult = "Success";
        }else{
            snapshotResult = "Fail";
        }
        return snapshotResult;
    }
}

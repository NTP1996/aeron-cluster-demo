# aeron-cluster-demo
这是一个 aeron cluster 的 demo，用于初步了解 aeron cluster
的运行方式。此项目基于 docker 能够仅使用一个脚本完成启动集群和依赖的服务（Nacos）的操作。

![img_1.png](img_1.png)
## 快速开始
前置条件
-[ ] MacOS
-[ ] 安装且配置 [maven](https://maven.apache.org/) 和 [docker Desktop](https://www.docker.com/)
```
# MacOS
open /Applications/Docker.app
git clone https://github.com/NTP1996/aeron-cluster-demo.git
cd aeron-cluster-demo
sh quickDocker.sh
```
部署完成后，点击链接能够访问对应服务；
[nacos](http://localhost:8848/nacos/#/serviceManagement?dataId=&group=&appName=&namespace=&pageSize=&pageNo=&namespaceShowName=public)

[node0](http://localhost:8080/)

[node1](http://localhost:8081/)

[node2](http://localhost:8082/)

使用docker desktop 查看各应用日志
![img.png](img.png)
# raftlogData
提交到集群的数据是一个（Long，String）元素，集群维护一个`Map<Long, String>`

```
# raftlogData:[key|valueLength|value]
public static int encoder(MutableDirectBuffer buffer, Long key, String value) {
        buffer.putLong(0, key);
        buffer.putInt(8, value.length());
        buffer.putBytes(12, value.getBytes());
        int length = 12 + value.length();
        return length;
    }
```
# todo
- 2.0 raftlog 持久化 + snapshot
- 3.0 raftlog 消费应用
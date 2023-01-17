#!/bin/bash
echo "nodeid = $nodeId"
hostname -I
java -jar -DnodeId=${nodeId}  node.jar

#-Daeron.event.cluster.log=all \


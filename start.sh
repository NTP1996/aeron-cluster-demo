#!/bin/bash
echo "nodeid = $nodeId"
hostname -I
java -jar -Daeron.cluster.tutorial.nodeId=${nodeId}  node.jar

#-Daeron.event.cluster.log=all \


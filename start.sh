#!/bin/bash
echo "nodeid = $nodeId"
hostname -I
java -javaagent:aeron-agent.jar -jar -Daeron.cluster.tutorial.nodeId=${nodeId}  node.jar

#-Daeron.event.cluster.log=all \


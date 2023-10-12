#!/bin/bash
nodeId=0
echo "nodeid = $nodeId"
hostname -I
mvn package
cd target
java -jar -DnodeId=${nodeId}  Node-1.0-SNAPSHOT.jar




#!/bin/bash
#create a docker network
docker network rm staticnet
docker network create --subnet=172.18.0.0/16 staticnet

#clean docker container
docker stop node0 node1 node2 nacos
docker rm node0 node1 node2 nacos

# run nacos server
docker run -d -p 8848:8848 --restart always -e MODE=standalone --name nacos --net staticnet --ip 172.18.0.12 centralx/nacos-server:2.0.4

# package project
mvn clean compile package

##clean docker image
docker rmi bowen/node:2.0

# build image
docker build -t bowen/node:2.0 .

#run node
docker run -d -e nodeId=0 --shm-size=1G -p 8080:8080 -v /tmp/cluster:/app/cluster --name  node0 --net staticnet --ip 172.18.0.20 bowen/node:2.0
docker run -d -e nodeId=1 --shm-size=1G -p 8081:8080 -v /tmp/cluster:/app/cluster --name  node1 --net staticnet --ip 172.18.0.21 bowen/node:2.0
docker run -d -e nodeId=2 --shm-size=1G -p 8082:8080 -v /tmp/cluster:/app/cluster --name  node2 --net staticnet --ip 172.18.0.22 bowen/node:2.0
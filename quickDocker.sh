#!/bin/bash
mvn clean compile package

#clean docker container
docker stop node0 node1 node2
docker rm node0 node1 node2


#clean docker image
docker rmi node:0 node:1 node:2

# build image
docker build --build-arg nodeNumber=0 -t node:0 .
docker build --build-arg nodeNumber=1 -t node:1 .
docker build --build-arg nodeNumber=2 -t node:2 .

#create a docker network
docker network rm staticnet
docker network create --subnet=172.18.0.0/16 staticnet

# run nacos server
docker run -d -p 8848:8848 --restart always -e MODE=standalone --name nacos --net staticnet --ip 172.18.0.12 centralx/nacos-server:2.0.4

#run node
docker run -d --shm-size=1G -p 8080:8080 --name  node0 --net staticnet --ip 172.18.0.20 node:0
docker run -d --shm-size=1G -p 8081:8080 --name  node1 --net staticnet --ip 172.18.0.21 node:1
docker run -d --shm-size=1G -p 8082:8080 --name  node2 --net staticnet --ip 172.18.0.22 node:2
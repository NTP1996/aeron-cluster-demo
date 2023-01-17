mvn clean compile package

#clean docker container
docker stop node0 node1 node2
docker rm node0 node1 node2


#clean docker image
docker rmi node:0 node:1 node:2

# build image and run
docker build --build-arg nodeNumber=0 -t node:0 .
docker build --build-arg nodeNumber=1 -t node:1 .
docker build --build-arg nodeNumber=2 -t node:2 .

docker run -d --shm-size=1G -p 8080:8080 --name node0 node:0
docker run -d --shm-size=1G -p 8081:8080 --name node1 node:1
docker run -d --shm-size=1G -p 8082:8080 --name node2 node:2
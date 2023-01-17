FROM azul/prime:8
MAINTAINER bowen.zhou@okg.com

WORKDIR /app
COPY target/Node-1.0-SNAPSHOT.jar node.jar
COPY start.sh start.sh
#COPY aeron-agent-1.40.0.jar aeron-agent.jar
ARG nodeNumber=0
ENV nodeId $nodeNumber
EXPOSE 8080

RUN chmod a+x start.sh
CMD ["./start.sh"]








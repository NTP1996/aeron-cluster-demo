FROM azul/prime:8
MAINTAINER bowen.zhou@okg.com

WORKDIR /app
COPY target/Node-1.0-SNAPSHOT.jar node.jar
COPY start.sh start.sh
EXPOSE 8080

RUN chmod a+x start.sh
CMD ["./start.sh"]








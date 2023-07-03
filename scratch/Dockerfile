# Use a Flink base image
FROM flink:1.16.0

ENV JOB_MANAGER_RPC_ADDRESS=session-test.default

#COPY flink-conf.yaml /opt/flink/conf/flink-conf.yaml
#COPY ./log4j-console.properties /opt/flink/conf/log4j-console.properties

# Expose the SQL Gateway port
EXPOSE 8083
# Start the SQL Gateway
#CMD ["/opt/flink/bin/sql-gateway.sh", "start-foreground", "-Dsql-gateway.endpoint.rest.address=localhost"]
 CMD ["sleep", "infinity"]

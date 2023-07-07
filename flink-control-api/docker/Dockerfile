#FROM flink:1.16.0
FROM flink16:local2

ENV CACHE_BUST=2

COPY docker-entrypoint.sh /docker-entrypoint2.sh
RUN chmod +x /docker-entrypoint2.sh

ENV JOB_MANAGER_RPC_ADDRESS=foo1
ENV BAR1=bar1

USER flink
ENTRYPOINT ["/docker-entrypoint2.sh"]
CMD ["/opt/flink/bin/sql-gateway.sh", "start-foreground", "-Dsql-gateway.endpoint.rest.address=localhost"]

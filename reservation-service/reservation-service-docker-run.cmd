docker run --name reservation-service ^
    -ti --rm ^
    --network vnet-saga-w/rollbacks-1 ^
    -e SPRING_DATASOURCE_URL=jdbc:mariadb://mariadb-service:3306/sagarlbk1 ^
    -e SPRING_DATASOURCE_USERNAME=sagau2 ^
    -e SPRING_DATASOURCE_PASSWORD=sagagrfedw ^
    -e SPRING_ACTIVEMQ_BROKER_URL=tcp://activemq-service:61616 ^
    -e SPRING_JMS_REDELIVERY_POLICY_MAXIMUM_REDELIVERIES=300 ^
    -e JAVA_TOOL_OPTIONS=\"-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n\" ^
    -p 8000:8000 ^
    saga-tests-rlbk-1/reservation-service:3.0-RLBK-NSTR-SEQEV
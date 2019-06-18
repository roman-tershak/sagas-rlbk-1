FROM openjdk:8-jre-alpine

# Application settings
ENV LANG="en_US.utf8" \
    APP_HOME="/opt/activemq" \
    APP_VERSION="5.15.9" \
    SCHEDULER_VOLUME="/opt/scheduler" \
    USER=activemq \
    GROUP=activemq \
    UID=10003 \
    GID=10003

# Install extra package
RUN apk --update add curl tar bash  &&\
    rm -rf /var/cache/apk/*

# Install ActiveMQ software
RUN \
    mkdir -p ${APP_HOME} /data /var/log/activemq  && \
    curl http://apache.mirrors.ovh.net/ftp.apache.org/dist/activemq/${APP_VERSION}/apache-activemq-${APP_VERSION}-bin.tar.gz -o /tmp/activemq.tar.gz &&\
    tar -xzf /tmp/activemq.tar.gz -C /tmp &&\
    mv /tmp/apache-activemq-${APP_VERSION}/* ${APP_HOME} &&\
    rm -rf /tmp/activemq.tar.gz &&\
    addgroup -g ${GID} ${GROUP} && \
    adduser -g "${USER} user" -D -h ${APP_HOME} -G ${GROUP} -s /bin/sh -u ${UID} ${USER}


RUN \
    chown -R ${USER}:${GROUP} ${APP_HOME} &&\
    chown -R ${USER}:${GROUP} /data &&\
    chown -R ${USER}:${GROUP} /var/log/activemq

# Expose all port
# Console, OpenWire, AMQP, STOPM, MQTT, WS
EXPOSE 8161 61616 5672 61613 1883 61614

#VOLUME ["/data", "/var/log/activemq"]
WORKDIR ${APP_HOME}
CMD ["bin/activemq", "console"]
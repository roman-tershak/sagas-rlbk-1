FROM openjdk:8-jre-alpine

ARG JAR_FILE
ARG DIR_NAME

COPY target/${JAR_FILE} /opt/${DIR_NAME}/

ENV LANG="en_US.utf8" \
    DIR_NAME="${DIR_NAME}" \
    JAR_FILE="${JAR_FILE}"

EXPOSE 8080 8000

# Looks like this is the only option that works with env variables
# The reason is that the shell form of ENTRYPOINT below invokes shell,
# which does preprocesing of env variables.
ENTRYPOINT java -jar /opt/${DIR_NAME}/${JAR_FILE}

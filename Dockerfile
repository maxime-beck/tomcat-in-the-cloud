FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp

USER root
RUN mkdir -m 777 -p /deployments

ARG war
RUN echo $war
ADD target/tomcat-in-the-cloud-1.0-SNAPSHOT.jar /deployments/app.jar
ADD $war/ /deployments/webapp.war

WORKDIR /deployments

ARG registry_id
ENV OPENSHIFT_KUBE_PING_NAMESPACE $registry_id

RUN sh -c 'touch app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar --war /deployments/webapp.war" ]

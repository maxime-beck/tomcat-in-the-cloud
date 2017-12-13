FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD target/tomcat-in-the-cloud-1.0-SNAPSHOT.jar app.jar
ENV OPENSHIFT_KUBE_PING_NAMESPACE default
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

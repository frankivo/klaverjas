FROM openjdk:8-alpine as builder
RUN apk add --no-cache maven
WORKDIR /root
COPY ./pom.xml /root/
COPY ./src/ /root/src/
RUN mvn package

FROM tomcat:jdk8-openjdk-slim
COPY --from=builder /root/target/klaverjas-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080

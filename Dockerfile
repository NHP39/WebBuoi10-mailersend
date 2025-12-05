FROM maven:3-eclipse-temurin-22 AS builder
WORKDIR /deployServletProject
COPY . .
RUN mvn clean package -DskipTests

FROM tomcat:jdk25
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /deployServletProject/target/SenderMail-0.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD [ "catalina.sh", "run" ]
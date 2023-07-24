FROM amazoncorretto:17
#ENV TZ=Europe/Moscow
COPY target/*.jar server.jar
ENTRYPOINT ["java","-jar","/server.jar"]
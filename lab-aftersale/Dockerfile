
FROM swr.cn-north-4.myhuaweicloud.com/oomall-javaee/openjdk:17-alpine


WORKDIR /app


COPY target/*.jar app.jar


EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]
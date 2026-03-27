FROM amazoncorretto:25-alpine

RUN apk update \
 && apk upgrade --no-cache \
 && rm -rf /var/cache/apk/*

RUN mkdir -p /opt/stub-oidc-server/
WORKDIR /opt/stub-oidc-server/

COPY build/libs/stub-oidc-server-1.0.0.jar app.jar

RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER 1001

EXPOSE 8091

CMD ["java", "-jar", "app.jar"]

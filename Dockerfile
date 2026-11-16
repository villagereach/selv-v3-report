FROM openlmis/service-base:7

RUN apk add --no-cache ttf-liberation
COPY docker/fontconfig/local.conf /etc/fonts/local.conf
RUN fc-cache -f

COPY build/libs/*.jar /service.jar

COPY build/consul /consul
WORKDIR /consul

RUN npm install --production

WORKDIR /

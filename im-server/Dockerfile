FROM openjdk:8-jdk-alpine

ENV APP_FILE im-server.jar
ENV APP_HOME /data/boxim

WORKDIR $APP_HOME

COPY ./dist/$APP_FILE $APP_HOME/

EXPOSE 8877
EXPOSE 8878

CMD java -jar $APP_HOME/$APP_FILE --spring.profiles.active=prod

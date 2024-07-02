FROM openjdk:8-jdk-alpine

ENV APP_FILE im-platform.jar
ENV APP_HOME /data/boxim

WORKDIR $APP_HOME

COPY ./dist/$APP_FILE $APP_HOME/

EXPOSE 8888
EXPOSE 9001

CMD java -jar $APP_HOME/$APP_FILE --spring.profiles.active=prod

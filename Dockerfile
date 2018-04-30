FROM agrdocker/agr_java_env:latest

WORKDIR /workdir/agr_java_core

RUN mkdir /root/.m2
ARG PASSWORD=mypassword
ENV PASSWORD ${PASSWORD}
RUN echo "<settingsSecurity><master>`mvn --encrypt-master-password $PASSWORD`</master></settingsSecurity>" > /root/.m2/settings-security.xml
ADD settings.xml /root/.m2
ADD . .
ARG ES_HOST=es.alliancegenome.org
ENV ES_HOST ${ES_HOST}

ARG VERSION=1.0.0
ENV VERSION ${VERSION}
RUN mvn -B versions:set -DnewVersion=$VERSION

RUN mvn -B clean package

CMD mvn -B deploy

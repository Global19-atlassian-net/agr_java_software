FROM agrdocker/agr_java_env:latest

WORKDIR /workdir/agr_java_software

ADD . .

RUN mvn -B clean package

FROM agrdocker/agr_base_linux_env:latest

WORKDIR /workdir/agr_java_software

ADD . .

RUN mvn -T 4 -B clean package

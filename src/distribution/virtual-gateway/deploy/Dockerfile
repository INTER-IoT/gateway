FROM docker.inter-iot.eu/alpine-jdk8:162b12
MAINTAINER Eneko Olivares <enolgor@teleco.upv.es>
COPY / /vgateway/
WORKDIR "/vgateway"
RUN chmod +x bin/run 
ENTRYPOINT ["bin/run"] 
EXPOSE 8080 8829
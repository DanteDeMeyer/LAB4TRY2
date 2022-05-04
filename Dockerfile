FROM debian:stretch
RUN echo 'deb http://ftp.debian.org/debian stretch-backports main' | tee /etc/apt/sources.list.d/stretch-backports.list
RUN apt update && apt-get upgrade -y
RUN apt install git curl tree openjdk-11-jdk -y
WORKDIR /project
RUN mkdir local
RUN mkdir replicated
COPY out/artifacts/project_jar/project.jar project.jar
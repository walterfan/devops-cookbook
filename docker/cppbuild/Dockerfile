FROM ubuntu:20.04
MAINTAINER Walter Fan <walter.fan@gmail.com>

VOLUME "/workspace"
WORKDIR "/workspace

RUN apt-get update && \
    apt-get dist-upgrade -y && \
    apt-get install gcc-multilib g++-multilib cmake wget -y  && \
    apt-get clean autoclean && \
    apt-get autoremove -y && \
    wget -O /tmp/conan.deb -L https://github.com/conan-io/conan/releases/download/0.25.1/conan-ubuntu-64_0_25_1.deb && \
    dpkg -i /tmp/conan.deb

ADD ./scripts/cmake-build.sh /build.sh

RUN chmod +x /build.sh

ENTRYPOINT exec /build.sh

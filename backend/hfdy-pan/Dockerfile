FROM openjdk:24-ea-17-slim-bullseye

WORKDIR /usr/hfdy

COPY ./target/hfdy-pan-0.0.1-SNAPSHOT.jar springboot.jar
# 安装字体
RUN apt-get update && \
    apt-get install -y --no-install-recommends fontconfig fonts-dejavu && \
    rm -rf /var/lib/apt/lists/
# 安装ffmpeg
RUN apt update  && \
    apt install -y wget xz-utils && \
    wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz && \
    xz -d ffmpeg-release-amd64-static.tar.xz && \
    tar -xvf ffmpeg-release-amd64-static.tar && \
    ln -s /usr/hfdy/ffmpeg-7.0.2-amd64-static/ffmpeg /usr/bin/ffmpeg

EXPOSE 8002

CMD ["java", "-jar", "springboot.jar"]

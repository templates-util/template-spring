FROM openjdk:18-jdk
RUN mkdir /opt/itexto-boot
COPY ./target/itexto-boot.jar /opt/itexto-boot
COPY ./wait /opt/itexto-boot
RUN chmod +x /opt/itexto-boot/wait
WORKDIR /opt/itexto-boot
ENTRYPOINT ./wait && java -jar itexto-boot.jar


FROM ubuntu:18.04

# Configure PKI Express

RUN apt-get -y update
RUN apt-get install -y curl gnupg2 wget apt-transport-https

RUN wget https://packages.microsoft.com/keys/microsoft.asc
RUN apt-key add microsoft.asc

RUN wget https://packages.microsoft.com/config/ubuntu/18.04/prod.list
RUN cp prod.list /etc/apt/sources.list.d/microsoft.list

RUN apt-get -y update
ENV DOTNET_CLI_TELEMETRY_OPTOUT true
RUN apt-get install -y dotnet-runtime-2.1

RUN wget https://files.lacunasoftware.com/pki-express/linux/pkie-1.6.2.tar.gz
RUN mkdir /usr/share/pkie
RUN tar xzf pkie-1.6.2.tar.gz -C /usr/share/pkie
RUN chmod -R a=r,a+X,u+w /usr/share/pkie

RUN touch /usr/local/bin/pkie
RUN echo '#!/bin/bash\ndotnet /usr/share/pkie/pkie.dll "$@"' >> /usr/local/bin/pkie
RUN chmod +x /usr/local/bin/pkie

RUN mkdir /var/log/pkie
RUN chmod 777 /var/log/pkie
RUN pkie config --set logdir=/var/log/pkie

# Configure PHP application

ENV TZ America/Sao_Paulo
RUN apt-get install -y tzdata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN apt-get -y update
RUN apt-get install -y php php-zip

COPY . /var/www/html/
WORKDIR /var/www/html/
EXPOSE 8000
CMD /usr/local/bin/pkie activate /tmp/LacunaPkiLicense.config ; /usr/bin/php -S 0.0.0.0:8000

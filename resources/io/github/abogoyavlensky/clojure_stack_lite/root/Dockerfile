FROM clojure:temurin-21-tools-deps-1.12.0.1479-alpine AS build

WORKDIR /app

# Install bb
RUN apk --no-cache add curl \
  && curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install \
  && chmod +x install \
  && ./install --version 1.12.197 --static

# Install tailwindcss
RUN curl -sL -o /usr/local/bin/tailwindcss https://github.com/tailwindlabs/tailwindcss/releases/download/v4.1.3/tailwindcss-linux-x64-musl \
  && chmod +x /usr/local/bin/tailwindcss

# bb: (trigger) install of Clojure tools and download deps
COPY bb.edn /app
RUN bb prepare

# clj: download deps
COPY deps.edn /app
RUN bb deps

# Build uberjar
COPY . /app
RUN bb build


FROM eclipse-temurin:21.0.2_13-jre-alpine
# TODO: update github username to manage images on the ghcr.io
LABEL org.opencontainers.image.source=https://github.com/{{developer}}/{{main/ns}}

WORKDIR /app
COPY --from=build /app/target/standalone.jar /app/standalone.jar

EXPOSE 80
# Increase the max memory limit to your needs
CMD ["java", "-Xmx256m", "-jar", "standalone.jar"]

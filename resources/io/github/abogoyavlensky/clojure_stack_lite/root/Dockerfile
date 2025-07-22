FROM clojure:temurin-21-tools-deps-1.12.1.1550-alpine AS build

WORKDIR /app

ARG BB_VERSION=1.12.206
ARG TAILWIND_VERSION=v4.1.11

# Install tailwindcss
RUN wget -qO /usr/local/bin/tailwindcss "https://github.com/tailwindlabs/tailwindcss/releases/download/${TAILWIND_VERSION}/tailwindcss-linux-x64-musl" \
    && chmod +x /usr/local/bin/tailwindcss

# Install bb
RUN wget -qO- https://raw.githubusercontent.com/babashka/babashka/master/install \
    | bash -s -- --version "${BB_VERSION}" --static

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

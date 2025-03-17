FROM clojure:temurin-21-tools-deps-1.12.0.1479-alpine AS build

# Install bb
RUN apk --no-cache add curl \
  && curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install \
  && chmod +x install \
  && ./install --version 1.12.194 --static

# Install tailwindcss
RUN curl -sL -o /usr/local/bin/tailwindcss https://github.com/tailwindlabs/tailwindcss/releases/download/v4.0.3/tailwindcss-linux-x64-musl \
  && chmod +x /usr/local/bin/tailwindcss

# Build uberjar
WORKDIR /app
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

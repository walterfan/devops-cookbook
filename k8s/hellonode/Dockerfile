FROM node:current-slim

LABEL MAINTAINER=walter.fan@gmail.com

# Copy source code to /src inside container
COPY . /src

# Install app and dependencies into /src inside container
RUN cd /src; npm install

# Document the port the app listens on
EXPOSE 8080

# Run this command (starts the app) when the container starts
CMD cd /src && node ./app.js

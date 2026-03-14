#!/bin/bash

BLUE_PORT=8081
GREEN_PORT=8082

ACTIVE_PORT=$(grep -o "127.0.0.1:[0-9]*" /etc/nginx/conf.d/app.conf | cut -d: -f2)

if [ "$ACTIVE_PORT" = "$BLUE_PORT" ]; then
    NEW_PORT=$GREEN_PORT
    OLD_PORT=$BLUE_PORT
else
    NEW_PORT=$BLUE_PORT
    OLD_PORT=$GREEN_PORT
fi

docker run -d -p ${NEW_PORT}:8080 --name app_${NEW_PORT} myapp:latest

# health check
for i in {1..10}; do
  sleep 3
  if curl -sf http://localhost:${NEW_PORT}/actuator/health | grep UP; then
      break
  fi
done

sed -i "s/${OLD_PORT}/${NEW_PORT}/" /etc/nginx/conf.d/app.conf

nginx -s reload

docker stop app_${OLD_PORT}
docker rm app_${OLD_PORT}

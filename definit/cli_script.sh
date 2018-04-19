#!/bin/bash


if [[ $1 != "" ]]; then
	CHANNEL_NAME=$1
else	
	CHANNEL_NAME=mychannel
fi

if [[ $(docker ps -f name=cli -q) != "" ]]; then
	docker rm cli --force
fi


docker-compose -f docker-compose-cli.yaml up -d 2>&1
docker logs -f cli


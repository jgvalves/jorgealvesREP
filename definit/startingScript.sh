#!/bin/bash

export FABRIC_CFG_PATH=$PWD
export CHANNEL_NAME=$1

if [$CHANNEL_NAME == ""]; then
	export CHANNEL_NAME="mychannel"
fi

rm -rf crypto-config
rm -rf channel-artifacts/*.block channel-artifacts/*.tx
rm -rf config

echo
echo CHANNEL_NAME=$CHANNEL_NAME
echo
echo

##1. Criar os certificados para as organizações de nós. Definido em crypto-config.yaml
echo "Starting cryptogen..."
./bin/cryptogen generate --config=./crypto-config.yaml
	if [ "$?" -ne 0 ]; then
    	exit 1
    fi
echo
echo

##2. Criar os artefactos para do canal. Definido em configtx.yaml

##	2.1 Criar o genesis.block, que contem a informação do canal a ser passados aos peers
	echo "Creating genesis block..."
	./bin/configtxgen -profile TwoOrgsOrdererGenesis -outputBlock ./channel-artifacts/genesis.block
	if [ "$?" -ne 0 ]; then
		echo
		echo
		echo "ERROR GENERATION GENESIS.BLOCK"
		echo
		echo
    	exit 1
    fi
	echo
	echo

##	2.2 Criar os canal entre os peers
	echo "Creating channel.tx..."
	./bin/configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME
	if [ "$?" -ne 0 ]; then
		echo
		echo
		echo "ERROR GENERATING CHANNEL.TX"
		echo
		echo
    	exit 1
    fi
	echo
	echo

##	2.3 Definir o ancor-peer para os canais. Neste caso, apenas para o canal 1
	echo "Defining anchor peer in Org1"
	./bin/configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org1MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org1MSP
	if [ "$?" -ne 0 ]; then
		echo
		echo
		echo "ERROR GENERATING Org1MSPanchors.TX"
		echo
		echo
    	exit 1
    fi
	echo
	echo


chmod 777 -R crypto-config channel-artifacts

##3. Inicializar o sistema: o CA peer, 2 Orderers, 1 Org com 2 Peers. Definido em docker-compose.yml

CA_PRIVATE=$(ls /etc/hyperledger/fabric-ca-server-config/ | grep sk)

echo "Composing docker images"
docker rm -f $(docker ps -aq) --force
yes | docker network prune


if [[ $2 == "ord1" ]] ; then
	docker-compose -f docker-compose.yml up -d
	echo "Logging Orderer 1..."
	docker-compose -f docker-compose.yml restart orderer.ordOrg.initial.com
	elif [[ $2 == "ord2" ]]; then
		docker-compose -f docker-compose.yml up -d
		echo "Logging Orderer 2..."
		docker-compose -f docker-compose.yml restart orderer2.ordOrg.initial.com
	elif [[ $2 == "peer0" ]]; then
		docker-compose -f docker-compose.yml up -d
		echo "Logging Peer 0..."
		docker-compose -f docker-compose.yml restart peer0.org1.initial.com
	elif [[ $2 == "peer1" ]]; then
		docker-compose -f docker-compose.yml up -d
		echo "Logging Peer 1..."
		docker-compose -f docker-compose.yml restart peer1.org1.initial.com
	elif [[ $2 == "peer2" ]]; then
		docker-compose -f docker-compose.yml up -d
		echo "Logging Peer 2..."
		docker-compose -f docker-compose.yml restart peer2.org1.initial.com
	elif [[ $2 == "ca" ]]; then
		docker-compose -f docker-compose.yml up -d
		echo "Logging CA Peer..."
		docker-compose -f docker-compose.yml restart ca.initial.com
	elif [[ $2 == "" ]]; then
		echo "Regular Start"
		docker-compose -f docker-compose.yml up -d
else
	echo "Invalid option!"
fi

sleep 2

echo
echo "Results:"
echo
docker ps -a

sleep 2

echo
echo
echo
echo
docker logs -f cli

#!/bin/bash
port="5555"
multicast="230.0.0.0"

read -p "Nameserver or Node? (S/N) " type
if [ $type = "S" ];
then
echo "Starting nameserver!"
java -jar ./out/artifacts/project_jar/project.jar nameserver $port $multicast

elif [ $type = "N" ];
then
read -p "Node name? " name
echo "Starting node $name!"
java -jar ./out/artifacts/project_jar/project.jar node $port $name $multicast

fi

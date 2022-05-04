#!/bin/bash
sudo apt-get update
sudo apt upgrade -y
sudo apt-get install git -y
sudo apt-get install curl -y
sudo apt-get install tree -y
sudo apt-get install neofetch -y

echo 'deb http://ftp.debian.org/debian stretch-backports main' | sudo tee /etc/apt/sources.list.d/stretch-backports.list
sudo apt update
sudo apt install openjdk-11-jdk

git clone https://github.com/wardgssens/dss-project.git
cd dss-project
git config credential.helper store
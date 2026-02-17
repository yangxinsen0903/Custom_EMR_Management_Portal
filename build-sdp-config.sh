#!/bin/bash

set -exu -o pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SRC_DIR=SDPManagementStudio
DOCKER_FILE_PATH=sdp-config/Dockerfile
DOCKER_BUILD_DIR=${SRC_DIR}
ACR_DIR=sdpharbortest.azurecr.io/sdp2
ACR_REPOSITORY=sdp-config


# get head commit id
pushd .
cd ${SCRIPT_DIR}/${SRC_DIR}
COMMIT_ID=$( git rev-parse --short HEAD )
echo $COMMIT_ID
popd

VERSION_TAG="v4.0.${COMMIT_ID}"

pushd .
cd ${SCRIPT_DIR}/${DOCKER_BUILD_DIR}
docker build -t ${ACR_DIR}/${ACR_REPOSITORY}:${VERSION_TAG} -f ${DOCKER_FILE_PATH} .
docker tag ${ACR_DIR}/${ACR_REPOSITORY}:${VERSION_TAG} ${ACR_DIR}/${ACR_REPOSITORY}:latest

docker push ${ACR_DIR}/${ACR_REPOSITORY}:${VERSION_TAG}
docker push ${ACR_DIR}/${ACR_REPOSITORY}:latest
popd

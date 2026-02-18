#!/bin/bash

version=1.1

cd /home/sunbox/sdpweb/sdp-magement-web/

sudo git pull origin sdp${version}
sudo docker build -t sdp-app:${version} -f ./Dockerfile .
sudo docker tag sdp-app:${version} acrsbxdevaks.azurecr.io/sdp-app:${version}
sudo docker push acrsbxdevaks.azurecr.io/sdp-app:${version}


kubectl delete deployment sdp-app -n sbx-k8s-test
kubectl apply -f sdp-app.yaml -n sbx-k8s-test
kubectl get pods -n sbx-k8s-test

cd ~

echo "发布成功！处理版本更新！"

exit 1;



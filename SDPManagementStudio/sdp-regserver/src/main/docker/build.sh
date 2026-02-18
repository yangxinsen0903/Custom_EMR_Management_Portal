#!/bin/bash

# 停止容器
docker stop regserver
# sleep 2s
# 删除容器
docker rm regserver

dname=sdp.sunbox.com/sdp-regserver

first=0
second=0
third=1

third_version=`docker images | grep $dname | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 3`
second_version=`docker images | grep $dname | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 2`
first_version=`docker images | grep $dname | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 1`

echo "last_version:"$first_version.$second_version.$third_version

if [ ! -n $third_version ];then
	echo "is null"
else
	let third=$third_version+1
fi
if [ ! -n $second_version ];then
	echo "is null"
else
	let second=$second_version
fi
if [ ! -n $first_version ];then
	echo "is null"
else
	let first=$first_version
fi

if [ $third -gt 9 ];then
    let third=0
    let second=$second+1
    if [ $second -gt 9 ];then
        let third=0
        let second=0
        let first=$first+1
    fi
fi

next_version=$first.$second.$third

echo "next_version:"$next_version

# 构建镜像 . 号表示在当前目录构建
docker build -t $dname:$next_version .

# docker rmi $dname:$first_version.$second_version.$third_version

# 登录到仓库
#cat  ../../dockerpassword.txt | docker login --username acrsbxdevaks --password-stdin  ccr.ccs.tencentyun.com
# 将provider docker 镜像上传到user_center仓库
#docker tag $dname:"$next_version" ccr.ccs.tencentyun.com/$dname:"$next_version"
#docker push ccr.ccs.tencentyun.com/$dname:"$next_version"
#echo "push success "

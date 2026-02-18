#!/bin/bash
source /etc/profile
# 停止容器
docker stop gw

# sleep 2s

# 删除容器
docker rm gw

# 删除镜像
docker rmi registry.cn-beijing.aliyuncs.com/sbx_igs/gw:0.0.1

# 构建镜像 . 号表示在当前目录构建
docker build -t registry.cn-beijing.aliyuncs.com/sbx_igs/gw:0.0.1 .

# 创建容器并启动
docker run -d -p 31000:31000 --net=host --name gw --env profile=$profile -v /root/logs:/usr/logs registry.cn-beijing.aliyuncs.com/sbx_igs/gw:0.0.1
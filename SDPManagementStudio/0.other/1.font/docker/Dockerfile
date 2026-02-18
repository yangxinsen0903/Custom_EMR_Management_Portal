FROM --platform=linux/amd64 nginx
MAINTAINER niyang
# 将前端dist文件中的内容复制到nginx目录
COPY ./dist/  /usr/share/nginx/html/
# 用本地的nginx配置文件覆盖镜像的Nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
# 暴露端口
EXPOSE 8081
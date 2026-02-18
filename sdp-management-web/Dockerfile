# FROM --platform=linux/amd64 node:13
FROM --platform=linux/arm64/v8 node:13
COPY . /data/
WORKDIR /data/
RUN npm install && npm run build
FROM --platform=linux/amd64  nginx
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=0 /data/dist /usr/share/nginx/html/

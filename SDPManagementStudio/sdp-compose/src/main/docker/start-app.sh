#!/bin/bash
echo "Calculating Java memory settings..."

memory_limit=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes)
max_mem=$(echo "$memory_limit * $APP_MEM_RATIO" |awk '{print $1 * $3}')

xmx=$(echo "$max_mem / 1024 / 1024 / 1024" | awk '{print $1 / 1024 / 1024 /1024}' )
xmx=$(printf "%.0f\n" $xmx)
xmx=$xmx"G"

echo "Setting Java memory options: -Xms$xmx -Xmx$xmx"

JAVA_OPTS="-Xms$xmx -Xmx$xmx -XX:+UseG1GC -XX:ParallelGCThreads=4 -Dserver.undertow.io-threads=8 -Dserver.undertow.worker-threads=1024"

java -jar /app/app.jar
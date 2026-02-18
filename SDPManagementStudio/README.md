# 0. 准备
1.修改 hosts 文件,增加如下配置:

`222.175.118.78   mvn.sunboxsd.top`

2.Maven仓库配置变更

@倪洋



# 1. 先编译打包系统
直接打包整个项目即可.

# 2. 运行注册中心
不能在IDE中运行，需通过下面的命令启动服务：

`java -jar sdp-regserver-0.1.0.jar`

# 3. 运行配置中心
不能在IDE中运行，需通过下面的命令启动服务：

`java -jar -Dspring.profiles.active=jdbc,dev sdp-config-1.0-SNAPSHOT.jar`

注: 在Windows下运行时，使用Cmd来运行，不要使用 Powershell 运行。

# 4. 运行各个服务
可以直接在IDE中运行XXXApplication类。
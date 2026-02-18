# 测试接口列表
1. 系统用户登录
2. 创建Ambari-Server数据库
3. 创建集群
4. 查询集群信息
5. 销毁集群

# 测试流程
1. 进行登录
> 登录成功后，将sdpToken设置到Cookie中
2. 开始创建流程 
   1. 创建Ambari-Server数据库
   2. 创建集群
   3. 检查集群是否创建完成
   > 此处循环调用查询集群信息 接口，检查集群的状态。集群创建完成后退出循环检查。
   4. 销毁集群

# 测试方案
1. 使用单线程进行测试
2. 测试循环执行50次

# 执行测试脚本方法
1. 安装JMeter
> JMeter下载地址： https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.5.tgz
> 下载后解压缩即可使用。
2. 设置脚本配置目录
> 使用JMeter打开脚本，在根目录下面修改 cfgPath, 设置需要使用的配置目录，此目录会生成临时文件，生成日志，生成测试报告。
> 在目录下面新建   report 目录
3. 设置脚本执行参数
> 在创建集群线程组中， 设置执行测试的参数：1.并发线程数。 2.执行次数
4. 运行测试脚本(注：修改里面的目录变量)
> nohup ${JMeterHome}/bin/jmeter -n -t ${cfgPath}/createTest.jmx -l ${cfgPath}/report/createTest.jtl  -e -o ${cfgPath}/report/createTest > log.log &


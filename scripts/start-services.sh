#!/usr/bin/env bash
# ============================================
# 本地启动两个服务（Git Bash 运行）
# 前置条件：
#   1. Docker 容器已启动：mysql(3307)、nacos(8848)、redis(6379)
#   2. 已执行过打包：mvn package -DskipTests
#   3. 数据库已初始化：scripts/init-local-db.sql
# 用法：bash scripts/start-services.sh
# ============================================

# 数据库口令走环境变量，不写入任何文件（配置文件里是 ${DB_PASSWORD} 占位）
# 用法：DB_PASSWORD=<你的本地MySQL密码> bash scripts/start-services.sh
if [ -z "$DB_PASSWORD" ]; then
    echo "请先设置环境变量: export DB_PASSWORD=<你的本地MySQL密码>"
    exit 1
fi
export DB_PASSWORD

echo "启动 lab-aftersale (端口 8082) ..."
/d/jdk17/bin/java -jar lab-aftersale/target/lab-aftersale-0.0.1-SNAPSHOT.jar &
AFTERSALE_PID=$!

echo "启动 lab-service (端口 8081) ..."
/d/jdk17/bin/java -jar lab-service/target/lab-service-0.0.1-SNAPSHOT.jar &
SERVICE_PID=$!

echo "两个服务已启动: aftersale_pid=$AFTERSALE_PID, service_pid=$SERVICE_PID"
echo "Ctrl+C 停止"
wait

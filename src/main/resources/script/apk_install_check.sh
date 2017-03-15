#!/bin/bash
# writer.sh
#参数介绍 1:设备id 2:设备名 3:adb变量 4:uiautomator dump0->pull本地文件目录
deviceId=$1
deviceName=$2
envPath=$3
buildTool=$4
dumpPath=$5

TYPE_LOG=1
TYPE_INIT_PID=2

# 导入环境变量
exportEnv(){
	export PATH=${PATH}:${envPath}/platform-tools/;
	export PATH=${PATH}:${envPath}/tools/;
	export PATH=${PATH}:${buildTool}/;
}

message(){
	echo "{\"type\": ${1},\"message\": \"${2}\"}"
}

# 导入当前界面树信息到电脑
uidump(){
	# 如果文件存在,删除文件
	if [ -f $dumpPath ] ;then
		rm $dumpPath
	fi
	result=$(adb -s $deviceId shell uiautomator dump /sdcard/ui.xml)
	if [[ $result =~ "UI hierchary dumped to:" ]] ;then
		message $TYPE_LOG "包名:$package 命令生成界面文件成功!"
		pull=$(adb -s $deviceId pull /sdcard/ui.xml $dumpPath)
		# 检测此返回信息是否匹配
		if [ -f $dumpPath ] ;then
			message $TYPE_LOG "包名:$package 上传文件到电脑成功!"
			exit 0
		else
			message $TYPE_LOG "包名:$package 上传文件到电脑失败!"
			exit 1
		fi
	else
		exit 1
	fi
}

# 导入环境变量
exportEnv
# 初始化,并启动任务
message $TYPE_INIT_PID $$
#导入界面信息
uidump

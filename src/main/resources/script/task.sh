#! /bin/bash
# test.sh
#传入参数声明:1:设备id 2:设备名 3:adb变量 4:测试apk包 5:测试test_apk 6:执行次数 7:结束时间 一直循环则为-1,否则则为结束日期的秒值 8:测试用例名称
#1:导入adb环境变量
#2:检测两个路径的apk文件,签名是否一致
#3:整理出消息格式 {type:1,"message":"运行1次"}},以及记录进程间问题
#4:整理生成文档


#message type
# 1:初始化pid
# 2:apk签名不
# 3:Log
# 4:应用安装失败了

TYPE_LOG=1
TYPE_INIT_PID=2
TYPE_RUN_COMPLETE=3
TYPE_RUN_LOOP=4
TYPE_RUN_RESULT=5

#无限循环时间标记
NO_TIME=-1

#维护socket及常规服务的客户端包名
CLIENT_PACKAGE="quant.testclient"

# 传入参数
deviceId=$1
deviceName=$2
envPath=$3
testApk1=$4
testApk2=$5
runCount=$6
endTime=$7
testCaseName=$8


# 生成通信消息 如{"type":1,"message":"pid"}
message(){
	echo "{\"type\": ${1},\"message\": \"${2}\"}"
}

# 导入环境变量
exportEnv(){
	export PATH=${PATH}:${envPath}/platform-tools/;
	export PATH=${PATH}:${envPath}/tools/;
	export PATH=${PATH}:${envPath}/build-tools/22.0.1/;
}

# 检测客户端程序是否运行.无运行,帮忙重启
checkClient(){
	pkg=$(adb -s $deviceId shell ps | grep $CLIENT_PACKAGE | awk '{print $9}')
	if [ -z "$pkg" ]
	then
		# 如果为空,则重启应用
		adb shell am force-stop $CLIENT_PACKAGE
		adb shell am start -n $CLIENT_PACKAGE/$CLIENT_PACKAGE.MainActivity  > /dev/null
	fi
}

# 执行测试用例
startTestCase(){
	# 测试包包名
	testPackage=$(aapt dump badging $testApk2 | awk '/package/{gsub("name=|'"'"'","");  print $2}')
	# 启动测试用例
	result=$(adb -s $deviceId shell am instrument -w $testPackage/android.support.test.runner.AndroidJUnitRunner)
	# message $TYPE_RUN_RESULT "$result"
}

# 开始执行测试用例
initTestCase(){
	# 检测执行
	if [ 0 -ne $runCount ] ; then
		message $TYPE_LOG "设备:$deviceName 继续执行任务:${testCaseName}"
	fi
	while :
	do
		if [ $NO_TIME -eq $endTime -o `date +%s` -lt $endTime ]
		then
			let runCount++
			message $TYPE_LOG "设备:$deviceName 第${runCount}次执行用例:$testCaseName"
			# 检测客户端程序是否运行,未运行,启动客户端
			checkClient
			# 执行测试用例
			startTestCase
			# 一次任务执行完毕,通知客户端,执行检测
			message $TYPE_RUN_LOOP $$
			# 暂停2秒,等客户端瘊定事件是否继续,若不需要,则会kill pid 结束整个事件,此处因为无法与shell进程通信.所以采取此类轮询方式
			sleep 2
		else
			message $TYPE_LOG "设备:$deviceName 执行用例:${testCaseName}执行完毕!"
			message $TYPE_RUN_COMPLETE "$$"
			exit 0
		fi
	done
}

# 导入环境变量
exportEnv

# 初始化,并启动任务
message $TYPE_INIT_PID $$
initTestCase






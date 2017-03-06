#! /bin/bash
# test.sh
#传入参数声明:1:设备id 2:设备名 3:adb变量 4:测试apk包 5:测试test_apk 6:执行次数 7:结束时间 一直循环则为-1,否则则为结束日期的秒值 8:测试用例名称
#1:导入adb环境变量
#2:检测两个路径的apk文件,签名是否一致
#3:整理出消息格式 {type:1,"message":"运行1次"}},以及记录进程间问题
#4:整理生成文档


#message type
# 1:初始化pid
# 2:apk签名不致
# 3:Log
# 4:应用安装失败了
TYPE_INIT_PID=1
TYPE_MD5_ERROR=2
TYPE_LOG=3
TYPE_INSTALL_SUCCESS=4
TYPE_INSTALL_FAILED=5
TYPE_RUN_COMPLETE=6
TYPE_RUN_LOOP=7



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


# 获取apk文件签名md5
apkMd5(){
	folder=$(pwd)
	cert_XSA1=`jar tf $apkFile | grep SA`
	# 解压RSA文件
	jar xf $apkFile $cert_XSA1
	md5=$(keytool -printcert -file $cert_XSA1 | grep MD5)
	# 删除解压包的临时文件
	rm -rf ${folder}"/META-INF"
	echo $md5 | awk '{print $2}'
}

#比较两个apk文件签名
equalsApkMd5(){
	v1=$(apkMd5 $testApk1)
	v2=$(apkMd5 $testApk2)
	if [ "$v1" = "$v2" ]
	then
		return 0
	else
		return -1
	fi
}

# 检测客户端程序是否运行.无运行,帮忙重启
checkClient(){
	pkg=$(adb -s $deviceId shell ps | grep $CLIENT_PACKAGE | awk '{print $9}')
	if [ -z "$pkg" ]
	then
		# 如果为空,则重启应用
		adb shell am start -n $CLIENT_PACKAGE/$CLIENT_PACKAGE.MainActivity  > /dev/null
	fi
}


#初始化数据
prepareTestCase(){
	# 安装包列表
	apkArray=($testApk1 $testApk2)
	# 这里检测应用是否安装,如果安装
	for i in ${apkArray[*]}
	do
		# 获取apk包名
		package=$(aapt dump badging $i | awk '/package/{gsub("name=|'"'"'","");  print $2}')
		# 卸载应用
		adb -s $deviceId uninstall $package > /dev/null
		# 安装应用
		adb -s $deviceId install $i > /dev/null
		# 检测应用是否安装
		filterPackage=$(adb shell pm list packages | grep $package)
		if [ "$filterPackage"=="package:$package" ]
		then
			message $TYPE_INSTALL_SUCCESS "$package"
		else
			message $TYPE_INSTALL_FAILED "$package"
		fi
	done
}

# 执行测试用例
startTestCase(){
	# 主包包名
	testPackage=$(aapt dump badging $testApk2 | awk '/package/{gsub("name=|'"'"'","");  print $2}')
	# 启动测试用例
	test=$(adb -s $deviceId shell am instrument -w $testPackage/android.support.test.runner.AndroidJUnitRunner)
}

# 开始执行测试用例
initTestCase(){
	# 检测执行
	if [ 0 -eq $runCount ]
	then
		# 准备测试用例
        prepareTestCase
	else
		message $TYPE_LOG "设备:$deviceName 继续执行任务:${testCaseName}}"
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
			exit
		fi
	done
}

# 导入环境变量
exportEnv

# 检测apk的签名
equalsApkMd5
if [ 0 -eq $? ]
then
	# 初始化,并启动任务
	message $TYPE_INIT_PID $$
	initTestCase
else
	# 签名不一致,终止测试
	message $TYPE_MD5_ERROR "应用与测试包签名不同,无法执行测试"
fi






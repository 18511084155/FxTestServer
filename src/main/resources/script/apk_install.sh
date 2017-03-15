#! /bin/bash
# test.sh
#传入参数声明:1:设备id 2:设备名 3:adb变量 4:build-tool 5:测试apk包 6:测试test_apk 7:测试用例名称 8:当前手机版本
#1:导入adb环境变量
#2:检测两个路径的apk文件,签名是否一致
#3:整理出消息格式 {type:1,"message":"运行1次"}},以及记录进程间问题
#4:整理生成文档


#message type
# 1:初始化pid
# 2:apk签名不致
# 3:Log
# 4:应用安装失败了
TYPE_LOG=1
TYPE_INIT_PID=2
TYPE_INSTALL_SUCCESS=3
TYPE_INSTALL_CHECK=4
TYPE_USER_RESTRICTED=5


#维护socket及常规服务的客户端包名
CLIENT_PACKAGE="quant.testclient"

# 传入参数
deviceId=$1
deviceName=$2
envPath=$3
buildTool=$4
testApk1=$5
testApk2=$6
testCaseName=$7
deviceSdk=$8


# 生成通信消息 如{"type":1,"message":"pid"}
 message(){
 	echo "{\"type\": ${1},\"message\": \"${2}\"}"
 }

# 导入环境变量
exportEnv(){
	export PATH=${PATH}:${envPath}/platform-tools/;
	export PATH=${PATH}:${envPath}/tools/;
	export PATH=${PATH}:${buildTool}/;
}

# 检测客户端程序是否运行.无运行,帮忙重启
checkClient(){
	# 检测客户端在不在前端显示,不在帮助启动
	topPackage=$(adb -s $deviceId shell dumpsys activity top | awk 'NR==1{print $2}')
	if [ "$topPackage" != "$CLIENT_PACKAGE" ] ;then
		message $TYPE_LOG "设备:$deviceName 当前顶端应用:$topPackage 被重启!"
		adb -s $deviceId shell am start -n $CLIENT_PACKAGE/$CLIENT_PACKAGE.MainActivity  > /dev/null
	fi
	pkg=$(adb -s $deviceId shell ps | grep $CLIENT_PACKAGE | awk '{print $9}')
	if [ -z "$pkg" ]
	then
		# 如果为空,则重启应用
		adb -s $deviceId shell am force-stop $CLIENT_PACKAGE
		adb -s $deviceId shell am start -n $CLIENT_PACKAGE/$CLIENT_PACKAGE.MainActivity  > /dev/null
	fi
}


#检测应用是否安装
checkApkInstallComplete(){
    result=-1
    package=$1
    filterPackage=$(adb -s $deviceId shell pm list packages | grep $package )
    for line in  $filterPackage;do
        # 去掉最后一个不知名的异常字符,不为\n 也不为空格.尼码.这里巨坑.
        length=${#line}
        realStr=${line:0:$length-1}
        if [ "package:$package" = "$realStr" ];then
             result=0
        fi
    done
    return $result
}

# 检测应用安装
checkApkInstall(){
	#发送信息,通知应用可以执行dump操作
	message $TYPE_INSTALL_CHECK "$package"
	message $TYPE_LOG "包:$package 上传完毕,进程等待确认!"
}


# 导入所有进程通信变量
exportAllFields(){
	export -f message
	export -f checkApkInstall
	export TYPE_LOG
    export TYPE_INSTALL_CHECK
    export TYPE_CHECK_INSTALL_COMPLETE
    export TYPE_CHECK_INSTALL_FAILED
    export TYPE_USER_RESTRICTED
}

# 安装应用程序
installApkFile(){
	apkFile=$1
	package=$2
	# 导入包名
	export package
	# 安装应用,这里检测到手机版本大于21时,厂商会弹出安装确认窗,所以需要执行uidump方法,与客户端交互,获取UI元素,并点击
	adb -s $deviceId install $apkFile | awk '{if ($0~/^\[100%\]/ && '$deviceSdk' >= 21 ) system("checkApkInstall");
											else if($0~/^Success/) system("sleep 2");
											else if($2~/\[INSTALL_FAILED_USER_RESTRICTED\]/) system("message $TYPE_USER_RESTRICTED 用户拒绝:${package}安装") }'
	# 检测应用是否安装,很难分析install信息获得,因为awk输出每一行时,有时分漏掉最后一句,但最后一句就是成功与否的关键
	sleep 2
	checkApkInstallComplete $package
	if [ 0 -eq $? ];then
		message $TYPE_LOG "设备:$deviceName 安装成功:$package"
	else
		message $TYPE_LOG "设备:$deviceName 安装失败:$package 准备重新安装"
		#重新检测安装
		adb -s $deviceId uninstall $package > /dev/null
		installApkFile $apkFile $package
	fi
}

#初始化数据
prepareTestCase(){
	# 导入所有子进程变量
	exportAllFields
	apkArray=($testApk1 $testApk2)
	i=0
	packages=()
	for apk in ${apkArray[*]} ;do
		# 获取apk包名
		package=$(aapt dump badging $apk | awk '/package/{gsub("name=|'"'"'","");  print $2}')
		packages[$i]=$package
		# 卸载应用
		message $TYPE_LOG "设备:$deviceName 开始卸载应用:$package"
		adb -s $deviceId uninstall $package > /dev/null
		let i++
    done
	# 这里检测应用是否安装,如果安装
	i=0
	for apk in ${apkArray[*]} ;do
		installApkFile $apk ${packages[i]}
		let i++
	done
	message $TYPE_INSTALL_SUCCESS "$$"
}

# 导入环境变量
exportEnv

checkClient

# 初始化,并启动任务
message $TYPE_INIT_PID $$
# 准备测试用例
prepareTestCase






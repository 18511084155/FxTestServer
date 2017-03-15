#! /bin/bash
# test.sh
#传入参数声明:1:adb变量 4:测试apk包
#检测apk文件信息,提取包名/应用名,版本号,应用的签名md5

apkFile=$1
envPath=$2
buildTool=$3

# 导入环境变量
exportEnv(){
	export PATH=${PATH}:${envPath}/platform-tools/;
    export PATH=${PATH}:${envPath}/tools/;
    export PATH=${PATH}:${buildTool}/;
}


# 开始分析
startAnalysis(){
    info=$(aapt dump badging $1)
    package=$(echo $info | awk '/package/{gsub("name=|'"'"'","");  print $2}')
    # 检测是否为test包,还是安装包 test包以.test结尾

	# 以下三个通用字段
    sdkVersion=$(aapt dump badging $1 | awk '/sdkVersion:/{gsub("sdkVersion:|'"'"'","");  print}')
    targetSdkVersion=$(aapt dump badging $1 | awk '/targetSdkVersion:/{gsub("targetSdkVersion:|'"'"'","");  print}')

    keyMd5=$(apkMd5 $1)
	if [ "test" != ${package##*.} ]
	then
		# 非测试包
    	versionName=$(echo $info | awk '/package/{gsub("versionName=|'"'"'","");  print $4}')
    	# label=$(aapt dump badging $1 | awk '/application-label:/{gsub("application-label:|'"'"'","");  print}')
    	message $package $versionName $sdkVersion $targetSdkVersion $keyMd5
	else
		# 测试包
		testMessage $package $sdkVersion $targetSdkVersion $keyMd5
	fi
}

# 获取apk文件签名md5
apkMd5(){
	folder=$(pwd)
	cert_XSA1=`jar tf $1 | grep SA`
	# 解压RSA文件
	jar xf $1 $cert_XSA1
	md5=$(keytool -printcert -file $cert_XSA1 | grep MD5)
	# 删除解压包的临时文件
	rm -rf ${folder}"/META-INF"
	echo $md5 | awk '{print $2}'
}


# package versionName,label sdkVersion  targetSdkVersion md5
message(){
	echo "{\"package\": \"${1}\",\"versionName\": \"${2}\",\"sdkVersion\": ${3},\"targetSdkVersion\": ${4},\"md5\": \"${5}\",\"test\": false}"
}

testMessage(){
	echo "{\"package\": \"${1}\",\"sdkVersion\": ${2},\"targetSdkVersion\": ${3},\"md5\": \"${4}\",\"test\": true}"
}

# 导入环境变量
exportEnv
# 提取信息
startAnalysis $apkFile




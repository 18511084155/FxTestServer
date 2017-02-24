#! /bin/bash
# test.sh
#传入参数声明:1:adb变量 4:测试apk包
#检测apk文件信息,提取包名/应用名,版本号,应用的签名md5

# envPath=$1
apkFile=$1

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


# 开始分析
startAnalysis(){
    info=$(aapt dump badging $apkFile)
    package=$(echo $info | awk '/package/{gsub("name=|'"'"'","");  print $2}')
    versionName=$(echo $info | awk '/package/{gsub("versionName=|'"'"'","");  print $4}')

    label=$(aapt dump badging $apkFile | awk '/application-label:/{gsub("application-label:|'"'"'","");  print}')
    sdkVersion=$(aapt dump badging $apkFile | awk '/sdkVersion:/{gsub("sdkVersion:|'"'"'","");  print}')
    targetSdkVersion=$(aapt dump badging $apkFile | awk '/targetSdkVersion:/{gsub("targetSdkVersion:|'"'"'","");  print}')

    keyMd5=$(apkMd5 $apkFile)
    message $package $versionName $label $sdkVersion $targetSdkVersion $keyMd5

}

# package versionName,label sdkVersion  targetSdkVersion md5
message(){
	echo "{\"package\": \"${1}\",\"versionName\": \"${2}\",\"label\": \"${3}\",\"sdkVersion\": ${4},\"targetSdkVersion\": ${5},\"md5\": \"${6}\"}"
}

# 导入环境变量
# exportEnv
# 提取信息
startAnalysis




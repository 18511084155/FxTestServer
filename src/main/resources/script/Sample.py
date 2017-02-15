import subprocess

print("开始执行!")
path = "C:\\Users\\Administrator\\Desktop\\app\\com.ldfs.wxkd-1.apk"
process1 = subprocess.Popen('adb install %s' % path,  shell=False, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
print("pid:%s" % process1.pid)
while True:
        line = process1.stdout.readline()
        if line:
            print(line)
        else:
            break
while True:
        line = process1.stderr.readline()
        if line:
            print(line)
        else:
            break
print("执行结束!")
process1.wait()
process1.kill()



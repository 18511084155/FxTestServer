package quant.test.server
import com.google.common.io.Files
import groovy.io.FileType
import quant.test.server.prefs.FilePrefs
import quant.test.server.scheduler.MainThreadSchedulers
import quant.test.server.service.StreamGobbler
import rx.Observable
import rx.schedulers.Schedulers

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
/**
 * Created by cz on 2017/2/16.
 */


def testGroovyList(){
    0.step(Integer.MAX_VALUE,1000){
        println "key:${it>>>24}"
    }
    int value=1000<<24
    println value
    println value>>>24
    if ((key >>> 24) < 2){
        println "error"
    }

    def map=[1:"a",2:"b"]
    def list=map.keySet() as List
    println list.getClass()

}

//项目代码行数
def readProjectLine(){
    def file=new File("/Users/cz/Desktop/master/FxTestServer/src/main/java/quant/test/server")
    if(file.exists()){
        def count=0
        file.eachFileRecurse(FileType.FILES) {
            if(it.name.endsWith('.groovy')) {
                def lineCount=it.readLines().size()
                println "File:$it.name line:$lineCount"
                count+=lineCount
            }
        }
        println "count$count"
    }
}

/**
 * 测试RxJava线程调度
 * 此测试己通过,但需要在javafx的启动时运行测试
 */
def testRxJavaSchedulers(){
    Observable.create({
        def items=[1,2,3,4]
        println Thread.currentThread().name
        it.onNext(items)
        it.onCompleted()
    }).subscribeOn(Schedulers.io()).
            observeOn(MainThreadSchedulers.mainThread()).
            subscribe({
                println Thread.currentThread().name
            })
}

def testScheduled(){
    def threadPool = Executors.newScheduledThreadPool(4)
    threadPool.scheduleAtFixedRate({println "hello1!"},3,3,TimeUnit.SECONDS)
}

def testTimer(){
    Timer timer=new Timer()
    def date=new Date()
    date.seconds+=10
    TimerTask timerTask={
        println "start timer!"
    } as TimerTask
    timer.schedule(timerTask,date)


    def date1=new Date()
    date1.seconds+=5
    timer.schedule({
        println "reset task1"
        timerTask.cancel()
        timer.purge()
    },date1)
    println "pass!"
}

/**
 * 测试文件操作
 */
def testFile(){
    def path="/Users/cz/Desktop/runApk/app-debug.apk"
    def file=new File(path)
    def fileName=path.substring(path.lastIndexOf("/")+1)
    def targetFile=new File(FilePrefs.TEST_CASE_FOLDER,fileName)
    Files.copy(file,targetFile)
}

def testTarget(){
    def file1=new File("/Users/cz/Desktop/runApk/app-debug.apk")
    def file2=new File("/Users/cz/Desktop/runApk/app-debug-androidTest-unaligned.apk")
    [file1,file2]
}

def testRegex(){
    def value="[100%] /data/local/tmp/app-debug.apk"
    def matcher=value=~/\[(100%)\]\s+(.+)/
    if(matcher){
        println matcher.group(1)
    }
}

def testXml(){
    File file=new File("ui.xml")
    Node node=new XmlParser().parse(file)
    def findItems=[]
    node.each {testNode(findItems,it)}
    findItems.each {
        println it
    }
}

def testNode(items,Node node){
    def findItem=node.attributes().find { it.value=="继续安装" }
    !findItem?:(items<<node)
    if(node.children()){
        node.children().each { testNode(items,it) }
    }
}

/**
 * 测试shell进程写入
 */
void testShell(String shell){
    def processBuilder=new ProcessBuilder(["/bin/sh", "-c", shell])
    def env=processBuilder.environment()
    env+=(System.getenv())
    processBuilder.redirectErrorStream(true)
    def process = processBuilder.start()
    final def outputStream=process.outputStream
    new Thread({
        while(true){
            outputStream.write(new String("test message").bytes)
            outputStream.flush()
            System.sleep(1000)
        }
    }).start()
    new StreamGobbler(process.inputStream,{println it}).start()
    new StreamGobbler(process.errorStream,{println it}).start()
    process.waitFor()
    println process.exitValue()
    process.destroy()
}

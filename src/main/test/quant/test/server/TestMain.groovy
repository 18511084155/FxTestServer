package quant.test.server
/**
 * Created by cz on 2017/2/16.
 */

//0.step(Integer.MAX_VALUE,1000){
//    println "key:${it>>>24}"
//}
//int value=1000<<24
//println value
//println value>>>24
//if ((key >>> 24) < 2){
//    println "error"
//}

//List<Integer> nums = Lists.newArrayList(1,1,null,2,3,4,null,5,6,7,8,9,10);
//println nums
//
//List<Integer> numsWithoutNull = nums.stream().filter({num -> num != null}).collect(Collectors.toList());
//println numsWithoutNull
//def process = Runtime.runtime.exec("/Users/cz/Desktop/master/FxTestServer/src/main/resources/script/Sample.py")
//process.inputStream.withReader {reader->reader.readLines().each {result.out<<it}}
//process.errorStream.withReader {reader->reader.readLines().each {result.error<<it}}
//process.waitFor()
//println process.exitValue()
//process.destroy()

//int exitValue=Command.exec("adb install /Users/cz/Desktop/runApk/weixinredian-c1001-debug-unaligned.apk"){
//    println it
//}
//println exitValue

//项目代码行数
//def readProjectLine(){
//    def file=new File("/Users/cz/Desktop/master/FxTestServer/src/main/java/quant/test/server")
//    if(file.exists()){
//        def count=0
//        file.eachFileRecurse(FileType.FILES) {
//            if(it.name.endsWith('.groovy')) {
//                def lineCount=it.readLines().size()
//                println "File:$it.name line:$lineCount"
//                count+=lineCount
//            }
//        }
//        println "count$count"
//    }
//}





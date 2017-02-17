package quant.test.server

import com.beust.jcommander.internal.Lists

import java.util.stream.Collectors

/**
 * Created by cz on 2017/2/16.
 */

//0.step(Integer.MAX_VALUE,1000){
//    println "key:${it>>>24}"
//}
int value=1000<<24
println value
println value>>>24
//if ((key >>> 24) < 2){
//    println "error"
//}

List<Integer> nums = Lists.newArrayList(1,1,null,2,3,4,null,5,6,7,8,9,10);
println nums

List<Integer> numsWithoutNull = nums.stream().filter({num -> num != null}).collect(Collectors.toList());
println numsWithoutNull
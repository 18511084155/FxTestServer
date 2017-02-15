package quant.test.server.protocol

import groovy.json.JsonBuilder

/**
 * Created by cz on 2017/2/6.
 */
class Json {
    static def map2(Map map){
        def builder=new JsonBuilder()
        builder{
            map.each{ "$it.key" it.value }
        }
        builder.toString()
    }
}

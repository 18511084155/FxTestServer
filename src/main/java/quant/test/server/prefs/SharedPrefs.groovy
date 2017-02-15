package quant.test.server.prefs

/**
 * Created by cz on 2017/2/15.
 */
class SharedPrefs {
    final static def CONFIG_PATH="TestServer/config"
    final static def CONFIG_NAME="config.properties"
    /**
     * 存储key value
     * @param adbPath
     */
    static def save(key,value){
        def configFolder=new File(System.properties["user.home"],CONFIG_PATH)
        if(!configFolder.exists()){
            configFolder.mkdirs()
        }
        Properties properties=new Properties()
        def file=new File(configFolder,CONFIG_NAME)
        if(file.exists()){
            properties.load(new FileInputStream(file))
        }
        properties.put(key,value as String)
        properties.store(new FileOutputStream(file),"save key:$key value:$value")
    }

    /**
     * 获取properties值
     * @return
     */
    static String get(key) {
        def value
        def configFolder=new File(System.properties["user.home"],CONFIG_PATH)
        if(configFolder.exists()) {
            Properties properties = new Properties()
            def file = new File(configFolder, CONFIG_NAME)
            if (file.exists()) {
                def inputStream = new FileInputStream(file)
                properties.load(inputStream)
                value = properties.getProperty(key)
            }
        }
        value
    }
}

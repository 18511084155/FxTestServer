package quant.test.server.prefs

/**
 * Created by cz on 2017/2/24.
 */
class FilePrefs {
    final static def CONFIG_PATH="TestServer/config"
    final static def DB_PATH="TestServer/database"
    final static def SCRIPT_PATH="TestServer/script"

    public final static File CONFIG_FOLDER=new File(System.properties["user.home"],CONFIG_PATH)
    public final static File DATABASE_FOLDER=new File(System.properties["user.home"],DB_PATH)
    public final static File SCRIPT_FOLDER=new File(System.properties["user.home"],SCRIPT_PATH)

    public final static File SCRIPT_TASK=new File(SCRIPT_FOLDER,"task.sh")
    public final static File SCRIPT_SCAN_APK=new File(SCRIPT_FOLDER,"apk_file.sh")


    static {
        ensureFolder(CONFIG_FOLDER,DATABASE_FOLDER,SCRIPT_FOLDER)
    }

    static def ensureFolder(File...folder){
        if(folder){
            folder.each { it.exists() ?: it.mkdir() }
        }
    }
}

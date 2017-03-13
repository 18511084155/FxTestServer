package quant.test.server.prefs

/**
 * Created by cz on 2017/2/24.
 */
class FilePrefs {
    final static def CONFIG_PATH="TestServer/config"
    final static def DB_PATH="TestServer/database"
    final static def SCRIPT_PATH="TestServer/script"
    final static def TEST_CASE_PATH="TestServer/testCase"
    final static def LOG_PATH="TestServer/log"
    final static def EXCEPTION_PATH="TestServer/exception"
    final static def CACHE_PATH="TestServer/dump"

    final static def SCRIPT_INSTALL_PATH="script/apk_install.sh"
    final static def SCRIPT_INSTALL_CHECK_PATH="script/apk_install_check.sh"

    public final static File CONFIG_FOLDER=new File(System.properties["user.home"],CONFIG_PATH)
    public final static File DATABASE_FOLDER=new File(System.properties["user.home"],DB_PATH)
    public final static File SCRIPT_FOLDER=new File(System.properties["user.home"],SCRIPT_PATH)
    public final static File TEST_CASE_FOLDER=new File(System.properties["user.home"],TEST_CASE_PATH)
    public final static File LOG_FOLDER=new File(System.properties["user.home"],LOG_PATH)
    public final static File EXCEPTION_FOLDER=new File(System.properties["user.home"],EXCEPTION_PATH)
    public final static File DUMP_FOLDER=new File(System.properties["user.home"],CACHE_PATH)

    public final static File SCRIPT_TASK=new File(SCRIPT_FOLDER,"task.sh")
    public final static File SCRIPT_SCAN_APK=new File(SCRIPT_FOLDER,"apk_file.sh")
    public final static File SCRIPT_INSTALL_APK=new File(SCRIPT_FOLDER,"apk_install.sh")
    public final static File SCRIPT_INSTALL_CHECK=new File(SCRIPT_FOLDER,"apk_install_check.sh")


    static {
        ensureFolder(CONFIG_FOLDER,
                DATABASE_FOLDER,
                SCRIPT_FOLDER,
                TEST_CASE_FOLDER,
                LOG_FOLDER,
                EXCEPTION_FOLDER,
                DUMP_FOLDER)
    }

    static def ensureFolder(File...folder){
        if(folder){
            folder.each { it.exists() ?: it.mkdir() }
        }
    }
}

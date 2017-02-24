package quant.test.server.prefs

/**
 * Created by cz on 2017/2/24.
 */
class FileManager {
    final static def CONFIG_PATH="TestServer/config"
    final static def DB_PATH="TestServer/db"

    final static def CONFIG_FOLDER=new File(System.properties["user.home"],CONFIG_PATH)
    final static def DATABASE_FOLDER=new File(System.properties["user.home"],DB_PATH)

    static {
        !CONFIG_FOLDER.exists()?:CONFIG_FOLDER.mkdir()
        !DATABASE_FOLDER.exists()?:DATABASE_FOLDER.mkdir()
    }
}

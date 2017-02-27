package quant.test.server.database

import quant.test.server.prefs.FileManager

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
/**
 * Created by cz on 2017/2/24.
 */
class Database {
    final static def DB_NAME="test-server.db"
    final static def TEST_CASE="test_case"
    final static def TEST_PLAN="test_plan"
    def static Connection con

    static {
        Class.forName("org.sqlite.JDBC")
        def statement =null
        try {
            statement = connection.createStatement()
            //初始化测试用例表
            statement.execute("CREATE TABLE IF NOT EXISTS test_case(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, apk1 TEXT,apk2 TEXT,app_name TEXT,app_version TEXT,package TEXT,test_package TEXT,sdk INTEGER,target_sdk INTEGER,md5_1 TEXT,md5_2 TEXT ,ct LONG,uid INTEGER)")
            //初始化测试计划列表
            statement.execute("CREATE TABLE IF NOT EXISTS test_plan(name TEXT,test_case_id INTEGER,uid INTEGER,st LONG,et LONG,cycle BOOLEAN,invalid BOOLEAN)")
        } catch (ex) {
            ex.printStackTrace()
        } finally {
            statement?.closed?:statement?.close()
        }
    }

    static Connection getConnection() {
        try {
            def localFile=new File(FileManager.DATABASE_FOLDER,DB_NAME)
            con = DriverManager.getConnection("jdbc:sqlite:"+localFile.absolutePath)
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return con;
    }

}

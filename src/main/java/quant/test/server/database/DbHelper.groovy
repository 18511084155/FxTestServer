package quant.test.server.database
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Created by cz on 2017/2/27.
 */
class DbHelper implements DbInterface{

    static final def helper=new DbHelper()

    public static DbHelper getHelper(){
        helper
    }
    @Override
    boolean insertTestCase(TestCaseItem item) {
        def connection=Database.connection
        PreparedStatement statement = connection.prepareStatement("INSERT INTO $Database.TEST_CASE(" +
                "name," +
                "apk1," +
                "apk2," +
                "app_name," +
                "app_version," +
                "package," +
                "test_package," +
                "sdk," +
                "target_sdk," +
                "md5_1," +
                "md5_2," +
                "ct," +
                "uid) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)")
        statement.setString(1,item.name)
        statement.setString(2,item.apk1)
        statement.setString(3,item.apk2)
        statement.setString(4,item.appName)
        statement.setString(5,item.appVersion)
        statement.setString(6,item.apkPackage)
        statement.setString(7,item.testPackage)
        statement.setInt(8,item.sdkVersion)
        statement.setInt(9,item.targetSdkVersion)
        statement.setString(10,item.apk1Md5)
        statement.setString(11,item.apk2Md5)
        statement.setLong(12,System.currentTimeMillis())
        statement.setInt(13,item.uid)
        boolean result = statement.execute()
        statement.closed?:statement.close()
        result
    }

    @Override
    void deleteTestCase(TestCaseItem item) {

    }

    @Override
    void updateTestCase(TestCaseItem item) {

    }

    @Override
    TestCaseItem queryTestCase(int id) {
        def connection=Database.connection
        Statement statement = connection.createStatement()
        ResultSet resultSet=statement.executeQuery("SELECT " +
                "_id," +
                "name," +
                "apk1," +
                "apk2," +
                "app_name," +
                "app_version," +
                "package," +
                "test_package," +
                "sdk," +
                "target_sdk," +
                "md5_1," +
                "md5_2," +
                "ct," +
                "uid FROM "+
                "$Database.TEST_CASE WHERE _id=$id ORDER BY _id DESC ")
        def item
        while(resultSet.next()){
            item=new TestCaseItem()
            item.id=resultSet.getInt(1)
            item.name=resultSet.getString(2)
            item.apk1=resultSet.getString(3)
            item.apk2=resultSet.getString(4)
            item.appName=resultSet.getString(5)
            item.appVersion=resultSet.getString(6)
            item.apkPackage=resultSet.getString(7)
            item.testPackage=resultSet.getString(8)
            item.sdkVersion=resultSet.getInt(9)
            item.targetSdkVersion=resultSet.getInt(10)
            item.apk1Md5=resultSet.getString(11)
            item.apk2Md5=resultSet.getString(12)
            item.ct=resultSet.getLong(13)
            item.uid=resultSet.getInt(14)
        }
        statement.closed?:statement.close()
        item
    }


    @Override
    List<TestCaseItem> queryTestCase() {
        def connection=Database.connection
        Statement statement = connection.createStatement()
        ResultSet resultSet=statement.executeQuery("SELECT " +
                "_id," +
                "name," +
                "apk1," +
                "apk2," +
                "app_name," +
                "app_version," +
                "package," +
                "test_package," +
                "sdk," +
                "target_sdk," +
                "md5_1," +
                "md5_2," +
                "ct," +
                "uid FROM "+
                "$Database.TEST_CASE ORDER BY _id DESC ")
        def items=[]
        while(resultSet.next()){
            def item=new TestCaseItem()
            item.id=resultSet.getInt(1)
            item.name=resultSet.getString(2)
            item.apk1=resultSet.getString(3)
            item.apk2=resultSet.getString(4)
            item.appName=resultSet.getString(5)
            item.appVersion=resultSet.getString(6)
            item.apkPackage=resultSet.getString(7)
            item.testPackage=resultSet.getString(8)
            item.sdkVersion=resultSet.getInt(9)
            item.targetSdkVersion=resultSet.getInt(10)
            item.apk1Md5=resultSet.getString(11)
            item.apk2Md5=resultSet.getString(12)
            item.ct=resultSet.getLong(13)
            item.uid=resultSet.getInt(14)
            items<<item
        }
        statement.closed?:statement.close()
        return items
    }

    @Override
    boolean insertTestPlan(TestPlanItem item) {
        def connection=Database.connection
        PreparedStatement statement = connection.prepareStatement("INSERT INTO $Database.TEST_PLAN(" +
                "name," +
                "test_case_id," +
                "test_case," +
                "uid," +
                "st," +
                "et," +
                "start_date," +
                "end_date," +
                "cycle," +
                "invalid) VALUES(?,?,?,?,?,?,?,?,?,?)")
        statement.setString(1,item.name)
        statement.setInt(2,item.caseId)
        statement.setString(3,item.testCase)
        statement.setInt(4,item.uid)
        statement.setLong(5,item.st)
        statement.setLong(6,item.et)
        statement.setString(7,item.startDate)
        statement.setString(8,item.endDate)
        statement.setBoolean(9,item.cycle)
        statement.setBoolean(10,item.invalid)
        boolean result=statement.execute()
        statement.closed?:statement.close()
        result
    }

    @Override
    void deleteTestPlan(TestPlanItem item) {

    }

    @Override
    void updateTestPlan(TestPlanItem item) {

    }

    @Override
    List<TestPlanItem> queryTestPlan() {
        def connection=Database.connection
        Statement statement = connection.createStatement()
        long todayTimeMillis=LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ResultSet resultSet=statement.executeQuery("SELECT " +
                "name," +
                "test_case_id," +
                "test_case," +
                "uid," +
                "st," +
                "et," +
                "start_date," +
                "end_date," +
                "cycle," +
                "invalid FROM "+
                "$Database.TEST_PLAN WHERE et>$todayTimeMillis OR cycle=1 ORDER BY _id DESC ")
        def items=[]
        while(resultSet.next()){
            def item=new TestPlanItem()
            item.name=resultSet.getString(1)
            item.caseId=resultSet.getInt(2)
            item.testCase=resultSet.getString(3)
            item.uid=resultSet.getInt(4)
            item.st=resultSet.getLong(5)
            item.et=resultSet.getLong(6)
            item.startDate=resultSet.getString(7)
            item.endDate=resultSet.getString(8)
            item.cycle=resultSet.getBoolean(9)
            item.invalid=resultSet.getBoolean(10)
            items<<item
        }
        statement.closed?:statement.close()
        return items
    }
}

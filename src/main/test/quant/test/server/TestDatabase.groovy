import quant.test.server.database.DbHelper
import quant.test.server.model.TestPlanItem

/**
 * 测试批量插入
 */
def testInsertSqlite(){
//    def connection=Database.getConnection();
//    Statement st = connection.createStatement();
//    st.execute("CREATE TABLE IF NOT EXISTS setting(name TEXT, value TEXT)");
//    try {
//        Map<String,String> items=new HashMap<>()
//        for(int i=0;i<100;i++){
//            items.put("key:$i","value:$i")
//        }
//        PreparedStatement statement = connection.prepareStatement("INSERT INTO setting(name,value) VALUES(?,?)")
//        for(Map.Entry<String,String> entry:items.entrySet()){
//            statement.setString(1,entry.key)
//            statement.setString(2,entry.value)
//            statement.addBatch()
//        }
//        statement.executeBatch()
//    } catch (SQLException ex) {
//        ex.printStackTrace();
//    }
//    st?.close()
}

/**
 * 测试查询用例
 * @return
 */
def testQueryTestCase(){
    def items=DbHelper.helper.queryTestCase()
    println items
}

/**
 * 插入插入测试计划
 */
def testInsertTestPlan(){
    TestPlanItem item=new TestPlanItem()
    item.name="任务1号"
    item.caseId=0
    item.cycle=true
    item.st=8*60*60
    item.et=15*60*60
    item.startDate="17:30:00"
    item.endDate="17:30:00"
    DbHelper.helper.insertTestPlan(item)
}

def testQueryTestPlan(){
    def items=DbHelper.helper.queryTestPlan()
    println items
}

testQueryTestPlan()



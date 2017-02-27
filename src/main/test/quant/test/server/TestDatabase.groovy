/**
 * Created by cz on 2017/2/24.
 */

//def connection=Database.getConnection();
//Statement st = connection.createStatement();
//st.execute("CREATE TABLE IF NOT EXISTS setting(name TEXT, value TEXT)");
//try {
//    Map<String,String> items=new HashMap<>()
//    for(int i=0;i<100;i++){
//        items.put("key:$i","value:$i")
//    }
//    PreparedStatement statement = connection.prepareStatement("INSERT INTO setting(name,value) VALUES(?,?)")
//    for(Map.Entry<String,String> entry:items.entrySet()){
//        statement.setString(1,entry.key)
//        statement.setString(2,entry.value)
//        statement.addBatch()
//    }
//    statement.executeBatch()
//} catch (SQLException ex) {
//    ex.printStackTrace();
//}
//st?.close()


//def items=DbHelper.helper.queryTestCase()
//println items
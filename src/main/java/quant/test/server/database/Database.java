package quant.test.server.database;


import java.sql.*;

public class Database {

    private String conString = "jdbc:sqlite:database.db";
    private Connection con = null;

    public Database() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception ex) {
             ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            con = DriverManager.getConnection(conString);
        } catch (SQLException ex) {
            ex.printStackTrace();
            con = null;
        }
        return con;
    }

    public void initTables() {
        try {
            Statement st = getConnection().createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS setting(name TEXT, value TEXT)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ################################################################################
    // # SETTING TABLE
    // ################################################################################

//    public List<ManagementAssertion.Setting> getSettings() {
//        List<ManagementAssertion.Setting> result = new ArrayList<ManagementAssertion.Setting>();
//        try {
//            Statement st = getConnection().createStatement();
//            ResultSet r = st.executeQuery("SELECT * FROM setting");
//            while (r.next()) {
//                result.add(new ManagementAssertion.Setting(r.getString("name"), r
//                        .getString("value")));
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//            result = new ArrayList<ManagementAssertion.Setting>();
//        }
//        return result;
//    }
//
//    public void createSetting(ManagementAssertion.Setting s) {
//        try {
//            Statement st = getConnection().createStatement();
//            st.execute("INSERT INTO setting (name,value) VALUES(" + s.getName()
//                    + "," + s.getValue() + ")");
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public void updateSetting(ManagementAssertion.Setting s) {
//        try {
//            Statement st = getConnection().createStatement();
//            st.execute("UPDATE setting SET value = " + s.getValue()
//                    + " WHERE name = " + s.getName());
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public Setting findSetting(String name) {
//        Setting setting = null;
//        try {
//            Statement st = getConnection().createStatement();
//            ResultSet rs = st
//                    .executeQuery("SELECT * FROM setting WHERE name = " + name);
//            while (rs.next()) {
//                setting = new Setting(rs.getString("name"),
//                        rs.getString("value"));
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null,
//                    ex);
//            setting = null;
//        }
//        return setting;
//    }
}
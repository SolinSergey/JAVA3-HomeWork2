import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    private class Entry {
        private String login;
        private String pass;
        private String nick;



        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }


    public BaseAuthService() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        System.out.println(connection);
        Statement stmt = connection.createStatement();
        System.out.println(stmt);
        entries = new ArrayList<>();
        ResultSet rs = stmt.executeQuery("SELECT login,password,nick FROM users;");
        while (rs.next()){
            entries.add(new Entry(rs.getString("login"), rs.getString("password"), rs.getString("nick")));

        }
        //for (int i=0;i<entries.size();i++){
        //    System.out.println(entries.get(i));
        //}

        //entries.add(new Entry("login2", "pass2", "nick2"));
        //entries.add(new Entry("login3", "pass3", "nick3"));
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }
}

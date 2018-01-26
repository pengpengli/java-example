package com.teclick.arch.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Created by pengli on 2017-11-03.
 */
public class Test {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");//指定连接类型
            Properties properties = new Properties();
            properties.put("user", "root");
            properties.put("password", "123456");
            properties.put("socketFactory", MySocketFactory.class.getName());
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/mysql", properties)) {
                try (PreparedStatement pst = conn.prepareStatement("select 100 from dual;")) {
                    ResultSet resultSet = pst.executeQuery();
                    while (resultSet.next()) {
                        System.out.println(resultSet.getLong(1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

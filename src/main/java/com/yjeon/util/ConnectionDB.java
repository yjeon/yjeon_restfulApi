package com.yjeon.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionDB {
	public Connection getConnection() {
		try {
			Class.forName("org.h2.Driver");
			Connection con = DriverManager.getConnection("jdbc:h2:./src/main/resources/db/myDB","sa","");
			return con;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}

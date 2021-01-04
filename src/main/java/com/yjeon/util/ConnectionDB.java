package com.yjeon.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ConnectionDB {
	public Connection getConnection() {
		try {
			Class.forName("org.h2.Driver");
			Connection con = DriverManager.getConnection("jdbc:h2:./src/main/resources/db/myDB","sa","");
			
			PreparedStatement ps = null;
			//create table 
			String sql = "CREATE TABLE IF NOT EXISTS TRANSACTION ( \n" + 
					"	ID VARCHAR(20) NOT NULL PRIMARY KEY, \n" + 
					"	TRANFLAG CHAR(1) NOT NULL, \n" + 
					"	AMOUNT VARCHAR(1000000000) NOT NULL, \n" + 
					"	TAX VARCHAR(1000000000) NOT NULL, \n" + 
					"	INSTALLMENT CHAR(2), \n" + 
					"	ORIGINTRANID VARCHAR(20), \n" + 
					"	CANCELFLAG CHAR(1), \n" + 
					"	CARDINFO VARCHAR(300) NOT NULL, \n" + 
					"	STRINGDATA VARCHAR(450) \n" + 
					")";
			ps = con.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
			return con;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}

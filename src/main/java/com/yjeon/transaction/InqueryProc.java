package com.yjeon.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;

import com.yjeon.util.AES128Util;
import com.yjeon.util.CommonUtil;
import com.yjeon.util.ConnectionDB;

public class InqueryProc {
	/*
	 * String null or empty check 
	 * transactionId
	 */
	public JSONObject valCheck(TransactionVO tranVO, JSONObject jobj) {
		boolean flag = true;
		jobj.put("transactionId", tranVO.getTransactionId());
		jobj.put("ReplyCode", "0000");
		jobj.put("ReplyMessage", "OK");
		
		try {
			//null or empty check start
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("transactionId"))) {
				jobj.put("ReplyCode", "9912");
				jobj.put("ReplyMessage", "transactionId input check");
				flag = false;
			}
			if(flag) {
				int tranIdLeng = jobj.get("transactionId").toString().length();
				if(flag && (tranIdLeng != 20)) {
					jobj.put("ReplyCode", "9913");
					jobj.put("ReplyMessage", "tranIdLeng length check (20 len)");
					flag = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			jobj.put("ReplyCode", "9999");
			jobj.put("ReplyMessage", "Data Error - InqueryProc - valCheck()");
		}
		
		return jobj;
	}
	
	public JSONObject selectData(String tranId) throws Exception {
		JSONObject rtnData = new JSONObject();
		rtnData.put("ReplyCode", "9995");
		rtnData.put("ReplyMessage", "No Exsits Origin Transaction ID - InqueryProc - selectData()");
		
		String tranFlag = "";
		String amount = "";
		String tax = "";
		String installment = "";
		String origintranId = "";
		String cancelFlag = "";
		String cardInfo = "";
		ConnectionDB db = new ConnectionDB();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			con = db.getConnection();
			String sql = "SELECT TRANFLAG, AMOUNT, TAX, INSTALLMENT, ORIGINTRANID,"
					+ " CANCELFLAG, CARDINFO FROM TRANSACTION WHERE ID = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, tranId);
			rs = ps.executeQuery();
			if(rs.next()) {
				if(rs.getString(1)!= null) tranFlag = rs.getString(1);
				if(rs.getString(2)!= null) amount = rs.getString(2);
				if(rs.getString(3)!= null) tax = rs.getString(3);
				if(rs.getString(4)!= null) installment = rs.getString(4);
				if(rs.getString(5)!= null) origintranId = rs.getString(5);
				if(rs.getString(6)!= null) cancelFlag = rs.getString(6);
				if(rs.getString(7)!= null) cardInfo = rs.getString(7);
				
				
				AES128Util aes = new AES128Util();
				String decryptData = aes.decrypt(cardInfo);
				String[] cardInfos = decryptData.split("\\|");
				rtnData.put("ccno", CommonUtil.maskingCardNum(cardInfos[0], 6, cardInfos[0].length()-3));
				rtnData.put("exp", cardInfos[1]);
				rtnData.put("cvc", cardInfos[2]);
				rtnData.put("transactionId", tranId);
				rtnData.put("amount", amount);
				rtnData.put("tax", tax);
				rtnData.put("tranFlag", tranFlag);
				rtnData.put("installment", installment);
				rtnData.put("origintranId", origintranId);
				rtnData.put("cancelFlag", cancelFlag);
				
				rtnData.put("ReplyCode", "0000");
				rtnData.put("ReplyMessage", "OK");
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			rtnData.put("ReplyCode", "9999");
			rtnData.put("ReplyMessage", "Data Error - InqueryProc - selectData()");
		} finally {
			rs.close();
			ps.close();
			con.close();
		}
		
		return rtnData;
		
	}
}

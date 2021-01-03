package com.yjeon.transaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;

import com.yjeon.util.AES128Util;
import com.yjeon.util.CommonUtil;
import com.yjeon.util.ConnectionDB;

public class CancelProc {

	/*
	 * String null or empty check
	 * transactionId, cancelAmount
	 */
	public JSONObject valCheck(TransactionVO tranVO, JSONObject jobj) {
		
		boolean flag = true;
		jobj = tranVO.toJSONValues();
		jobj.put("ReplyCode", "0000");
		jobj.put("ReplyMessage", "OK");
		
		try {
			//null or empty check start
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("transactionId"))) {
				jobj.put("ReplyCode", "9912");
				jobj.put("ReplyMessage", "transactionId input check");
				flag = false;
			}
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("amount"))) {
				jobj.put("ReplyCode", "9905");
				jobj.put("ReplyMessage", "amount input check");
				flag = false;
			}
			//null or empty check end
			if(flag) {
				int tranIdLeng = jobj.get("transactionId").toString().length();
				if(flag && (tranIdLeng != 20)) {
					jobj.put("ReplyCode", "9913");
					jobj.put("ReplyMessage", "tranIdLeng length check (20 len)");
					flag = false;
				}
				
				int amount = Integer.parseInt((String)jobj.get("amount")); 
				if(flag && (amount<100 || amount>1000000000)) {
					jobj.put("ReplyCode", "9910");
					jobj.put("ReplyMessage", "amount value check (under 1000000000 && over 100)");
					flag = false;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			jobj.put("ReplyCode", "9999");
			jobj.put("ReplyMessage", "Data Error - CancelProc - valCheck()");
		}
		
		return jobj;
	}
	
	public JSONObject dataCheck(String tranId) throws Exception {
		JSONObject originData = new JSONObject();
		originData.put("ReplyCode", "9998");
		originData.put("ReplyMessage", "No Origin Transaction ID - CancelProc - dataCheck()");
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
				originData.put("ccno", cardInfos[0]);
				originData.put("exp", cardInfos[1]);
				originData.put("cvc", cardInfos[2]);
				originData.put("transactionId", tranId);
				originData.put("amount", amount);
				originData.put("tax", tax);
				originData.put("tranFlag", tranFlag);
				originData.put("installment", installment);
				originData.put("origintranId", origintranId);
				originData.put("cancelFlag", cancelFlag);
				if("Y".equals(cancelFlag)) {
					originData.put("ReplyCode", "9997");
					originData.put("ReplyMessage", "Already Canceled Transaction");
				} else {
					originData.put("ReplyCode", "0000");
					originData.put("ReplyMessage", "OK");
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			rs.close();
			ps.close();
			con.close();
		}
		
		return originData;
	}
	
	public JSONObject approveCancelProc(JSONObject originTran, JSONObject cancelTran) throws IOException, Exception {
		JSONObject rtnData = new JSONObject();
		
		String tranId = CommonUtil.createTransactionID();
		cancelTran.put("transactionId", tranId);
		int originAmount = Integer.parseInt((String)originTran.get("amount"));
		int cancelAmount = Integer.parseInt((String)cancelTran.get("amount"));
		int totalCancelAmount = getTotalCancelAmount((String)originTran.get("transactionId"));
		
		String cancelData = "";
		
		if((totalCancelAmount+cancelAmount)>originAmount) {
			//원거래보다 취소 가 더 큰 경우 
			rtnData.put("ReplyCode", "9996");
			rtnData.put("ReplyMessage", "Cancel amount cant be bigger than original amount - CancelProc - dataCheck()");
		} else {
			
			if(originAmount>cancelAmount) {
				if((totalCancelAmount+cancelAmount)==originAmount) {
					cancelData = setCancelProc(cancelTran, originTran, "Y");
				} else {
					//partial cancel
					cancelData = setCancelProc(cancelTran, originTran, "P");
				}
				rtnData.put("transactionId", tranId);
				rtnData.put("stringData", cancelData);
				rtnData.put("ReplyCode", "0000");
				rtnData.put("ReplyMessage", "OK");
			} else if(originAmount == cancelAmount) {
				//full cancel
				cancelData = setCancelProc(cancelTran, originTran, "Y");
				rtnData.put("transactionId", tranId);
				rtnData.put("stringData", cancelData);
				rtnData.put("ReplyCode", "0000");
				rtnData.put("ReplyMessage", "OK");
			} else {
				//취소 가 더 큰 경우 
				//errorcode
				rtnData.put("ReplyCode", "9996");
				rtnData.put("ReplyMessage", "Cancel amount cant be bigger than original amount - CancelProc - dataCheck()");
			}
		}
		
		return rtnData;
	}
	
	public String setCancelProc(JSONObject cacelTran, JSONObject originTran, String type) throws IOException, Exception {
		String rtnData = "";
		String ccno = (String)originTran.get("ccno");
		String exp = (String)originTran.get("exp");
		String cvc = (String)originTran.get("cvc");
		String originTransactionId = (String)originTran.get("transactionId");
		String originAmount = (String)originTran.get("amount");
		String originTax = (String)originTran.get("tax");
		
		String installment = "00";
		String cancelTransactionId = (String)cacelTran.get("transactionId");
		String cancelAmount = (String)cacelTran.get("amount");
		String cancelTax = (String)cacelTran.get("tax");
		
		String data = ccno+"|"+exp+"|"+cvc;
		AES128Util aes = new AES128Util();
		String encData="";
		try {
			encData = aes.encrypt(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String header = "";
		header+=CommonUtil.strPadding("CANCEL", 10, "L");
		header+=CommonUtil.strPadding(cancelTransactionId, 20, "L");
		String sData = "";
		sData+=CommonUtil.strPadding(ccno, 20, "L");			//카드번호
		sData+=CommonUtil.strPadding(installment, 2, "0");		//할부개월수
		sData+=CommonUtil.strPadding(exp, 4, "L");				//유효기간
		sData+=CommonUtil.strPadding(cvc, 3, "L");				//cvc
		sData+=CommonUtil.strPadding(cancelAmount, 10, null);			//결제금액
		sData+=CommonUtil.strPadding(cancelTax, 10, "0");				//부가세
		sData+=CommonUtil.strPadding("", 20, "L");				//원거래번호(취소시만)
		sData+=CommonUtil.strPadding(encData, 300, "L");		//암호화된카드정보
		sData+=CommonUtil.strPadding("", 47, "L");			//예비필드
		int sleng = (header+sData).length();
		rtnData = CommonUtil.strPadding(String.valueOf(sleng), 4, null)+header+sData;
		
		
		ConnectionDB db = new ConnectionDB();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = db.getConnection();
			String sql = "INSERT INTO TRANSACTION VALUES (?,'2',?,?,?,?,?,?,?)";
			ps = con.prepareStatement(sql);
			ps.setString(1, cancelTransactionId);
			ps.setString(2, cancelAmount);
			ps.setString(3, cancelTax);
			ps.setString(4, installment);
			ps.setString(5, originTransactionId);
			ps.setString(6, type);
			ps.setString(7, encData);
			ps.setString(8, rtnData);
			ps.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			con = db.getConnection();
			String sql_update = "UPDATE TRANSACTION SET CANCELFLAG = ? WHERE ORIGINTRANID = ? OR ID = ?";
			ps = con.prepareStatement(sql_update);
			ps.setString(1, type);
			ps.setString(2, originTransactionId);
			ps.setString(3, originTransactionId);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ps.close();
			con.close();
		}
		
		
		return rtnData;
	}
	
	public int getTotalCancelAmount(String originTranId) throws SQLException {
		int totalAmount = 0;
		String sAmount = "";
		ConnectionDB db = new ConnectionDB();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = db.getConnection();
			String sql = "select sum(cast(amount as number)) from transaction"
					+ " where originTranId = ?  and cancelFlag = 'P' and tranFlag = '2'";
			ps = con.prepareStatement(sql);
			ps.setString(1, originTranId);
			rs = ps.executeQuery();
			if(rs.next()) {
				if(rs.getString(1)!= null) sAmount = rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ps.close();
			con.close();
		}
		if(sAmount == "") {
			return totalAmount;
		}
		totalAmount = Integer.parseInt(sAmount);
		return totalAmount;
	}
}

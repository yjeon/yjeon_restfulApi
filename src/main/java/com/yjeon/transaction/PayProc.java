package com.yjeon.transaction;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.simple.JSONObject;

import com.yjeon.util.AES128Util;
import com.yjeon.util.CommonUtil;
import com.yjeon.util.ConnectionDB;

public class PayProc {
	
	/*
	 * Approve Process
	 */
	public JSONObject approveProc(JSONObject jobj) throws Exception {
		JSONObject rtnData = new JSONObject();
		
		String tranId = CommonUtil.createTransactionID();
		String payData = setPayProc(jobj, tranId);
		
		rtnData.put("transactionId", tranId);
		rtnData.put("stringData", payData);
		
		//System.out.println(payData);
		
		return rtnData;
	}
	
	/*
	 * Create String data and Inserting data
	 */
	public String setPayProc(JSONObject jobj, String tranId) throws Exception {
		String rtnData = "";
		
		String ccno = (String)jobj.get("ccno");
		String exp = (String)jobj.get("exp");
		String cvc = (String)jobj.get("cvc");
		String amount = (String)jobj.get("amount");
		String tax = (String)jobj.get("tax");
		String installment = (String)jobj.get("installment");
		String data = ccno+"|"+exp+"|"+cvc;
		AES128Util aes = new AES128Util();
		String encData="";
		try {
			encData = aes.encrypt(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String header = "";
		header+=CommonUtil.strPadding("PAYMENT", 10, "L");
		header+=CommonUtil.strPadding(tranId, 20, "L");
		String sData = "";
		sData+=CommonUtil.strPadding(ccno, 20, "L");			//카드번호
		sData+=CommonUtil.strPadding(installment, 2, "0");		//할부개월수
		sData+=CommonUtil.strPadding(exp, 4, "L");				//유효기간
		sData+=CommonUtil.strPadding(cvc, 3, "L");				//cvc
		sData+=CommonUtil.strPadding(amount, 10, null);			//결제금액
		sData+=CommonUtil.strPadding(tax, 10, "0");				//부가세
		sData+=CommonUtil.strPadding("", 20, "L");				//원거래번호(취소시만)
		sData+=CommonUtil.strPadding(encData, 300, "L");		//암호화된카드정보
		sData+=CommonUtil.strPadding("", 47, "L");			//예비필드
		
		int sleng = (header+sData).length();
		rtnData = CommonUtil.strPadding(String.valueOf(sleng), 4, null)+header+sData;
		//System.out.println(aes.decrypt(encData ));
		
		ConnectionDB db = new ConnectionDB();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = db.getConnection();
			String sql = "INSERT INTO TRANSACTION VALUES (?,'1',?,?,?,'','N',?,?)";
			ps = con.prepareStatement(sql);
			ps.setString(1, tranId);
			ps.setString(2, amount);
			ps.setString(3, tax);
			ps.setString(4, installment);
			ps.setString(5, encData);
			ps.setString(6, rtnData);
			ps.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ps.close();
			con.close();
		}
		
		return rtnData;
	}
	
	/*
	 * String null or empty check
	 * ccno, exp, cvc, installment, amount 
	 */
	public JSONObject valCheck(TransactionVO tranVO, JSONObject jobj) {
		
		boolean flag = true;
		jobj = tranVO.toJSONValues();
		jobj.put("ReplyCode", "0000");
		jobj.put("ReplyMessage", "OK");
		
		try {
			
			//null or empty check start
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("ccno"))) {
				jobj.put("ReplyCode", "9901");
				jobj.put("ReplyMessage", "ccno input check");
				flag = false;
			}
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("exp"))) {
				jobj.put("ReplyCode", "9902");
				jobj.put("ReplyMessage", "exp input check");
				flag = false;			
			}
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("cvc"))) {
				jobj.put("ReplyCode", "9903");
				jobj.put("ReplyMessage", "cvc input check");
				flag = false;
			}
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("installment"))) {
				jobj.put("ReplyCode", "9904");
				jobj.put("ReplyMessage", "installment input check");
				flag = false;
			}
			if(flag && !CommonUtil.strNullCheck((String)jobj.get("amount"))) {
				jobj.put("ReplyCode", "9905");
				jobj.put("ReplyMessage", "amount input check");
				flag = false;
			}
			//null or empty check end
			
			if(flag) {
				//data length or value check start
				int ccnoLeng = jobj.get("ccno").toString().length();
				if(flag && (ccnoLeng <10 && ccnoLeng>16)) {
					jobj.put("ReplyCode", "9906");
					jobj.put("ReplyMessage", "ccno length check (10~16len)");
					flag = false;
				}
				int expLeng = jobj.get("exp").toString().length();
				if(flag && (expLeng!=4)) {
					jobj.put("ReplyCode", "9907");
					jobj.put("ReplyMessage", "exp length check (size 4)");
					flag = false;			
				}
				int cvcLeng = jobj.get("cvc").toString().length();
				if(flag && (cvcLeng!=3)) {
					jobj.put("ReplyCode", "9908");
					jobj.put("ReplyMessage", "cvc length check (size 3)");
					flag = false;
				}
				String installment = jobj.get("installment").toString();
				if(flag && installment.length()>2) {
					jobj.put("ReplyCode", "9909");
					jobj.put("ReplyMessage", "installment value check (under 12 && over 00)");
					flag = false;
				}
				int install = Integer.parseInt(jobj.get("installment").toString());
				if(flag && (install >12 || install<0)) {
					jobj.put("ReplyCode", "9909");
					jobj.put("ReplyMessage", "installment value check (under 12 && over 00)");
					flag = false;
				}
				int amount = Integer.parseInt((String)jobj.get("amount")); 
				if(flag && (amount<100 || amount>1000000000)) {
					jobj.put("ReplyCode", "9910");
					jobj.put("ReplyMessage", "amount value check (under 1000000000 && over 100)");
					flag = false;
				}
				String taxStr = (String)jobj.get("tax");
				if(flag && (taxStr!=null && taxStr.trim().length() != 0)) {
					int tax = Integer.parseInt(taxStr);
					if(tax>amount) {
						jobj.put("ReplyCode", "9911");
						jobj.put("ReplyMessage", "Tax cannot be bigger than amount");
						flag = false;
					}
				}
				//data length or value check end
			}
		} catch (Exception e) {
			e.printStackTrace();
			jobj.put("ReplyCode", "9999");
			jobj.put("ReplyMessage", "Data Error - PayProc - valCheck()");
		}
		
		return jobj;
	}
}

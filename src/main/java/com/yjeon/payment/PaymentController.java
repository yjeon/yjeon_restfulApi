package com.yjeon.payment;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yjeon.transaction.CancelProc;
import com.yjeon.transaction.InqueryProc;
import com.yjeon.transaction.PayProc;
import com.yjeon.transaction.TransactionVO;

@RestController
public class PaymentController {
	
	@RequestMapping(value = "/payment/reqApprove", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public JSONObject reqApprove(@RequestBody TransactionVO tranVO) {
		//init
		JSONObject jobj = new JSONObject();
		System.out.println(tranVO.toString());
		//Data Validation Check
		PayProc pp = new PayProc();
		jobj = pp.valCheck(tranVO, jobj);
		
		if ("0000".equals(jobj.get("ReplyCode"))) {
			//부가가치세 확인
			String taxStr = (String)jobj.get("tax");
			if(taxStr==null ||taxStr.trim().length() == 0) {
				//자동계산
				double amount = Integer.parseInt((String)jobj.get("amount"));
				int tax = (int) Math.round(amount/11);
				jobj.put("tax", String.valueOf(tax));
			}
			try {
				jobj = pp.approveProc(jobj);
			} catch (Exception e) {
				e.printStackTrace();
				jobj.put("ReplyCode", "9999");
				jobj.put("ReplyMessage", "Data Error - approveProc()");
			}
			
		}
		return jobj;
	}
	
	
	@RequestMapping(value = "/payment/reqCancel", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public JSONObject reqCancel(@RequestBody TransactionVO tranVO) {
		//init
		JSONObject jobj = new JSONObject();
		System.out.println(tranVO.toString());
		JSONObject originTran = new JSONObject();
		JSONObject rtnData = new JSONObject();
		//Data Validation Check
		CancelProc cp = new CancelProc();
		jobj = cp.valCheck(tranVO, jobj);
		
		if("0000".equals(jobj.get("ReplyCode"))) {
			//Data exists check
			try {
				originTran = cp.dataCheck((String)jobj.get("transactionId"));
			} catch (Exception e) {
				e.printStackTrace();
				rtnData.put("ReplyCode", "9999");
				rtnData.put("ReplyMessage", "Data Error - dataCheck()");
			}
		}
		
		if("0000".equals(originTran.get("ReplyCode"))) {
			//case data exists
			//rtnData = originTran;
			//tax calculate
			String taxStr = (String)jobj.get("tax");
			if(taxStr==null ||taxStr.trim().length() == 0) {
				//결제데이터의 부가가치세로 금액으로
				jobj.put("tax", originTran.get("tax"));
			}
			try {
				rtnData = cp.approveCancelProc(originTran, jobj);
			} catch (Exception e) {
				e.printStackTrace();
				rtnData.put("ReplyCode", "9999");
				rtnData.put("ReplyMessage", "Data Error - reqCancel()");
			}
			
		} else {
			rtnData.put("ReplyCode", jobj.get("ReplyCode"));
			rtnData.put("ReplyMessage", jobj.get("ReplyMessage"));
		}
		
		return rtnData;
	}
	
	@RequestMapping(value = "/payment/reqInquery", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public JSONObject inqueryTransaction(@RequestBody TransactionVO tranVO) throws Exception {
		JSONObject jobj = new JSONObject();
		System.out.println(tranVO.toString());
		//Data Validation Check
		InqueryProc ip = new InqueryProc();
		jobj = ip.valCheck(tranVO, jobj);
		if ("0000".equals(jobj.get("ReplyCode"))) {
			jobj = ip.selectData(jobj.get("transactionId").toString()); 
		}
		return jobj;
	}
	
}

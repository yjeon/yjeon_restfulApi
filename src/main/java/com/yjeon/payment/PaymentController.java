package com.yjeon.payment;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yjeon.transaction.PayProc;
import com.yjeon.transaction.TransactionVO;

@RestController
public class PaymentController {
	
	@RequestMapping(value = "payment/reqApprove.do", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public JSONObject reqApprove(@RequestBody TransactionVO tranVO) {
		//init
		JSONObject jobj = new JSONObject();
		//JSONObject rtnData = new JSONObject();
		
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
	
	
}

package com.yjeon.payment;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yjeon.transaction.TransactionVO;

@RestController
public class PaymentController {
	
	@RequestMapping(value = "payment/req", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public JSONObject req(@RequestBody TransactionVO tranVO) {
		String ccno = tranVO.getCcno();
		String amount = tranVO.getAmount();
		String cvc = tranVO.getCvc();
		String exp = tranVO.getExp();
		//System.out.println(test);
		JSONObject jobj = new JSONObject();
		jobj.put("ccno", ccno);
		jobj.put("amount", amount);
		jobj.put("cvc", cvc);
		jobj.put("exp", exp);
		
		return jobj;
	}
}

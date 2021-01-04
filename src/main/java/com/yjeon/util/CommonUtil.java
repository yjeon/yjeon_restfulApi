package com.yjeon.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CommonUtil {
	
	
	/*
	 * String null or empty check
	 */
	public static boolean strNullCheck(String str) throws Exception{
		boolean flag = true;
		if(str == null || str.trim().length() == 0) {
			flag = false;
		}
		return flag;
	}
	
	/*
	 *  Adding padding for String value 
	 */
	public static String strPadding(String str, int leng, String type) {
		String rtnValue = "";
		int numLength = str.length();
		
		if(numLength != leng) {
			
			if("0".equals(type)) {
				//case type = 숫자(0)
				for(int i = 0; i<(leng-numLength); i++) {
					rtnValue+="0";
				}
				rtnValue+=str;
			} else if("L".equals(type)) {
				//case type = 숫자(L) or 문자
				rtnValue+=str;
				for(int i = 0; i<(leng-numLength); i++) {
					rtnValue+="_";
				}
			} else {
				//case type is null 숫자
				for(int i = 0; i<(leng-numLength); i++) {
					rtnValue+="_";
				}
				rtnValue+=str;
			}
		} else {
			rtnValue = str;
		}
		return rtnValue;
	}
	
	/*
	 * create unique txn id 
	 */	
	public static String createTransactionID() {
		
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date d = new Date();
		String dateTime = sFormat.format(d).toString();
		
		String temp = "abcdeABCDEF0123456789";
		
		int len = temp.length();
		String key ="";
		
		Random rand = new Random();
		
		for(int i = 0; i<6;  i++) {
			key+= temp.charAt(rand.nextInt(len));
		}
		
		return dateTime+key;
	}
	
	//card masking process.
	public static String maskingCardNum(String ccno, int start, int end) {
		String rtnData = "";
		
		if(ccno == null) return rtnData;
		if(ccno.length()<end) return ccno;
		
		for(int i =0; i<ccno.length(); i++) {
			if(i>=start && i<end) {
				rtnData+="*";
			} else {
				rtnData+=ccno.charAt(i);
			}
		}
		return rtnData;
	}
}

package com.yjeon.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yjeon.transaction.TransactionVO;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTests {
	
	@Autowired
	private MockMvc mvc;
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	@Before 
	public void setup() throws Exception {

		this.mvc.perform(post("/payment/reqApprove")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print());
	}
	
	@Test 
	public void transactionVOTest1() throws Exception{
		TransactionVO tranVO = new TransactionVO();
		tranVO.setAmount("1000"); 
		tranVO.setCcno("1234123412341234");
		tranVO.setCvc("777"); 
		tranVO.setExp("0325"); 
		tranVO.setInstallment("00");
		final String amount = tranVO.getAmount();
		assertEquals("1000", amount);
	}
	
	@Test
	public void approveTest1() throws Exception{
		TransactionVO tranVO = new TransactionVO();
		tranVO.setAmount("1001"); 
		tranVO.setCcno("1234123412341234");
		tranVO.setCvc("777"); 
		tranVO.setExp("0325"); 
		tranVO.setInstallment("00");
		String param = objectMapper.writeValueAsString(tranVO.toJSONValues());
		mvc.perform(post("/payment/reqApprove").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andDo(print());
	}
	
	@Test
	public void approveReplyCodeTest1() throws Exception{
		//amount validation
		TransactionVO tranVO = new TransactionVO();
		tranVO.setAmount("10"); 
		tranVO.setCcno("1234123412341234");
		tranVO.setCvc("777"); 
		tranVO.setExp("0325"); 
		tranVO.setInstallment("00");
		String param = objectMapper.writeValueAsString(tranVO.toJSONValues());
		mvc.perform(post("/payment/reqApprove").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("9910"))
         .andDo(print());
	}
	
	@Test
	public void approveReplyCodeTest2() throws Exception{
		//ccno validation
		TransactionVO tranVO = new TransactionVO();
		tranVO.setAmount("1000"); 
		tranVO.setCcno("");
		tranVO.setCvc("777"); 
		tranVO.setExp("0325"); 
		tranVO.setInstallment("00");
		String param = objectMapper.writeValueAsString(tranVO.toJSONValues());
		mvc.perform(post("/payment/reqApprove").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("9901"))
         .andDo(print());
	}
	
	@Test
	public void approveReplyCodeTest3() throws Exception{
		//exp length check
		TransactionVO tranVO = new TransactionVO();
		tranVO.setAmount("1000"); 
		tranVO.setCcno("1234123412341234");
		tranVO.setCvc("777"); 
		tranVO.setExp("023"); 
		tranVO.setInstallment("00");
		String param = objectMapper.writeValueAsString(tranVO.toJSONValues());
		mvc.perform(post("/payment/reqApprove").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("9907"))
         .andDo(print());
	}
	
	@Test
	public void cancelTest1() throws Exception{
		JSONObject jobj = new JSONObject();
		jobj.put("transactionId", "20210104010640D8e7Be");
		jobj.put("amount", "1000");
		
		String param = objectMapper.writeValueAsString(jobj);
		mvc.perform(post("/payment/reqCancel").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk()).andDo(print());
		
	}
	
	@Test
	public void cancelReplyCodeTest1() throws Exception{
		JSONObject jobj = new JSONObject();
		jobj.put("amount", "1000");
		
		String param = objectMapper.writeValueAsString(jobj);
		mvc.perform(post("/payment/reqCancel").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("9912"))
         .andDo(print());
		
	}
	@Test
	public void cancelReplyCodeTest2() throws Exception{
		JSONObject jobj = new JSONObject();
		jobj.put("amount", "1000");
		jobj.put("transactionId", "20210104010640D8e7B");
		String param = objectMapper.writeValueAsString(jobj);
		mvc.perform(post("/payment/reqCancel").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("9913"))
         .andDo(print());
		
	}
	
	@Test
	public void inqueryTest1() throws Exception{
		JSONObject jobj = new JSONObject();
		jobj.put("transactionId", "20210104010640D8e7Be");
		
		String param = objectMapper.writeValueAsString(jobj);
		mvc.perform(post("/payment/reqInquery").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("0000"))
         .andDo(print());
		
	}
	
	@Test
	public void inqueryReplyCodeTest1() throws Exception{
		JSONObject jobj = new JSONObject();
		jobj.put("transactionId", "20210104010640D8e7B0");
		
		String param = objectMapper.writeValueAsString(jobj);
		mvc.perform(post("/payment/reqInquery").contentType(MediaType.APPLICATION_JSON).content(param))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.ReplyCode").value("9995"))
         .andDo(print());
		
	}
	
}

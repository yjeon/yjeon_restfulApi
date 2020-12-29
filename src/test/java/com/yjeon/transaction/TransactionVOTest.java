package com.yjeon.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TransactionVOTest {

	@Test
	public void getCcno() {
		final TransactionVO tranvo = new TransactionVO();
		tranvo.setCcno("1234567891234567");
		final String ccno = tranvo.getCcno();
		assertEquals("1234567891234567", ccno);
	}
}

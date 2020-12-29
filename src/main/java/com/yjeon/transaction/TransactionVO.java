package com.yjeon.transaction;

public class TransactionVO {
	private String ccno;
	private String exp;
	private String cvc;
	
	private String amount;
	private String tax;
	private String installment;
	public String getCcno() {
		return ccno;
	}
	public void setCcno(String ccno) {
		this.ccno = ccno;
	}
	public String getExp() {
		return exp;
	}
	public void setExp(String exp) {
		this.exp = exp;
	}
	public String getCvc() {
		return cvc;
	}
	public void setCvc(String cvc) {
		this.cvc = cvc;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getTax() {
		return tax;
	}
	public void setTax(String tax) {
		this.tax = tax;
	}
	public String getInstallment() {
		return installment;
	}
	public void setInstallment(String installment) {
		this.installment = installment;
	}
	
	@Override
	public String toString() {
		return "TransactionVO [ccno=" + ccno + ", exp=" + exp + ", cvc=" + cvc + ", amount=" + amount + ", tax=" + tax
				+ ", installment=" + installment + "]";
	}
	
}

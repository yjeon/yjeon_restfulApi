# yjeon_restfulApi
결제시스템 (승인, 취소, 부분취소, 조회)

## 개발 프레임워크
```bash
Java, Spring Framework Boot, Maven, H2 Database(embedded)
```

## 테이블 정보
```bash
Table : Transaction
```
|Column Name | Type | Size | isNull | PK|
|--|--|--|--|--|
|ID|VARCHAR|20|N|PK|
|TRANFLAG|CHAR|1|N||
|AMOUNT|VARCHAR|1000000000|N||
|TAX|VARCHAR|1000000000|N||
|INSTALLMENT|CHAR|2|Y||
|ORIGINTRANID|VARCHAR|20|Y||
|CANCELFALG|CHAR|1|Y||
|CARDINFO|VARCHAR|300|N||
|STRINGDATA|VARCHAR|450|Y||


## 문제해결 및 전략
Request를 VO에 담고, Validation 체크 후 승인프로세스로 넘어가 데이터베이스에 넣습니다.
승인 프로세스를 개발 후 조회 프로세스 개발 하여 시간 절약을 했습니다.
취소 프로세스의 경우, 승인, 조회와 같은 방식으나, 부분취소와 전체취소의 개념을 DB에 구분자로 구분하여, 같은 프로세스를 사용하여 공통소스로 개발하였습니다.


## 빌드 및 실행 방법
- 빌드 방법
Project를 import하신 후 maven에서 Update Project합니다.
Manven install을 통해 Maven Depenedncies의 파일들을 받고 maven build 통해 war 생성합니다.

- 실행 방법
1. Project를 eclipse에서 실행 시킨다. (Spring Boot App)
2. 생성 된 war를 실행 시킵니다.
  예시) java -jar payment-0.0.1-SNAPSHOT.war




## API Spec (JSON format)
-**승인 요청 값**
|파라미터|필드 설명 | 타입 | Size | 필수값 여부 | 비고
|--|--|--|--|--|--|
|ccno| 카드번호 | (String)숫자 | 10~16 | 필수 | |
|exp| 유효기간 | (String)숫자 | 4 | 필수 | mmyy |
|cvc| CVC | (String)숫자 | 3 | 필수 |  |
|installment| 할부개월수 | (String)숫자 | 2 | 필수 | 00 일시불, 01~12 |
|amount| 결제금액 | (String)숫자 | 3~1000000000 | 필수 | 100원이상 10억 이하|
|tax| 부가가치세 | (String)숫자 | 3~1000000000 | 선택 | |

-**승인 응답 값**
|파라미터|필드 설명 | 타입 | Size |  비고
|--|--|--|--|--|
|transactionId| 관리번호 | 문자 | 20 | unique|
| stringData | 카드사 전달 문자 | 문자 | 450 |  |

-**승인 예시**
```json
Request 예시 (호출 url: http://localhost:8080/payment/reqApprove ) [post]
  {"ccno" : "123456789012345", "amount" : "3000", "cvc" : "777", "exp":"1125", "installment":"00"}
Response 예시
  {
    "transactionId": "20210104010640D8e7Be",
    "stringData": "_446PAYMENT___20210104010640D8e7Be123456789012345_____001125777______30000000000273____________________UZ4QGmS24NC8qG0w+P2a9q0vTDRISGjyk6GsN8dAMuA=_______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________"
  }
```

-**취소 요청 값**
|파라미터|필드 설명 | 타입 | Size | 필수값 여부 | 비고
|--|--|--|--|--|--|
|transactionId| 관리번호 | 문자 | 20 | 필수 |unique|
|amount| 취소금액 | (String)숫자 | 3~1000000000 | 필수 | 100원이상 10억 이하|
|tax| 부가가치세 | (String)숫자 | 3~1000000000 | 선택 | |

-**취소 응답 값**
|파라미터|필드 설명 | 타입 | Size |  비고
|--|--|--|--|--|
|transactionId| 관리번호 | 문자 | 20 | unique|
| stringData | 카드사 전달 문자 | 문자 | 450 |  |


-**취소 예시**
```json
Request 예시 (호출 url: http://localhost:8080/payment/reqCancel ) [post]
  {
    "transactionId": "20210104010640D8e7Be",
    "amount": "1000"
  }
Response 예시
  {
    "ReplyCode": "0000",
    "transactionId": "202101040107404CdD5A",
    "stringData": "_446CANCEL____202101040107404CdD5A123456789012345_____001125777______10000000000273____________________UZ4QGmS24NC8qG0w+P2a9q0vTDRISGjyk6GsN8dAMuA=_______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________",
    "ReplyMessage": "OK"
  }
```

-**조회 요청 값**
|파라미터|필드 설명 | 타입 | Size | 필수값 여부 | 비고
|--|--|--|--|--|--|
|transactionId| 관리번호 | 문자 | 20 | 필수 |unique|


-**조회 응답 값**
|파라미터|필드 설명 | 타입 | Size |  비고
|--|--|--|--|--|
|ccno| 카드번호 | (String)숫자 | 10~16 |  |
|exp| 유효기간 | (String)숫자 | 4 |  mmyy |
|cvc| CVC | (String)숫자 | 3 | |
|installment| 할부개월수 | (String)숫자 | 2 |  00 일시불, 01~12 |
|amount| 결제금액 | (String)숫자 | 3~1000000000 |  100원이상 10억 이하|
|tax| 부가가치세 | (String)숫자 | 3~1000000000 |  |
|origintranId| 승인 관리번호 | 문자 | 20 |  |
|cancelFlag| 취소 여부 | 문자 | 1 |  N: 취소 안됨, P: 부분취소, Y: 전체취소|
|tranFlag| 결제구분 | 문자 | 1 | 1:승인, 2:취소|

-**조회 예시**
```json
Request 예시 (호출 url: http://localhost:8080/payment/reqInquery ) [post]
  {
    "transactionId": "20210104010640D8e7Be"
  }
Response 예시
  {
      "cvc": "777",
      "amount": "3000",
      "tranFlag": "1",
      "ReplyCode": "0000",
      "installment": "00",
      "origintranId": "",
      "tax": "273",
      "exp": "1125",
      "cancelFlag": "N",
      "ReplyMessage": "OK",
      "transactionId": "20210104010640D8e7Be",
      "ccno": "123456789012345"
  }
```


## 응답코드
|ReplyCode|ReplyMessage|
|--|--|
|0000|OK                                                                           |
|9901|Ccno input check                                                             |
|9902|Exp input check                                                              |
|9903|Cvc input check                                                              |
|9904|Installment input check                                                      |
|9905|Amount input check                                                           |
|9906|Ccno length check (10~16len)                                                 |
|9907|Exp length check (size 4)                                                    |
|9908|Cvc length check (size 3)                                                    |
|9909|Installment value check (under 12 && over 00)                                |
|9910|Amount value check (under 1000000000 && over 100)                            |
|9911|Tax cannot be bigger than amount                                             |
|9912|TransactionId input check                                                    |
|9913|TranIdLeng length check (20 len)W                                            |
|9995|No Exsits Origin Transaction ID - InqueryProc - selectData()                 |
|9996|Cancel amount cant be bigger than original amount - CancelProc - dataCheck() |
|9997|Already Canceled Transaction                                                 |
|9998|No Origin Transaction ID - CancelProc - dataCheck()                          |
|9999|DATA ERROR (COMMON ERROR)                                                    |



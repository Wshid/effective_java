## [ITEM.60] 정확한 답이 필요할 때 float, double 피하기
### float/double 타입의 용도
- 과학/공학 계산용으로 설계
- **이진 부동소수점** 연산에 쓰임
- 넓은 범위의 수를 빠르게 정밀한 **근사치**로 계산
- **정확한 결과**가 필요할 때는 사용하면 X
- `float`/`double` 타입은 **금융 관련 계산**과는 맞지 X
- `0.1` 혹은 `10`의 음의 거듭제급 수(`10^-1`, `10^-2`)를 표현할 수 없음

### 단순 연산 예시
```java
System.out.println(1.03 - 0.42);
```
- 결과는 `0.6100..001`을 출력
```java
System.out.println(1.00 - 9 * 0.10);
```
- 결과는 `0.0999...999` 츨력

### float/double의 맹점
- 결과를 출력하기 전에 **반올림**이 해결방법?
  - 오히려 **틀린 결과**가 나올 수 있음
- 예시
  - 주머니에 1달러, 사탕이 10센트, 20센트, .. 1달러까지 다양하게 여러개
  - 10센트부터 얼마나 살 수 있을지 확인하는 코드
  ```java
  public static void main(String[] args) {
    double funds = 1.00;
    int itemsBought = 0;
    for (double price = 0.10; funds >= price; price += 0.10) {
      funds -= price;
      itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러):" + funds);
  }
  ```
- 위 코드 실행시,
  - 사탕 3개를 구입한 후
  - 잔돈은 `0.399...999`달러가 남음
    - 잘못된 결과 출력

### 올바르게 해결하는 방법
- 금융 계산에는 `BigDecimal`, `int` 혹은 `long`을 사용해야 함
- 앞 코드에서 `double` -> `BigDecimal`로 교체
  - `BigDecimal`의 생성자 중, **문자열**을 받는 생성자를 사용
  - 계산 시 **부정확한 값**이 사용되는 것을 회피하기 위한 조치
- 개선된 코드
  ``` java
  public static void main(Stringp[] args) {
    final BigDecimal TEN_CENTS = new BigDecimal(".10");

    int itemsBought = 0;
    BigDecimal funds = new BigDecimal("1.00");
    for (BigDecimal price = TEN_CENTS;
          funds.compareTo(price) >=0;
          price = price.add(TEN_CENTS)) {
      funds = funds.substract(price);
      itemsBought++;
    }

    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(달러):" + funds);
  }
  ```
- 위 코드에서 **사탕 4개**를 구입한 후 잔돈은 0달러가 남는다.


### BigDecimal의 단점
- 기본 타입보다 쓰기가 훨씬 불편하고, 훨씬 느림
  - **단발성 계산**이라면 **느리다는 문제**는 무시할 수 있으나,
    - 쓰기 불편하다는 점은 여전함
- `BigDecimal`의 대안으로 `int`혹은 `long` 타입 사용할 수 있음
  - 하지만, 다루는 **값의 크기**가 제한되고, **소수점**을 직접 관리해야 함
- 모든 계산을 달러 대신 센트로 수행하는 코드
  ```java
  public static void main(String[] args) {
    int itemsBought = 0;
    int funds = 100;
    for (int price = 10; funds >= price; price += 10) {
      funds -= price;
      itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(센트): " + funds);
  }
  ```

### 결론
- 정확한 답이 필요한 `float`/`double`을 피하라
- **소수점 추적**은 시스템 맡기고,
  - 코딩 시의 불편함이나 성능 저하를 신경쓰지 않았다면
    - `BigDecimal`을 사용하기
- `BigDecimal`이 제공하는 **여덟 가지 반올림 모드**를 이용하여 반올림을 완벽히 제어 가능
  - **비즈니스 계산**에서 편리한 기능
- **성능**이 중요하고 **소수점**을 직접 추적 가능하고, 숫자가 너무 크지 않다면
  - `int`나 `long`을 사용
- 자료형별 사용
  - `int`
    - 숫자를 **9자리** 십진수로 표현 가능
  - `long`
    - 숫자를 **18자리** 십진수로 표현 가능
  - `BigDecimal`
    - 숫자가 **18자리** 넘어갈 경우
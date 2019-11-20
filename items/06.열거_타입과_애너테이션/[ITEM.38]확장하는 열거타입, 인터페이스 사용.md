## [ITEM.38] 확장하는 열거타입, 인터페이스

### 열거 타입
- 타입 안전 열거 패턴(typesafe enum pattern) 보다 우수함
- 단, 예외가 존재
  - **타입 안전 열거 패턴**은 **확장**할 수 있으나
  - **열거 타입**은 **확장 불가**
- **타입 안전 열거 패턴**
  - 열거한 값들을 그대로 가져온 다음,
  - **값을 더 추가하여** 다른 목적으로 사용 가능
    - **열거타입**은 불가
- 대부분의 상황에서 **열거타입**을 확장하는 것은 **좋지 않음**
  - 확장한 타입의 원소는, 기반 타입의 원소로 취급
    - 그 반대도 성립해야 하기 때문
  - **기반 타입**과 **확장된 타입**의 **순회 방법**도 모호
- 확장성을 높이려면 매우 복잡

### 확장할 수 있는 열거 타입의 사용
- `opcode`(연산 코드, operation code)
- 연산 코드의 각 원소는
  - 특정 기계가 수행하는 연산을 뜻함
- 이따금 `API`가 제공하는 **기본 연산** 외에
  - 사용자 확장 연산을 추가할 수 있도록 하는 경우가 존재
    - **열거 타입으로 해결 가능**

### 열거 타입을 활용
- 열거 타입이 **임의의 인터페이스**를 구현할 수 있음을 이용
- **연산 코드용 인터페이스 정의**
  - 열거 타입이 이 **인터페이스**를 구현하도록 함
- **열거 타입**이 인터페이스의 **표준 구현체** 역할을 한다.

### Operation 예시
- 인터페이스를 이용해, 확장 기능 열거 타입을 흉내
  ```java
  public interface Operation {
    double apply(double x, double y);
  }
  ```
  ```java
  public enum BasicOperation implements Operation {
    PLUS("+"){
      public double apply(double x, double y) { return x+y; }
    },
    MINUS("-"){
      public double apply(double x, double y) { return x-y; }
    }, ...

    private final String symbol;

    BasicOperation(String symbol) {
      this.symbol = symbol;
    }

    @Override public String toString(){
      return symbol;
    }
  }
  ```
  - 열거 타입인 `BasicOperation`은 확장할 수 없으나,
  - 인터페이스인 `Operation`은 확장 가능
    - 해당 인터페이스를 **연산의 타입**으로 활용
  - `Operation`을 구현한, 다른 열거타입으로
    - `BasicOperation`을 대체 할 수 있음

### 연산 타입의 추가
- `EXP`와 `REMAINDER`를 추가하기
- `Operation` 인터페이스를 구현한 열거 타입만 사용하면 됨
- 예시 코드
  ```java
  public enum ExtendedOperation implements Operation { 
    EXP("^") {
      public double apply(double x, double y) {
        return Math.pow(x, y);
      }
    },
    REMAINDER("%") {
      public double apply(double x, double y) {
        return x % y;
      }
    };

    private final String symbol;

    ExtendedOperation(String symbol) {
      this.symbol = symbol;
    }

    @Override public String toString() {
      return symbol;
    }
  }
  ```
  - 새로 작성한 연산의 경우
    - 기존 연산을 쓰는 곳을 대체하여 사용할 수 있음
  - `BasicOperation`이 아닌
    - `Operation` 인터페이스를 사용하도록 작성되어 있기만 하면 됨
  - `apply`가 인터페이스(`Operation`)에 정의되어 있기 때문에
    - 열거 타입에 따로 **추상 메서드**로 선언하지 않아도 됨

### 타입 수준에서의 활용
- 개별 인스턴스 수준뿐 아니라, **타입 수준**에서도
  - 기본 열거 타입 대신
  - 확장된 열거타입을 넘겨
    - 확장된 열거 타입의 **원소 모두**를 사용하게 할 수 있음
- `ExtendedOperation`의 변환 코드
  ```java
  public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    test(ExtendedOperation.class, x, y);
  }

  private static <T extends Enum<T>& Operation> void test(
    Class<T> opEnumType, double x, double y) {
        for(Operation op : opEnumType.getEnumConstants())
          System.out.println("%f %s %f = %f%n",
            x, op, y, op.apply(x, y));
    }
  ```
  - `main`
    - `test` 메서드에 `ExtendedOperation.class` 사용
      - 확장된 연산이 무엇인지 알려 줌
    - `class` 리터럴은 **한정적 타입 토큰**(ITEM.33)
  - `onEnumType`의 매개변수
    - `<T extends Enum<T> &Operation Class<T>`
      - `Class`객체가
        - 열거 타입인 동시에,
        - `Operation`의 하위 타입
      - 열거 타입이어야 **원소 순회**가 가능
      - `Operation`이어야 원소가 뜻하는 **연산**을 수행할 수 있음
- `ExtendedOpeartion` 2차 대안 코드
  - `Class` 객체 대신, 한정적 와일드 카드 타입(ITEM.31)을 넘기는 방법
  ```java
  public static void main(String[] args) {
    double x = ...;
    double y = ...;
    test(Arrays.asList(ExtendedOperation.values()), x, y);
  }

  private static void test(Collection<? extends Operation> opSet, double x, double y) {
    for(Operation op : opSet)
      System.out.printf("%f %s %f = %f%n", x, op, y , op.apply(x, y));
  }
  ```
  - `test` 메서드가 더 유연함
    - **여러 구현 타입의 연산을 조합해 호출 가능**
  - 하지만, 특정 연산에서는 `EnumSet`(ITEM.36), `EnumMap`(ITEM.37)을 사용할 수 없음
- 두 코드를 실행 시켰을 때의 결과
  ```java
  4.000000 ^ 2.000000 = 16.000000
  4.000000 % 2.000000 = 0.000000
  ```

### 인터페이스를 이용한 열거타입 흉내의 문제
- **열거 타입**끼리 구현 **상속**이 불가능 함
- 아무 상태도 의존하지 않는 경우에는
  - **디폴트 구현(ITEM.20)**으로 인터페이스 추가가 가능
- 반면, `Operation` 예시는
  - 연산 기호를 저장하고, 찾는 로직이
    - `BasicOperation`, `ExtendedOperation` 모두에 포함되어야 함
  - 공유하는 기능이 많을 경우
    - 해당 부분만 **도우미 클래스**나
    - **정적 도우미 메서드**로 분리
      - 코드 중복을 방지할 수 있음

### 자바 라이브러리 예시
  - `java.nio.file.LinkOption` 열거 타입
  - `CopyOption`과 `OpenOption` 인터페이스를 사용

### 결론
- **열거 타입 자체**는 확장 불가능
- 인터페이스와 그 인터페이스를 구현하는 **기본 열거 타입**을 사용하여 같은 효과 확보 가능
- C는 **인터페이스**를 구현해
  - 자신만의 열거 타입(또는 다른 타입)을 만들어 낼 수 있음
- `API`가 **기본 열거 타입을 직접 명시하지 않고**
  - **인터페이스 기반으로 작성**시,
    - 기본 열거 타입의 인스턴스가 쓰이는 모든 곳을 
    - **확장한 열거타입의 인스턴스로 대체 사용 가능**
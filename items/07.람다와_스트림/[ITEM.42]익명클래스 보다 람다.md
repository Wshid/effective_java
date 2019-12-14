## [ITEM.42] 익명 클래스보다 람다

### 함수 객체(function object)
- 함수 타입을 표현할 때
  - **추상 메서드**를 하나만 담은 **인터페이스** 사용
  - 또는 **추상 클래스** 사용
  - 해당 인터페이스의 인스턴스를 **function object**라 한다.

### 익명 클래스의 인스턴스를 함수 객체로 사용
- 코드 예시
  ```java
  Collections.sort(words, new Comparator<String>()) {
      public int compare(String s1, String s2) {
          return Integer.compare(s1.length(), s2.length());
      }
  }
  ```
  - 문자를 길이순으로 정렬
  - 정렬을 위한 비교함수로 **익명 클래스** 사용
- **함수 객체**를 사용하는 과거 **객체 지향 디자인 패턴**에서는
  - **익명 클래스**면 충분했다.
- `Comparator` 인터페이스
  - 정렬을 담당하는 추상 전략
  - 문자열을 정렬하는 구체 전략을
    - **익명 클래스**로 구현
- 하지만 **익명 클래스** 방식은
  - 코드의 길이가 김
  - 자바는 적합하지 않음

### 람다식(lambda expression)
- `java8:`이후
  - **추상 메서드** 하나만 존재하는 인터페이스
- 함수형 인터페이스
  - 해당 인터페이스의 인스턴스를 람다식을 사용해 만들 수 있다.
- 익명 클래스를 대체한 예시 코드
  ```java
  Collections.sort(words, 
    (s1, s1) -> Integer.compare(s1.length(), s2.length()));
  ```
- 람다, 매개변수 `(s1, s2)`, 반환값의 타입은 각각
  - `Comparator<String>`, `String`, `int`
  - 위 코드에서는 언급이 없음
    - 대신 **컴파일러**가 문맥을 살펴 추론
- 상황에 따라 자동으로 컴파일러가 타입을 못 유추할 경우
  - 프로그래머가 직접 명시해야 함
- **타입 추론 규칙**은 많ㅇ ㅣ복잡함
  - 타입을 명시해야할 코드가 **더 명확할 떄**를 제외하고는
    - **람다의 매개변수 타입은 생략**하는 것이 좋음
  - 이후 컴파일러가 **타입을 알 수 없음** 오류 발생시
    - 타입을 명시하는 방식
  - **반환 값**이나 **람다식 전체**를 형변환 할 때도 있겠지만,
    - 해당 상황은 드물다

### 타입 추론
- 이전 ITEM 제네릭 권고 사항
  - ITEM.26 : **제네릭**을 `raw` 타입으로 사용하지 말 것
  - ITEM.29 : **제네릭**을 사용할 것
  - ITEM.30 : **제네릭 메서드**를 사용할 것
- 위 사항들의 경우 **람다**와 함께 쓸 때 더 중요
- 컴파일러가 **타입 추론**을 할 때,
  - 필요한 타입 정보 대부분을 **제네릭**에서 얻기 때문
- 해당 정보를 제공하지 않을 경우
  - **컴파일러**는 **람다의 타입**을 추론할 수 **없음**
  - 결국 프로그래머가 계속 명시하는 상황 발생

### 함수 최적화
- 코드를 더 간결하게 하는 방법
- **람다** 자리에 **비교자 생성 메서드** 사용
  ```java
  Collections.sort(words, comparingInt(String::length));
  ```
- `java8` `List` interface에서 추가된 `sort` 메서드 이용
  ```java
  words.sort(comparingInt(String::length));
  ```

### 람다의 장점
- 언어 차원에서 지원하면서, 기존에서 적합하지 않은 곳에서도
  - 객체를 실용적으로 사용할 수 있게 됨
- 상수별 클래스 몸체와 데이터를 사용한 열거타입(`Operation`)
  ```java
  public enum Operation { 
      PLUS("+") {
          public double apply(double x, double y) { return x + y; }
      },
      MINUS("-") {
          public double apply(double x, double y) { return x - y ; }
      }, ...

      private final String symbol;

      Operation(String symbol) { this.symbol = symbol; }

      @Override public String toString() { return symbol; }
      public abstract double apply(double x, double y);
  }
  ```
- **ITEM.34**에서는
  - 상수별 클래스 몸체를 구현하는 방식 보다
  - 열거 타입에 **인스턴스 필드**를 두는 편이 낫다고 정의
    - 람다를 이용하면 쉽게 구현 가능
- **열거 타입 상수**의 동작을 **람다**로 구현하여
  - **생성자**에 넘기고
  - **생성자**는 이 람다를 **인스턴스 필드**로 저장
  - 이후, `apply` 메서드에서 필드에 지정된 람다만 호출하면 됨
- 개선 된 코드
  ```java
  public enum Operation {
      PLUS("+", (x,y) -> x + y),
      MINUS("-", (x,y) -> x - y),
      ...

      private final String symbol;
      private final DoubleBinaryOperator op;

      Operation(String symbol, DoubleBinaryOperator op) {
          this.symbol = symbol;
          this.op = op;
      }

      public double apply(double x, double y) {
          return op.applyAsDouble(xm y);
      }
  }
  ```
  - 열거 타입 상수의 동작을 표현한 람다를
    - `DoubleBinaryOperator` 인터페이스 변수에 할당
  - `DoubleBinaryOperator`
    - `java.util.function` 패키지가 제고하는 다양한 함수 인터페이스(ITEM.44) 중 하나
    - `Double` 타입의 인수르 2개 받아, `Double` 형태 반환

### 람다의 단점
- 상수별 클래스 몸체의 장점
  - 메서드나 클래스 와는 달리
    - 람다는 **이름이 없고, 문서화도 어려움**
    - 코드 자체로 동작이 **명확히 설명되지 않음**
    - 코드 줄 수가 많아진다면 람다를 사용하면 안됨
- 람다는 **세 줄 안에 끝내는게 좋다**
  - 그 이상일 경우, 가독성이 떨어짐
- 람다가 길거나 읽기 어렵다면
  - 더 줄이거나, 
  - 람다를 쓰지 않는 방향으로 개선
- **열거 타입 생성자**에 넘겨지는 인수들의 타입도
  - **컴파일 타임**에 추론 됨
- 즉, **열거 타입 생성자**안의 람다는
  - **열거 타입**의 **인스턴스 멤버**에 접근 불가능
  - 인스턴스가 **런타임** 때 만들어 지기 때문
- 상수별 동작을,
  - 몇 줄로 구현하기 어렵거나
  - **인스턴스 필드**나 **메서드**를 사용해야하는 상황이라면
    - **상수별 클래스 몸체**를 사용해야 함

### 익명 클래스 사용처
- 람다는 **함수형 인터페이스**에서만 사용 됨
- **추상 클래스**의 인스턴스를 만들 때
  - 람다를 사용할 수 없으므로, **익명 클래스**를 사용해야 함
- **추상 메서드**가 여러 개인 **인터페이스**의 **인스턴스** 생성시 사용
- **람다**는 자신을 참조할 수 없음
  - `this` 키워드는 바깥 인스턴스를 가리킴
  - **익명 클래스**에서의 `this`는 **익명 클래스**의 인스턴스 자신 가리킴
  - **함수 객체**가 자신을 참조해야 할 때 **익명 클래스** 사용

### 람다 사용시 주의점
- **익명 클래스** 처럼
  - **직렬화** 형태가 구현별로(가상머신 별로) 다를 수 있음
- **람다를 직렬화 하는 일은 삼가야 함**
  - 익명 클래스의 인스턴스도 마찬가지
- 직렬화 해야하는 **함수 객체**가 있다면
  - `private` 정적 중첩 클래스(ITEM.24)의 인스턴스 사용할 것

### 결론
- `java8`에서 **작은 함수 객체**를 구현하는데 **람다**가 쓰임
- **함수형 인터페이스가** 아닌 **익명 클래스**는
  - **타입의 인스턴스**를 만들때만 사용하기
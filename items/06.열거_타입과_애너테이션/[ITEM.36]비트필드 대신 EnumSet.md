## [ITEM.36] 비트필드 대신 EnumSet


### 과거의 활용법
- 열거한 값들이 주로 (단독이 아닌) 집합으로 사용될 경우
- 각 상수에 서로 다른 **2의 거듭제곱**값을 할당한 **정수 열거 패턴**을 사용했다.
- **비트 필드 열거 상수**(과거 기법)
  ```java
  public class Text {
    public static final int STYLE_BOLD = 1 << 0;
    public static final int SYTLE_ITALIC = 1 << 1;
    public static final int STYLE_UNDERLINE = 1 << 2;
    public static final int STYLE_STRIKETHROUGH = 1 <<3;

    // 매개변수 styles는 0개 이상의 SYTLE_ 상수를 비트별 OR한 값이다.
    public void applyStyles(int styles) { ... }
  }
  ```
  - **비트별 OR**을 사용하여
    - 여러 상수를 **하나의 집합**으로 모을 수 있다.
    - 예시 코드
      ```java
      text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
      ```
  - 이를 **비트 필드(bit field)**라 한다.

### 비트 필드의 특징
- 비트별 연산을 사용해
  - **합집합**, **교집합** 같은 **집합 연산**을 효율적으로 수행할 수 있음
- **정수 열거 상수**의 단점을 그대로 보유
- 비트 필드 값이 그대로 출력 되면,
  - 단순히 정수 열거 상수를 출력할때보다 **해석하기 어려움**
- 비트 필드 하나에 녹아있는 **모든 원소 순회가 어려움**
- 최대 몇 비트가 필요한지
  - **API 작성시 미리 예측해야함**
  - 적절한 타입(보통 `int`나 `long`)을 선택해야 함
  - **API** 수정하지 않고는 적절한 비트 수(32bit, 64bit)를 늘릴 수 없기 때문

### 비트 필드의 사용처, 그리고 대안
- 정수 상수보다 **열거 타입**을 선호하는 프로그래머 중,
  - **상수 집합**을 주고 받을 때 사용
- `java.util`의 `EnumSet` 클래스를 사용하면 됨

### EnumSet
- **열거 타입 상수의 값으로 구성된 집합**을 효과적으로 표현
- `Set` 인터페이스를 구현
- 타입 안전 및 다른 `Set` 구현체와 사용 가능
- 내부는 **비트 벡터**로 구현됨
- 성능 예시
  - 원소가 64개 이하일 경우,
  - `EnumSet` 전체를, `long` 변수 하나로 표현
  - 비슷한 성능을 보여준다.
- `removeAll`과 `retainAll`같은 대량 작업은
  - 비트를 효율적으로 처리할 수 있는 산술 연산을 사용
  - 비트 필드를 사용할 때와 같음
- 난해한 작업은 `EnumSet`에서 해주기 때문에
  - 비트를 직접 다룰 때의 오류 방지

### EnumSet을 활용한 개선 코드
- 개선 코드
  ```java
  public class Text {
    public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }

    // 어떤 Set을 넘겨도 되나, EnumSet이 가장 좋음
    public void applyStyles(Set<Style> styles) { ... }
  }
  ```
- C 코드
  ```java
  text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
  ```
- `EnumSet` 인스턴스를 인자로 넘기는 코드
- `EnumSet`은 집합 생성 등,
  - 다양한 기능의 **정적 팩터리** 제공
  - `of` 메서드를 활용
- `applyStyles` 메서드가
  - `EnumSet<Style>`이 아닌, `Set<Style>`을 받음
  - **이왕이면 인터페이스로 받는게 일반적으로 좋은 습관이기 때문**(ITEM.64)
    - `EnumSet`을 C에서 많이 건네 주겠지만,
      - 특이한 C가 Set 구현체를 넘기더라도 처리가 가능하기 때문

### 결론
- 열거 할 수 있는 타입을 한데 모아, **집합 형태**로 사용하더라도,
  - **비트 필드**를 사용할 이유가 **없음**
- `EnumSet`
  - 비트 필드 수준의 명료함과 성능 제공
  - 열거 타입의 장점까지 보유(ITEM.34)
- `EnumSet`의 단점
  - `:java9`에서 **Immutable EnumSet** 생성이 불가능
  - `Collections.unmodifiableSet`으로 감싸 대체하여 사용은 가능함
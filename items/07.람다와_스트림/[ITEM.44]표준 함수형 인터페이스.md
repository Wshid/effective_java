## [ITEM.44] 표준 함수형 인터페이스

### 자바가 람다를 지원하면서
- 상위 클래스의 기본 메서드를 재정의해
  - 원하는 동작을 구현하는 **템플릿 메서드 패턴**의 사용이 줄어들었다.
- 현대적인 해법
  - 같은 효과의 **함수 객체**를 매개변수로 받는 **생성자**와 **메서드**를 많이 만드는 방법
  - **함수형 매개변수 타입**을 올바르게 선택하기

### LinkedHashMap 예시
- 이 클래스의 `protected` 메서드인 `removeEldestEntry`를 재정의 하면
  - **캐시**로 사용할 수 있다.
- 맵에 새로운 키를 추가하는 `put` 메서드는
  - 이 메서드를 호출하여 `true` 반환시, 오래된 원소 제거
- `removeEldestEntry`를 다음처럼 재정의 시,
  - Map 원소를 최대 100개 유지하며, 이후 오래된 원소를 제거
  - 최근 원소 100개 유지
- `removeEldestEntry` 재정의 예시
  ```java
  protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return size() > 100;
  }
  ```

### 람다를 사용한 개선
- `LinkedHashMap`을 구현한다면,
  - 함수 객체를 받는 **정적 팩터리**나 **생성자**를 제공했을 것
- `removeEldestEntry`선언에서
  - 함수 객체는 `Map.Entry<K,V>`를 받아 `boolean` 반환
    - 해야할 것 같지만, 꼭 그렇지만은 않음
  - `removeEldestEntry`는 `size()`를 호출하여 원소 수를 알아 냄,
    - `removeEldestEntry`가 **인스턴스 메서드**여서 사용 가능
- 생성자에 넘기는 함수 객체는 이 맵의 **인스턴스 메서드**가 아님
  - **팩터리**나 **생성자**를 호출할 때는
    - 맵의 **인스턴스**가 존재하지 않기 때문
- **맵**은 결국, **자기 자신**도 **함수 객체**에 건네주어야 한다.

### 함수형 인터페이스 예시
- 예시 코드
  ```java
  // 불필요한 함수형 인터페이스 - 대신 표준 함수형 인터페이스 사용할 것
  @FunctionalInterface interface EldestEntryRemovalFunction<K,V> {
    boolean remove(Map<K,V> map, Map.Entry<K,V> eldest);
  }
  ```
- 위 예시도 잘 동작은 하나, 굳이 사용할 필요는 없음
  - 자바 표준 라이브러리에서 이미 제공
- `java.util.function` 패키지 내부에, 다양한 용도의 함수형 인터페이스가 잇음
  - **필요한 용도**에 맞는게 있다면, 직접 구현하지 말고 **표준 함수형 인터페이스**를 활용할 것
- 표준 함수형 인터페이스 에서는
  - 유용한 **default method**를 지원 함
  - 다른 코드와의 **상호 운용성**이 좋아짐
- `Predicate` 인터페이스는
  - `predicate`를 조합하는 메서드를 제공함
- `LinkedHashMap`에서
  - 직접 만든 `EldestEntryRemovalFunction` 대신
  - `BiPredicate<Map<K,V>, Map.Entry<K,V>>`를 사용하면 됨

### java.util.function
- 43개의 인터페이스
- 기본 인터페이스 **6**개
  - 모두 참조 타입용

### 기본 인터페이스(6)
- `Operator` 인터페이스
  - 인수가 1개인 `UnaryOperator`와
    - 2개인 `BinaryOperator`로 나뉜다
  - **반환 값** == **인수의 타입**
- `Predicate` 인터페이스
  - **인수 1개**를 받아 `boolean`을 반환
- `Function` 인터페이스
  - **인수** != **반환 타입**
- `Supplier` 인터페이스
  - 인수의 값을 **받지 않고**, 반환(혹은 제공)하는 함수
- `Consumer` 인터페이스
  - **인수 1개** 받고, 반환값은 **없는**(인수 소비) 함수

### 기본 인터페이스와 시그니처

| Interface         | Function Signature  | Example             |
|-------------------|---------------------|---------------------|
| UnaryOperator<T>  | T apply(T t)        | String::toLowerCase |
| BinaryOperator<T> | T apply(T t1, T t2) | BigInteger::add     |
| Predicate<T>      | boolean test(T t)   | Collection::isEmpty |
| Function<T,R>     | R apply(T t)        | Arrays::asList      |
| Supplier<T>       | T get()             | Instant::now        |
| Consumer<T>       | void accept(T t)    | System.out::println |)

#### 기본 인터페이스
- 기본 타입인 `int`, `long`, `double`용으로 3개씩 변형이 생긴다
- 이름도 인터페이스의 이름 앞에
  - 해당 기본 타입을 이름을 붙여 짓는다.
- 예시
  - `int`를 받는 `Predicate` => `IntPredicate`
  - `long`을 반환하는 `BinaryOperator` => `LongBinaryOperator`
- `Function`의 변형만 매개변수화 됨
  - 정확히는 **반환 타입**만 **매개변수화**
  - `LongFunction<int[]>`는 `long` 인수를 받아 `int[]`를 리턴

### Function 인터페이스의 기본 타입 반환 변형(9)
- 기본 타입 반환하는 변형이 9개 더 존재
- 인수와 같은 타입 반환 : `UnaryOperator`
- `Function` 인터페이스의 변형은
  - **입력 타입** != **결과 타입**
- 입력과 결과 타입이 모두 **기본 타입**(6)
  - `SrcToResult` 접두어 사용
  - `long`을 받아 `int` 반환시
      - `LongToIntFunction`
- 입력이 **객체 참조**이고, 결과가 `int, long, double` 변형(3)
  - 입력을 **매개변수화**하고
  - 접두어로 `ToResult` 사용
  - `ToLongFunction<int[]>`
    - `int[]`를 받아 `long`을 반환

### 인수를 2개씩 받는 변형(9)
- `BiPredicate<T,U>`, `BiFunction<T,U,R>`, `BiConsumer<T,U>`
  - `BiFunction`에는 기본 타입을 반환하는 3가지 존재
    - `ToIntBiFunction<T,U>`
    - `ToLongBiFunction<T,U>`
    - `ToDoubleBiFunction<T,U>`
  - `Consumer`에도 객체 참조와 기본타입 하나를 받는 변형 존재(인수가 2개)
    - `ObjDoubleConsumer<T>`
    - `ObjIntConsumer<T>`
    - `ObjLongConsumer<T>`
- 기본 인터페이스의 인수 2개 변형은 총 9개

### Supplier 변형
- `BooleanSupplier` 인터페이스
  - `boolean`을 반환하도록 하는 `Supplier`의 변형
  - 표준 함수형 인터페이스 중 `boolean`을 이름에 명시한 유일한 인터페이스
- `Predicate`와 그 변형 4개도 `boolean`을 반환할 수 있음

### 표준 함수형 인터페이스와 기본 타입
- 대부분 **기본 타입**만 지원
- 기본 함수형 인터페이스에
  - **박싱된 기본 타입**을 넣어 사용하는 것은 지양
- 동작은 할 수 있으나, **박싱된 기본 타입 대신 기본 타입을 사용하라**(ITEM.61)에 위배
- 계산량이 많을때, 성능 저하가 심하다.

### 표준 함수형 인터페이스 보다 코드를 직접 구성해야 할 때
- 표준 인터페이스 중 필요한 용도에 맞는게 없을 때
- 예
  - 매개변수 3개를 받는 `Predicate`
  - 검사 예외를 던져야 하는경우
- 구조적으로 똑같은 **표준 함수형 인터페이스**가 있더라도 직접 작성해야할 때가 있음
  - `Comparator<T>` 인터페이스 예시
    - 구조적으로는 `ToIntBiFunction<T,U>`와 동일
      - 자바 라이브러리에 `Comparator<T>`를 추가할 때
        - `ToIntBiFunction<T,U>`가 존재하더라도 사용 x
    - `Comparator`가 독자적인 인터페이스가 되어야 하는 이유
      - `API`에서 매우 많이 사용 됨
      - 구현하는 쪽에서 꼭 지켜야할 **규약**을 담고 있음
      - **비교자**들을 변환하고 조합해주는 유용한 **디폴트 메서드**가 존재
    - `Comparator`의 특징 중 하나 이상을 만족한다면, 전용 함수형 인터페이스 구성 고려
      - 자주 쓰이며, 이름 자체가 용도를 명확히 설명할 때
      - 반드시 따라야 하는 규약 존재
      - 유용한 디폴트 메서드 제공 가능 여부
- 전용 함수형 인터페이스 제작할 때, 주의해서 설계해야함(ITEM.21)

### @FunctionaInterface
- 에너테이션은 `@Override` 사용 이유와 비슷함
- 프로그래머의 의도를 명시함
  - 해당 클래스에 코드나, 설명 문서를 읽을 때, **람다용으로 설계** 되었음을 알려줌
  - 해당 인터페이스가 **추상 메서드**를 오직 **하나만**가지고 있어야 컴파일 되게 함
  - 유지보수 과정에서 실수로 **메서드 추가**를 하지 못하도록 함
- **직접 만든 함수형 인터페이스**에는 항상 `@RunctionalInterface` 에너테이션을 사용할 것

### 함수형 인터페이스를 사용시 주의점
- 서로 다른 **함수형 인터페이스**를
  - **같은 위치**의 인수로 받는 메서드들을 **다중 정의**하면 안된다.
    - 클라이언트에게 상당히 모호함
    - 모호함으로 인해 문제 발생 가능
    - 예시
      - `ExecutorService`의 `submit` 메서드는
        - `Callable<T>`를 받는 것과 `Runnable`를 받는 것을 다중정의
        - 올바른 메서드를 알려주기 위해 **형변환** 해야할 비용이 증가(ITEM.52)
    - 문제를 피하려면
      - **서로 다른 함수형 인터페이스**를 **같은 위치**의 인수로 사용하는 **다중 정의**를 피해야 함
- 다중정의는 주의해서 사용하라(ITEM.52)의 특수 예

### 결론
- 자바도 **람다**를 지원한다.
- `API`를 설계할 때, **람다**도 고려해야 함
- **입력값**과 **반환값**에 **함수형 인터페이스 타입**을 활용
  - 보통은 `java.util.function` 패키지의 **표준 함수형 인터페이스**를 사용
- 흔하진 않지만, 직접 함수형 인터페이스를 만들어야하는 경우도 존재

## [ITEM.55] Optional 반환
### 특정 조건에서 값을 반환할수 없을 때의 대처방안
- `java :8`에서의 선택지는 두가지
  - 예외를 던진다
  - `null`을 반환한다(반환 타입이 **객체 참조**일 경우)
- 두 방법엔 문제가 있음
  - 예외를 던진다
    - **진짜 예외적인 상황**에서만 사용해야 하며(ITEM.69)
    - 예외를 생성할 때, **스택 추적 전체**를 캡쳐한다
      - 비용이 높은 작업
  - `null`을 반환
    - 위와 같은 문제는 발생하지 않으나,
    - `null`을 반환할 수 있는 메서드를 호출할 경우
      - 별도의 `null`처리 코드를 추가해야 함
    - `null`처리를 무시할 경우,
      - 언젠가 `NullPointerException`이 발생할 수 있음
      - `null`을 반환하게 한 실제 원인과는 다른 곳에서 문제 발생 가능성 존재

### java 8에서의 대처방안 - Optional
- `Optional<T>`
  - `null`이 아닌 `T`타입 참조를 담거나
  - 아무것도 담지 않을 수 있음
- 아무것도 담지 않은 optional을 **비었다(empty)**로 포현
- 어떤 값을 가진 `optional`은 **비지 않음**으로 표현
- `Optional`은 원소를 최대 **1개**가질 수 있는 **불변 컬렉션**
  - `Optional<T>`가 `Collection<T>`를 구현하지는 않지만
    - 원칙적으로는 해당함

### Optional<T>의 사용방법
- 보통은 `T`를 반환해야 하나,
  - 특정 조건에서 아무것도 반환하지 않아야 할 때,
  - `T`대신, `Optional<T>`를 반환하도록 하면 된다.
- 유효한 반환값이 없을 때는,
  - **빈 결과를 반환**하는 메서드가 만들어짐
- `Optional`을 사용할 경우
  - 예외를 던지는 메서드보다 유연하고, 사용하기 쉬움
  - `null` 반환 메서드보다 오류 가능성이 적음


### 예시 - 컬렉션에서 최대값 구하기
- 기존 방식
  - 컬렉션이 비어있으면 예외를 던지도록 설계
  - 코드
    ```java
    public static <E extends Comparable<E>> E max(Collection<E> c) {
      if (c.isEmpty())
        throw new IllegalArgumentException('empty collection');
      
      E result = null;
      for (E e : c)
        if(result == null || e.compareTo(result) > 0)
          result = Objects.requireNonNull(e);
      
      return result;
    }
    ```
- `Optional<E>`를 사용하여 수정한 방식
  ```java
  public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    if (c.isEmpty())
      return Optional.empty();
    
    E result = null;
    for (E e : c)
      if (result == null || e.compareTo(result) > 0)
        result = Objects.requireNonNull(e);
    
    return Optional.of(result);
  }
  ```
- `Optional`을 사용할 때
  - 적절한 **정적 팩터리**를 사용하여 `Optional`을 생성해주면 된다.
- 두가지 정적 팩터리
  - 빈 옵셔널은 `Optional.empty()`
  - 값이 든 옵셔널은 `Optional.of(value)`
- `Optional.of(value)`에 `null`을 주입할 경우
  - `NullPointerException`을 발생시킴
- `null`값도 허용하는 `Optional`을 만들때는
  - `Optional.ofNullable(value)`를 사용하면 됨
  - **하지만, `Optional`을 반환하는 메서드에서는 절대 `null`을 반환하지 말 것**

### 스트림과 Optional
- 스트림의 **종단 연산**중 상당수가 Optional을 반환
- 위의 예시 코드의 `max` 버전을 `Stream`버전으로 만들기
  ```java
  public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    return c.stream().max(Comparator.naturalOrder());
  }
  ```

  ### Optional 반환을 선택해야 하는 기준
- `null`을 반환하거나, 예외를 던지는 대신
  - `Optional` 반환을 선택해야하는 기준은?
- **Optional은 검사 예외**와 취지가 비슷함(ITEM.71)
- **반환 값**이 없을 수도 있음을, **API**에 명시적으로 알려주는 것
- **비검사 예외**를 던지거나, `null`을 반환할 경우
  - 사용자가 그 사실을 인지하지 못해, 의도치 않은 결과 발생 가능
- 하지만 **검사 예외**를 던질 경우
  - C에서는 반드시 이에 대처하는 코드를 반환해야 함

### Optional 반환시 C의 대처방안
- 기본값 설정
  ```java
  String lastWordInLexicon = max(words).orElse("단어 없음..");
  ```
- 상황에 맞는 예외 발생
  ```java
  Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);
  ```
  - 실제 예외가 아니라, **팩터리**를 건네었다
    - **예외가 실제로 발생하지 않는 한, 예외 생성 비용은 들지 않음**
- 곧바로 값을 꺼낸 사용하기
  ```java
  Element lastNobleGas = max(Elements.NOBLE_GASES).get();
  ```
  - 단, 잘못판단시 `NoSuchElementException` 발생

### orElseGet와 고급 메서드
- `Supplier<T>`를 인수로 받는 `orElseGet`을 사용하면
  - 값이 처음 **필요할 때** `Supplier<T>`를 사용해 생성하므로, 초기 설정비용을 낮출 수 있음
    - 해당 메서드의 경우
      - `compute`로 시작하는 `Map`메서드와 밀접하므로
      - `orElseCompute`로 이름 짓는게 나을 수 있음
- 더 특별한 쓰임에 대비한 메서드
  - `filter`, `map`, `flatMap`, `isPresent`
- 앞서의 기본 메서드로 처리하기 어려울 경우
  - 고급 메서드 처리 방법 확인

### isPresent
- 안전벨브 역할 메서드
- `Optional`이 채워져 있다면 `true`
  - 비어 있다면 `false` 반환
- 이 메서드로 모든 작업을 할 수는 있으나
  - **신중하게 사용해야 함**
- 실제 `isPresent`를 사용한 코드의 상당 수는
  - 언급한 메서드로 대체할 수 있으며,
  - 그럴 경우 더 **명확**하고 **용법**에 맞게 사용

### 예시
- `ppid`를 출력하거나 부모가 없다면 `N/A`를 출력
- `java 9` 예시
  ```java
  // ProcessHandle은 java 9이상에서 지원
  Optional<ProcessHandle> parentProcess = ph.parent();
  System.out.println("ppid : " + (parentProcess.isPresent()?
    String.valueOf(parentProcess.get().pid()) : "N/A"));
  ```
- `Optional`의 `map`을 사용한 해결
  ```java
  System.out.println("ppid : " +
    ph.parent().map(h -> String.valueOf(h.pid())).orElse("N/A"));
  ```

### 스트림 예시
- `Optional`들을 `Stream<Optional<T>>`로 받아,
  - 채워진 `Optional`에서 값을 뽑아, `Stream<T>`로 건네 처리
- `java 8` 구현 방식
  ```java
  streamOfOptionals
    .filter(Optional::isPresent)
    .map(Optional::get)
  ```
  - `Optional::isPresent`를 묶어줌으로써,
    - 해당 값이 있다면, `Optional::get` 스트림에 매핑
- `java 9` 구현 방식
  - `Optional`에 `stream()`메서드가 추가 됨
  - `Optional`을 `Stream`으로 변환해주는 어댑터
    - `Optional`에 값이 있다면, 그 값을 원소로 담은 스트림으로,
    - 값이 없다면, 빈 스트림으로 변환
  - `Stream`의 `flatMap` 메서드(ITEM.45)와 조합하면, 앞의 코드를 다음과 같이 변경 가능
    ```java
    stremaOfOptionals.flatMap(Optional::stream)
    ```

### 컨테이너를 Optional로 감싸지 말 것
- `Optional`을 반환값으로 사용한다고 하여, 무조건 득이되는 것은 아님
- 컬렉션, 스트림, 배열, 옵셔널 같은 **컨테이너 타입**은
  - `Optional`로 감싸면 안됨
- 빈 `Optional<List<T>>`를 반환하기 보다,
  - 빈 `List<T>`를 반환하는 것이 좋음(ITEM.54)
- **빈 컨테이너를 그대로 반환**하면
  - C에 **옵셔널 처리 코드**를 넣지 않아도 됨
- 참고로 `ProcessHandle.Info` 인터페이스의
  - `arguments` 메서드는
  - `Optional<String[]>`을 반환하는데,
    - 예외케이스니, **따라하지 말 것**

### 메서드 반환 타입을 `Optional<T>`로 해야하는 경우
- 기본 규칙
  - **결과**가 없을 수 있으며,
  - C가 이 상황을 **특별하게 처리**해야 한다면
    - `Optional<T>`를 반환
- 하지만 `Optional<T>`를 처리하는데 비용이 든다.
- `Optional<T>`도 **새로 초기화** 해야하는 객체
  - 그 안에서 값을 꺼내려면, **메서드**를 호출해야 함
- **성능**이 중요한 상황에서는
  - `Optional`이 맞지 않음
- 어떤 메서드가 이 상황에 처하는지 알아내려면, 측정할 수 밖에 없음(ITEM.67)

### Boxing Type과 Optional
- **Boxing**된 기본 타입을 담는 `Optional`은
  - 기본 타입 자체보다 **무겁다**
    - 값을 두번이나 **감싸기** 때문
- 이를 대비해
  - `OptionalInt`, `OptionalLong`, `OptionDouble`과 같은 클래스 존재
    - 각각 `int`, `long`, `double` 전용 `Optional` 클래스
- `Optional<T>`를 거의다 제공함
- **박싱된 기본 타입**을 담은
  - **옵셔널**을 반환하지 말 것
- 단, `Boolean`, `Byte`, `Character`, `Short`, `Float`은 예외

### `Optional`의 부적절한 쓰임 - 컬렉션의 원소
- 위에 언급된 상황 외, 대부분 적절치 않음
- `Optional`을 **맵의 값**으로 사용하면 안됨
- 그리 하게 될 경우,
  - **맵**안에 **키가 없음**을 나타내는 방법이 두 가지로 분기 됨
- 두가지 경우
  - 키 자체가 없는 경우
  - 키는 있으나, 그 키가 속이 빈 `Optional`
    - 쓸데없이 복잡성이 높아져, 혼란과 오류 가능성 증대
- `Optional`을
  - 컬렉션의 **키**, **값**, **원소**나 **배열의 원소**로 사용하는게 적절한 상황은 거의 없음

### `Optional`의 의문
- `Optional`을 인스턴스 필드로 저장해 두는게 필요할까?
- 위 상황 대부분은
  - **필수 필드**를 갖는 클래스와
  - 이를 확장해 **선택적 필드**를 추가한 **하위 클래스**를 따로 만들어야하는
    - **bed smell**
- 하지만, 적절한 상황도 있음
  - ITEM.2의 `NutritionFacts` 클래스
  - `NutritionFacts`의 인스턴스 필드 중, 상당수는 필수가 아님
  - 그 필드들의 경우,
    - **기본 타입**이라, 값이 **없음**을 나타내는 방법이 불명확
  - 이런 클래스의 경우
    - **선택적 필드**의 `getter` 메서드들이 `Optional`을 반환하면 좋은 방법
  - 위와 같은 상황일 때,
    - **필드 자체**를 `Optional`로 선언하는 것이 좋음

### 결론
- 값을 **반환하지 못할** 가능성이 있고,
  - 호출할때마다, **반환값**이 **없을** 가능성을 염두에 둬야 하는 메서드라면
  - `Optional`을 반환해야하는 상황일 수 있음
- 단, `Optional` 반환에는
  - **성능 저하**가 있다.
    - 성능에 민감한 메서드일 경우, `null`을 반환하거나 예외를 던질 것
- **`Optional`을 _반환 값_ 이외에 용도로 쓰는 경우는 매우 드물다**
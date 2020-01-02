## [ITEM.46] 스트림에서는 부작용 없는 함수
- 스트림의 특징
    - 하나의 API가 아닌 **함수형 프로그래밍**에 기초한 패러다임

### 스트림 패러다임의 핵심
- 계산을 일련의 변환(transformation)으로 **재구성**
- 각 변환 단계는 가능한 한,
    - 이전 단계의 **결과**를 받아 처리하는 **순수 함수**
- 순수 함수
    - 오직 **입력**만이 **결과**에 영향을 주는 함수
    - 다른 **가변 상태**를 참조하지 않음
    - 함수 자체로 **다른 상태**로 변경하지 않음
    - **중간** 단계든 **종단**단계든
        - 스트림 연산에 건네는 함수 객체는 **부작용**이 없어야 함

### 스트림을 잘못 사용한 예
- 코드
    ```java
    Map<String, Long> freq = new HashMap<>();
    try (Stream<String> words = new Sccanner(file).tokens()) {
        words.forEach(word -> {
            freq.merge(word.toLowerCase(), 1L, Long::sum);
        });
    }
    ```
- 스트림 패러다임을 이해 못하고, **API**만 사용한 예
- 텍스트 파일에서 단어별 수를 세어 빈도표로 만드는 역할
- **스트림**, **람다**, **메서드 참조**를 사용
- 하지만 **스트림 코드**가 아님
    - 스트림 API의 이점을 살리지 못하여 같은 기능의 반복적 코드보다 길고 읽기 어려움 / 유지보수 x
-  모든 연산이 **종단 연산** `forEach`에서 일어난다.
    - 외부 상태(빈도표)를 수정하는 람다를 실행하면서 문제가 생김
    - `Foreach`가 스트림이 수행한 연산 결과를 보여주는 일 이상을 한다.
        - 람다가 **상태를 수정**한다.
        
### 올바르게 작성한 `Stream` 코드
- 코드
    ```java
    Map<String, Long> freq;
    try (Stream<String> words = new Scanner(file.tokens()) {
            freq = words
                    .collect(groupingBy(String::toLowerCase, counting()));
    }
    ```
    - `Scanner`의 스트림 메서드인 `tokens`를 사용함
        - java9 부터 지원하는 문법
        - 이전 버전의 경우, `Iterator`를 구현한 Scanner`를 스트림으로 변환할 수 있음
        - `Iterable<E>`
- 스트림 API를 제대로 사용
- `ForEach`는 **종단 연산** 중,가장 스트림에 가깝지 않음
    - 대놓고 반복적이라, **병렬화**가 불가능
- `ForEach`는
    - 스트림 계산 결과를 보고 할 때만 사용
    - **계산**할 때는 사용하지 말 것

### Collector
- `java.util.stream.Collections`
- `Collector` 인터페이스를 생각하는 것이 아닌,
    - `reduction` 전략을 캡슐화한 블랙박스 객체
- reduction
    - 스트림의 원소들을 **객체 하나**에 취합하는 과정
- `Collector`가 생성하는 객체는 일반적으로 **컬렉션**
- 스트림의 원소를 쉽게 `Collection`으로 모을 수 있음

### Collector의 종류
- `toList()`, `toSet()`, `toCollection(collectionFactory)`
- 각각 리스트, 집합, 프로그래머가 지정한 컬렉션 타입 반환
- 예시 - 빈도표에서 가장 흔한단어 10개 뽑아내는 파이프라인
    ```java
    List<String> topTen = freq.keySet().stream()
        .sorted(comparing(freq::get).reversed())
        .limit(10)
        .collect(toList());
    ```
    - `toList`는 `Collectors`의 메서드
    - `Collectors`의 멤버를 **정적 임포트**하여 사용하면
        - **스트림 파이프라인**의 **가독성**이 좋아짐
- `comparing(freq::get).reversed()`
    - `sorted`에 넘긴 비교자
    - `comparing` 메서드
        - 키 추출 함수를 받는 비교자 생성 메서드(ITEM.14)
        - **한정적 메서드 참조**
    - `freq::get`
        - 입력받은 단어(키)를 빈도표에서 찾아 그 빈도를 반환
    - 가장 흔한 단어가 위로 오도록 `reversed`로 정렬함

### Collectors의 36개의 메서드
- 대부분은 **스트림**을 **맵**으로 취합하는 기능
    - 진짜 컬렉션에 취합하는 것보다 복잡함
- 스트림의 각 원소는 **키**와 **값**에 연관되어 있음
- 다수의 스트림 원소가 동일한 **키**에 연관될 수 있음
#### `toMap(keyMapper, valueMapper)`
- 인수
    - 스트림 원소를 **키**에 매핑하는 함수
    - 스트림 원소를 **값**에 매핑하는 함수
    - 열거 타입 상수의 **문자열 표현**을 **열거 타입 자체**에 매핑하는 `fromString`을 구현하는데 사용
- 코드
    ```java
    private static final Map<String, Operation> stringtoEnum = 
        Stream.of(values()).collect(
            toMap(Object::toString, e -> e));
    ```
- `toMap` 형태는 **스트림**의 각 원소가 **고유한 키**에 매핑되어 있을 때 적합
- 스트림 원소 다수가, 같은 키를 사용한다면
    - 파이프라인에서 `IllegalStateException`을 반환
- 더 복잡한 형태의 `toMap`이나 `groupingBy`에서는 충돌을 다루는 전략 제공
    - `toMap`에 **키 매퍼**와 **값 매퍼** 뿐만 아니라 `merge` 함수 제공
- 병합 함수의 형태는 `BinaryOperator<U>` 형태
    - `U`는 해당 맵의 **값 타입** 이다.
    - 같은 키를 공유하는 값들은
        - 해당 병합 함수를 사용하여 **기존 값**에 합쳐짐
    - 병합함수가 곱셈일 경우
        - 키가 같은 모든 값(**키/값 매퍼가 정한**)을 곱한 결과를 리턴
#### 인수 3개를 받는 `toMap`은
- 어떤 키
- 그 키와 연관된 원소중 하나를 골라
- 연관짓는 맵을 만들때 유용
- 예시
    - 음악가의 앨범을 담은 스트림을 가지고
        - 음악가와 그 음악가의 베스트 엘범을 연관짓는 것
    - 코드
        ```java
        Map<Artiest, Album> topHits = albums.collect(
            toMap(Album::artist, a -> a, maxBy(comparing(Album::sales))));
        ```
        - 비교자
            - `BinaryOperator`에서 정적 임포트 한, `maxBy`라는 정적 팩터리 메서드 사용
            - `maxBy`는 `Comparator<T>`를 받아 `BinaryOperator<T>`를 리턴
        - 비교자 생성 메서드인 `comparing`이, `maxBy`에 넘겨줄 **비교자** 반환
            - 자신의 키 추출 함수 `Album::sales`를 인수로 받음
        - **앨범 스트림**을 **맵**으로변경
            - **맵**은 **음악가**와 **음악가**의 베스트 앨범을 짝짓는다.
- 인수가 3개인 `toMap`은 충돌이 나면
    - 마지막 값을 취하는 `last-write-wins` 수집기를 만들 때 유용
    - 많은 스트림의 결과가 **비결정적**
    - 매핑 함수가 다음과 같은 상황에 필요
        - **키**하나에 연결해준 **값**들이 모두 같을 때,
        - 혹은 **값**이 다르더라도 모두 허용되는 값일때
    - 마지막 쓴 값을 취하는 수집기
        ```java
        toMap(keyMapper, valueMapper, (oldVal, newVal) -> newVal)
        ```
#### 마지막 `toMap` - 네번째 인수로 **맵 팩터리**를 받음
- 인수로 `EnumMap`이나 `TreeMap`처럼 원하는 특정 **맵 구현체**를 직접 지정
- 세가지 `toMap`은 변종이 존재
- `toCurrentMap`은 **병렬 실행**된 후 결과로 `ConcurrentHashMap` 인스턴스를 생성

### GroupingBy
- `Collectors`가 제공하는 또 다른 메서드
- 입력으로 **분류 함수**(classifier)를 받고
- 출력으로 원소들을 **카테고리**별로 모아놓은 **맵**을 담은 수집기 반환
- **카테고리**가 해당 원소의 **맵 키**로 쓰인다.

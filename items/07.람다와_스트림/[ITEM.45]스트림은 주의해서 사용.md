## [ITEM.45] 스트림은 주의해서 사용

### Stream 이란?
- 다량의 데이터 처리 작업(순차/병렬)을 위해 java8에 추가됨
- 핵심
  - 스트림은 데이터 원소의 **유한** 또는 **무한** sequence를 의미
  - 스트림 **파이프라인**은 이 원소들로 수행하는 **연산 단계**
- 스트림의 **원소**들은 어디서나 올 수 있다.
  - 컬렉션, 배열, 파일, 정규표현식 패턴 매처(matcher), 난수 생성기, 다른 스트림 등..
- 스트림 안의 **데이터 원소**들은
  - **객체 참조**나 **기본 타입**이다.
  - 기본 타입으로는 `int`, `long`, `double`을 지원

### 스트림 파이프 라인의 특징
- `src stream`으로 시작하여, 종단 연산(terminal operation)으로 끝남
  - 하나 이상의 중간 연산(intermediate operation)이 존재할 수 있다.
- 각 **중간 연산**은 스트림을 특정한 방식으로 **변환**(transform)
- 예시
  - 각 원소에 함수를 적용
  - 특정 조건을 만족 못하는 원소 걸러내기
- **중간 연산**들은
  - **스트림**을 **다른 스트림**으로 변환
  - 변환된 스트림의 **원소 타입** ? 변환 전 스트림의 **원소 타입**
    - 같거나, 다를 수 있음
- **종단 연산**은
  - 마지막 중간 연산이 내놓은 스트림에 **최후의 연산** 추가
  - 최후의 연산
    - 원소를 정렬해 컬렉션에 담기
    - 특정 원소 하나를 선택
    - 모든 원소를 출력

### lazy evaluation
- 지연 평가
- 평가는 **종단 연산**이 호출될 때 이루어진다.
- 종단 연산에서 쓰이지 않는 데이터 원소는
  - **계산에 쓰이지 않음**
- 지연 평가는 **무한 스트림**을 사용할 수 있게 해주는 개념
- **종단 연산**이 없는 스트림 파이프라인은
  - 아무 일도 하지 않는 명령어인 `no-op`와 같음
- **종단 연산**이 누락되지 않도록 확인해야 함

### Fluent API
- 메서드 연쇄를 지원함
- 파이프 라인 하나를 구성하는 **모든 호출을 연결**하여
  - 단 **하나의 표현식**으로 완성 가능
- 파이프라인 여러개를 연결해 표현식 하나로 만들 수 있다.

### 스트림 파이프라인의 순차적 실행
- 기본적으로 **스트림 파이프라인**은 순차 수행 됨
- 파이프라인 **병렬**실행 시,
  - 파이프 라인을 구성하는 스트림 중 하나에서 `parallel` 메서드 호출
  - 하지만, 효과를 보는 상황은 많지 않음(ITEM.48)

### 스트림 API의 사용처
- 잘 사용하면 프로그램이 짧고 깔끔해지나,
  - 잘못사용하면, 어렵고 유지보수가 힘듦
- 예시
  - 사전 파일에서 단어를 읽어, 지정한 제한치보다 원소수가 많은 anagram 그룹 출력
    - anagram : 철자를 구성하는 알파벳이 같고, 순서만 다른 단어
  - 각 사용자가 명시한 사전파일에서 각 단어를 읽어 맵에 저장
    - `key:value` = `anagram:anagram_group`
      - `aelpst`:`staple`, `petals`
      - 같은 키를 공유 함
  - 사전 하나를 모두 처리하고 나면, 각 집합은 사전에 올라간 모든 아나그램을 담은 형태
  - `values()`메서드로 마지막에 원소 수가 제한보다 큰 집합을 출력
  - 코드
    ```java
    public class Anagrams {
      public static void main(String[] args) throws IOException {
        File dictionary = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        Map<String, Set<String>> groups = new HashMap<>();
        try ( Scanner s = new Scanner(dictionary)) {
          while(s.hasNext()) {
            String word = s.next();
            // stream 사용처
            groups.computeIfAbsent(alphabetize(word),
              (unused) -> new TreeSet<>()),add(word);
          }
        }

        for (Set<String> group : groups.values())
          if (group.size() >= minGroupSize)
            System.out.println(group.size() + ": " + group);
      }

      private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
      }
    }
    ```
    - `ComputeIfAbsent`
      - 맵 안에 키가 있는지 찾은 뒤
        - 있다면 **키에 매핑된 값**을 반환
        - 없다면 건네진 **함수 객체를 키에 적용** 하여 **값**을 계산
          - 이후 키:값 매핑 및 값 반환
    - 각 키에 다수의 값을 매핑하는 맵을 쉽게 구현 가능

### 스트림을 과하게 사용한 예시
- 사전 파일을 여는 부분만 제외하면, 프로그램 전체가 하나의 표현식이 된다.
- 사전을 여는 작업을 분리한 이유는, `try-with-resources`를 사용해, 사전파일을 닫기 위함
- 예시
  ```java
  public class Anagrams {
    public class void main(String[] args) throws IOException {
      Path directory = Paths.get(args[0]);
      int minGroupSize = Integer.parseInt(args[1]);

      try (Stream<String> words = Files.lines(dictionary)) {
        words.collect(
          groupingBy(word -> word.chars().sorted()
                                  .collect(StringBuilder::new,
                                  (sb, c) -> sb.append((char) c),
                                  StringBuilder::append).toString())
        ).values().stream()
        .filter(group -> group.size() >= minGroupSize)
        .map(group -> group.size() + ": " + group)
        .forEach(System.out::println);
      }
    }
  }
  ```
  - 코드를 이해하기 어려움
- 스트림을 과하게 사용하면 프로그램을 읽거나, 유지보수가 어려워 짐

### 스트림을 적절히 사용한 예시
- 코드
  ```java
  public class Anagrams {
    public static void main(String[] args) throws IOException {
      Path dictionary = Paths.get(args[0]);
      int minGroupSize = Integer.parseInt(args[1]);

      try ( Stream<String> words = Files.lines(dictionary)) {
        words.collect(groupingBy(word -> alphabetize(word)))
              .values().stream()
              .filter(group -> group.size() >= minGroupSize)
              .forEach(g -> System.out.println(g.size() + ": " + g));
      }
    }

    // alphabetize는 동일
  }
  ```
  - `try-with-resources` 블록에서 사전 파일 열기
    - 파일의 모든 라인으로 구성된 스트림을 가져옴
  - 스트림 변수의 이름 `words`
  - 중간 연산은 없으며, **종단 연산**에서 모든 단어를 수집해 **맵**으로 모은다.
    - **맵**은 단어들을 아나그램으로 묶어놓은 것
  - `values()`가 반환한 값으로부터 새로운 `Stream<List<String>>`스트림 열기
  - 이 스트림의 원소는 아나그램 **리스트**
    - 리스트 중 원소가 `minGroupSize()` 보다 작으면 필터링 되어 무시된다.
  - 종단 연산인 `forEach`는 살아남은 리스트 출력

### 람다의 매개변수 이름
- 주의하여 정해야 함
- 람다에서는 **타입 이름**을 주로 **생략**하기 때문에
  - **매개변수 이름**을 잘 지어야 **스트림 파이프라인**의 가독성이 유지 됨
- 단어의 철자를 알파벳순으로 정렬하는 일은 `alphabetize`에서 수행
  - 연산에 적절한 이름을 지어주고, 세부 구현을 주 프로그램 밖으로 빼내어 가독성을 높임
- **도우미 메서드**를 적절히 활용하는 것
  - 반복 코드 보다, **스트림 파이프 라인**에서 중요
    - 파이프라인에서는 **타입 정보**가 명시되지 않거나, **임시 변수**를 자주 사용하기 때문

### alphabetize의 스트림 변환?
- `alphabetize` 메서드도 **스트림**을 사용해 다르게 구현 가능
  - 그렇게 되면 **명확성**이 떨어지고 **잘못 구현할 수 있음**
  - 속도가 느려질 수 있다.
    - java가 기본타입 **char**형 스트림을 지원하지 않기 때문
- 안좋은 `print` 예시
  ```java
  "Hello world!".chars().forEach(System.out.print);
  ```
  - 숫자값이 출력 됨
  - `Hello world!".chars()`가 반환하는 **스트림**의 원소는
    - `char`가 아닌 `int`값이기 때문
  - 이름이 `chars`인데, `int` 스트림을 반환하면 혼란스러울 수 있음
- 바른 `print` 예시
  ```java
  "Hello World!".chars().forEach(x -> System.out.print((char) x));
  ```
- 하지만 `char`를 처리할 때는 스트림을 쓰지 않는 것이 좋다.

### 반복문을 스트림으로 전환할때 유의점
- **스트림**으로 바꾸는게 가능 하더라도,
  - **가독성**과 **유지보수**가 떨어질 수 있음
- **스트림**과 **반복문**을 적절히 조합할 것
- 기존 코드는 **스트림**을 사용하도록 **리팩터링** 하되,
  - 새 코드가 나아 보일 때만 진행

### 반복 vs 스트림 - 반복이 더 유리한 경우
- **스트림 파이프라인**은 되풀이 하는 계산을
  - **함수 객체**(주로 람다나 메서드 참조)로 표현
- 반면 **반복 코드** 에서는
  - **코드 블록**으로 표현
- **함수 객체**로는 할 수 없지만, **코드 블록**으로 할 수 있는 일
  - **코드 블록**에서는 범위 안의 **지역 변수**를 읽고 수정할 수 있음
    - **람다**에서는 `final`이거나 사실상 `final`인 변수만 읽을 수 있으며
      - **지역변수**를 수정하는 것은 불가능
    - **코드 블록**에서는 `return`문을 사용해 메서드를 빠져 나가거나
      - `break`나 `continue`로 블록 바깥의 **반복문**을 종료하거나 반복을 스킵할 수 있음
      - 메서드 선언에 명시된 **검사 예외**를 동작시킬 수 있음
      - **람다**에서는 모두 불가능

### 스트림이 적합한 상황
- 원소들의 시퀀스를 **일관**되게 변환
- 원소들의 시퀀스를 **필터링**
- 원소들의 시퀀스를 **하나의 연산**을 사용해 결합
  - 더하기, 연결, 최솟값 등
- 원소들의 시퀀스를 **컬렉션**에 모음
  - 공통된 속성을 기준으로
- 원소들의 시퀀스에서 **특정 조건**을 만족하는 원소 찾기

### 스트림 사용이 어려운 상황
- 데이터가 **파이프라인**의 여러 단계(stage)를 통과 할때,
  - 각 데이터의 각 단계에서의 값들에 **동시에 접근하기는 어려운 상황**
- **스트림 파이프라인**은 한 값을 **다른 값**에 매핑 하고 나면
  - 원래의 값을 **잃어버리는 구조**
- **원래 값**과 **새로운 값**의 쌍을 저장하여 우회하는 방법은 있으나,
  - 완벽한 해법은 아님
- **매핑 객체**가 필요한 단계가 여러 곳이면, 더 큰 문제 발생
  - 코드가 복잡하고, 스트림을 쓰는 목적을 벗어남
- 가능하다면,
  - **앞 단계**의 값이 필요할 때, 매핑을 **거꾸로 수행**하는 방법

### 매핑 역연산 예시
- 예시
  - 처음 20개의 메르센 소수(Mersenne prime)를 출력하는 프로그램
  - 메르센 수 = `2^p-1`
  - `p`가 소수이면, 메르센 수도 소수 일 수 있음
    - 이를 메르센 소수라고 함
  - 파이프라인의 첫 스트림으로는 **모든 소수**를 사용
  - 무한 스트림을 반환하는 메서드
  - `BigInteger`의 정적 멤버들은 **정적 임포트**하여 사용한다고 가정
- 코드
  ```java
  static Stream<BigInteger> primes() {
    return Stream.iterate(TWO, BigInteger::nextProbablePrime);
  }
  ```
  - 메서드 이름 `prime`은 **스트림의 원소**가 소수임을 알려줌
  - 스트림을 반환하는 **메서드 이름**은
    - 원소의 정체를 알려주는 **복수 명사**로 쓰기를 권장
    - **스트림 파이프라인**의 가독성이 높아짐
  - `Stream.iterate`라는 **정적 팩터리**는 매개변수를 2개 받음
    - 스트림의 첫 번째 원소
    - 스트림에서 다음 원소를 생성해주는 함수
- 예시 - 처음 20개의 메르센 소수를 출력
  ```java
  public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
            .filter(mersenne -> mersenne.isProbablePrime(50))
            .limit(20)
            .forEach(System.out::println);
  }
  ```
  - 소수들을 사용하여 메르센 수를 계산
  - 결과값이 소수인 경우만 남김
    - 매직넘버 `50`은, 소수성 검사가 `true`를 반환할 확률 제어
  - 결과 스트림의 원소 수를 20으로 제한하여, 작업이 끝나면 리턴
- 메르센 소수 앞에 지수(`p`)를 출력해보기
  - 이 값은 **초기 스트림**에만 나타내므로,
  - 결과를 출력하는 **종단 연산**에서는 접근 불가능
  - 하지만, 첫 중간 연산에서 **매핑 역수행**을 하여 값을 구할 수 있다.
    - 지수 : 단순히 숫자를 **이진수**로 표현한 다음 몇 비트인지 세면 된다.
  - 코드 - 종단 연산을 바꾸어 지수를 구하는 코드
    ```java
    .forEach(mp -> System.out.println(mp.bitLength() + ": "+ mp));
    ```

### 스트림 vs 반복 결정이 어려운 경우
- 카드 덱을 초기화 하는 작업
  - 카드는 숫자(`rank`)와 무늬(`suit`)를 묶은 **불변 값 클래스**
  - 숫자와 무늬는 모두 **열거 타입**
  - 두 집합의 원소들로 만들수 있는 가능한 **모든 조합**을 계산하는 문제
    - **집합의 데카르트 곱**
- `forEach` 반복문을 중첩해 구현한 코드
  ```java
  private static List<Card> newDeck() {
    List<Card> result = new ArrayList<>();
    for (Suit suit : Suit.values())
      for( Rank rank : Rank.values())
        result.add(new Card(suit, rank));
    return result;
  }
  ```
- 스트림으로 구현한 코드
  ```java
  private static List<Card> newDeck() {
    return Stream.of(Suit.values())
              .flatMap(suit -> 
                Stream.of(Rank.values())
                .map(rank -> new Card(suit, rank))) // 중첩된 람다 사용
              .collect(toList());
  }
  ```
  - 중간 연산으로 사용한 `flatMap`은
  - 스트림의 원소 각각을 **스트림**으로 매핑한 다음
    - 스트림들을 다시 **하나의 스트림**으로 합친다.
      - `flattening`
    - 중첩 람다를 사용하여 구현
- 위 두가지 방법 중 **취향**에 맞게 선택
- 확신이 서지 않을 경우 **첫번째 방식**이 더 안전

### 결론
- **스트림**을 사용하여 좋게 처리하는 방법이 있지만,
  - **반복 방식**이 더 맞는 경우도 있음
- 수 많은 케이스에서 이 둘을 조합하여 사용
- **스트림**과 **반복** 중 어느쪽이 나은지 확신하기 어려울 경우
  - 둘 다 해보고 나은 쪽을 판별할 것
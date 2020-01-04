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
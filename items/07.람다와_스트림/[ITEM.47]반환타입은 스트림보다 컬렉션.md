## [ITEM.47] 반환 타입은 스트림보다 컬렉션

### 원소 시퀀스
- 일련의 원소를 반환하는 메서드는 많음
- java7까지는 이 메서드의 반환 타입으로
  - `Collection`, `Set`, `List`와 같은 **컬렉션 인터페이스**,
  - 또는 `Iterable`이나 **배열**을 사용함
- 반환 타입 선택하기
  - 기본 타입은 **컬렉션 인터페이스**
  - `Iterable` 인터페이스
    - `for-each`문에서만 쓰임
    - 반환된 원소 시퀀스가 `Collection` 메서드를 구현할 수 없을 때
      - `contains(Object)` 처럼
- java8이 되면서 **반환 타입 선택**이 복잡해짐

### 원소 시퀀스 반환 = 스트림?
- **ITEM.45**에서 처럼, 스트림은 **반복**(iteration)을 지원하지 않음
- **스트림**과 **밚복**을 알맞게 조합해야 한다.
- **API**가 스트림만을 반환할 경우, 
  - 반환 스트림을 `for-each`로 사용하기 어려움
- `Stream` 인터페이스는 
  - `Iterable` 인터페이스가 정의한 **추상 메서드** 포함
  - `Iterable` 인터페이스가 정의한 방식대로 동작
- 그래도 `for-each`로 **스트림**을 반복 못하는 이유는
  - `Stream`이 `Iterable`을 `extend`하지 않기 때문

### 좋지 않은 우회 방법
- 좋은 우회 방법은 없음
  - `Stream`의 `iterator` 메서드에 **메서드 참조**를 주면 될 수도 있으나, 지저분함
- 컴파일 되지 않는 코드
  ```java
  for (ProcessHandle ph : ProcessHandle.allProcesses()::iterator) {
      // 프로세스 처리
  } 
  ```
- 스트림을 반복하기 위한 안좋은 우회 방법
   ```java
   for (ProcessHandle ph : (Iterable<ProcessHandle>) ProcessHandle.allProcesses()::iterator) {
       // 프로세스 처리
   }
   ```
   - 작동은 하나,
     - **난잡**, **직관성이 떨어짐**
- 어댑터 메서드를 사용한 코드
  ```java
  public static <E> Iterable<E> iterableOf(Stream<E> stream) {
      return stream::iterator;
  }
  ```
  ```java
  for (ProcessHandle p : iterableOf(ProcessHandle.allProcesses())) {
      // 프로세스를 처리한다.
  }
  ```
  - 자바의 **타입 추론**이 문맥을 파악하여
    - **어댑터 메서드**안에 따로 **형번환이 필요 없음**


### API가 Iterable만 반환할경우
- 스트림 파이프라인에서 처리가 어려움
- 우회 방법
  ```java
  // Iterable<E>를 Stream<E>로 중개하는 어댑터
  public static <E> Stream<E> streamOf(Iterable<E> iterable) {
      return StreamSupport.stream(iterable.spliterator(), false);
  }
  ```

### 객체 반환 메서드 작성 가이드?
- **객체 시퀀스**를 반환하는 메서드를 작성하는데,
  - 이 메서드가 오직 **스트림 파이프라인**에서만 쓰일 것이라면, 단순 **스트림**을 반환
  - 이 메서드가 오직 **반복문**에서만 쓰일 경우, `Iterable` 반환
- 하지만 공개용 API에서는 두 측을 다 고려해야 함

### Collection Interface
- `Collection` 인터페이스는
  - `Iterable`의 하위 타입
  - `stream` 메서드도 제공
- **반복**과 **스트림**을 동시에 지원
- **원소 시퀀스**를 반환하는 **공개 API 반환 타입**에는
  - `Collection`이나 그 **하위 타입**을 쓰는게 일반적으로 최선
- `Arrays` 역시
  - `Arrays.asList`나, `Stream.of` 메서드로 **반복**과 **스트림** 지원
- 반환하는 시퀀스의 크기가 **메모리에 올릴 정도로 작다면**
  - `ArrayList`나 `HashSet` 같은 **표준 컬렉션 구현체** 반환
- **컬렉션**을 반환한다는 이유로
  - **덩지 큰 시퀀스**를 **메모리에 올리면 X

### 반환할 시퀀스가 크지만, 표현을 간결하게 할 수 있을 때
- 전용 컬렉션을 구현하기
- 예시
  - 주어진 집합의 **멱집합**구하기
    - 한 집합의 모든 부분 집합을 원소로 하는 집합
  - 원소 개수가 `n`일 경우, 멱집합의 원소 개수는 `2^n`
  - 멱집합을 **표준 컬렉션 구현체**에 구현하는 것은 위험하나,
    - `AbstractList`를 이용하면 전용 컬렉션 구현이 가능
  - 멱집합을 구성하는 **각 원소의 인덱스**를 **비트 벡터**(bit vector)로 사용
  - 인덱스의 `n`번째 비트 값은,
    - 멱집합의 해당 원소가 원 집합의 `n`번째 원소를 포함하는지 여부 파악
  - `0`부터 `2^n-1`까지의 이진수와,
    - 원소 `n`개인 멱집합과 자연스럽게 매핑
  - 코드
    ```java
    public class PowerSet {
        public static final <E> Collection<Set<E>> of(Set<E> s) {
            List<E> src = new ArrayList<>(s);
            if (src.size() > 30)
                throw new IllegalArgumentException("집합의 원소가 많습니다");
            
            return new AbstractList<Set<E>> {
                @Override public int size() {
                    // 멱집합의 크기 = 2^원소 수
                    return 1 << src.size();
                }
                @Override public boolean contains(Object o) {
                    return o instanceof Set && src.containsAll((Set) o);
                }
                @Override public Set<E> get(int index) {
                    Set<E> result = new HashSet<>();
                    for (int i=0; index !=0; i++, index >>=1)
                        if((index & 1) == 1)
                            result.add(src.get(i));
                    return result;
                }
            }
        }
    }
    ```
    - 원소 수가 `30`이 넘을 경우 `PowerSet.of`가 예외를 발생
      - `Stream`이나 `Iterable`이 아닌 `Collection`을 반환 타입으로 쓸 때의 단점이 보임
      - `Collection`의 `size` 메서드가 `int` 값을 반환하므로
        - `PowerSet.of`가 반환되는 시퀀스의 최대 길이는
          - `Integer.MAX_VALUE` 또는 `2^31-1`로 제한
      - `Collection` 명세에 따르면
        - 컬렉션이 더 크거나, 무한대일 때
        - `size`가 `2^31-1`을 반환해도 되나, 만족스러운 해법은 아님
    - `AbstractCollection`을 활용해서 `Collection` 구현체를 작성할 때는
      - `Iterable`용 메서드 외에 2개를 더 구현해야 함
        - `contains`, `size`
        - 두 메서드는 쉽게 구현 가능
        - 구현이 불가능 할 경우
          - 반복이 시작되기 전에는 시퀀스 내용 확정 불가 이슈 등
          - **컬렉션**보다는 **스트림**이나 `Iterable`을 반환하는 것이 좋음
        - 원한다면 별도의 메서드를 두어 두 방식을 모두 제공해도 됨

### 구현이 쉬운 쪽을 선택하기
- 때로는 구현 쉬운 쪽을 선택
- 예시
  - **입력 리스트**의 연속적인 **부분 리스트**를 모두 반환하는 메서드 작성
  - 필요한 **부분 리스트**를 만들어 **표준 컬렉션**에 담는 코드는 간단하게 작성되나,
    - 이 **컬렉션**은 **입력 리스트 크기**의 **거듭 제곱**만큼 메모리를 차지함
  - **멱집합**처럼 전용 컬렉션 구현 x
    - 자바는 여기에 적합한 `Iterable`도 지원하지 않음
  - 하지만 **입력 리스트**의 모든 **부분 리스트**를 **스트림**으로 구현은 쉽게 가능
    - 첫 번쨰 원소를 포함하는 부분 리스트를 **리스트의 prefix**
      - `(a, b, c)` prefix => `(a), (a,b), (a,b,c)`
    - 마지막 원소 포함하는 부분리스트를 **리스트의 suffix**
    - 리스트의 부분 리스트는 단순히 리스트의 `prefix | suffix`에 **빈 리스트** 하나만 추가하면 됨
    - 코드
      ```java
      public class SubLists {
          public static <E> Stream<List<E>> of(List<E> list) {
              return Stream.concat(
                  Stream.of(Collections.emptyList()), 
                            prefixes(list).flatMap(SubLists::suffixes));
          }

          public static <E> Stream<List<E>> prefixes(List<E> list) {
              return IntStream.rangeClosed(1, list.size()).maptoObj(end -> list.subList(0, end));
          }

          public static <E> Stream<List<E>> suffixes(List<E> list) {
              return IntStream.range(0, list.size()).mapToObj(start -> list.subList(start, list.size()));
          }
      }
      ``` 
      - `Stream.concat` 메서드
        - 반환되는 스트림에 **빈 리스트**를 추가하며,
      - `flatMap` 메서드(ITEM.45)
        - 모든 prefix의 모든 suffix로 구성된 하나의 스트림을 생성
      - `prefix`와 `suffix`의 스트림은
        - `IntStream.range`와 `IntStream.rangeClosed`가 반환하는 연속된 **정수 값**을 매핑하여 생성
      - 이 **관용구**는
        - **정수 인덱스**를 사용한 표준 `for` 반복문의 **스트림 버전**
      - `for` 반복문을 중첩한 것과 유사
        ```java
        for (int start = 0; start < src.size(); start++)
            for( int end = start + 1; end <= src.size(); end++)
                System.out.println(src.subList(start, end));
        ```
        - 위 반복문은 그대로 스트림으로 변환이 가능하나, **가독성**이 좋지 않음
      - `for`반복문의 스트림 변환
        ```java
        public static <E> Stream<List<E>> of(List<E> list) {
            return IntStream.range(0, list.size())
                .mapToObj(start -> 
                    IntStream.rangeClosed(start + 1, list.size())
                        .mapToObj(end -> list.subList(start, end)))
                .flatMap(x -> x);
                        
        }
        ```
        - 빈 리스트는 변환하지 않음
          - 고치려면, `concat`을 사용하거나 
          - `rangeClosed` 호출 코드의 `1`을 `Math.signum(start)`로 고치면 됨

### 스트림을 반환하는 두가지 구현의 생각
- 모두 사용은 가능하나
  - 반복을 사용하는게 더 자연스러운 상황이어도,
  - 사용자는 그냥 **스트림**을 쓰거나
    - `Stream`을 `Iterable`로 변환해주는 **어댑터**를 이용해야 한다.
- 이러한 **어댑터**는 클라이언트 코드를 복잡하게 하며, **2.3**배 느리다


### 결론
- **원소 시퀀스**를 반환하는 메서드 작성시
  - **스트림**과 **반복** 처리 방식 둘 다 고려
- 반환 전부터 이미 원소들을 **컬렉션**에 담아 관리하거나,
  - **컬렉션**을 하나 더 만들 수 있을 정도로, **원소 수가 적다면**
    - `ArrayList`와 같은 **표준 컬렉션**에 반환
- 그렇지 않을 경우, **멱집합** 예시처럼, **전용 컬렉션** 구성 여부 판단
- **컬렉션**반환이 **불가능**할 경우,
  - **스트림**과 `Iterable` 중 더 자연스러운 것을 반환
- 나중에 `Stream` 인터페이스가 `Iterable`을 지원하도록 자바가 수정된다면
  - 이때는 `Stream`을 반환하면 됨
    - **스트림** 처리와 **반복**에 모두 사용가능하기 때문
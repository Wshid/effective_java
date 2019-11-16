## [ITEM.37] ordinal 인덱싱 대신 EnumSet

### ordinal 메서드
- 배열이나, 리스트에서 원소를 꺼낼때 사용하는 경우
- 이 때 `oridnal`(ITEM.35)로 인덱스를 얻는 코드
  ```java
  class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL } // 식물의 생애주기별 묶기

    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
      this.name = name;
      this.lifeCycle = lifeCycle;
    }

    @Override public String toString() {
      return name;
    }
  }
  ```
  - 식물들을 배열 하나로 관리
  - 생애주기별 묶기
  - 3개의 집합을 만들어 식물을 해당 집합에 넣기
  - `ordinal`을 배열 인덱스로 사용한 코드 - 비추천
    ```java
    Set<Plant>[] plantsByLifeCycle =
      (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
    for(int i = 0; i < platnsByLifeCycle.length; i++)
      plantsByLifeCycle[i] = new HashSet<>();
    
    for(Plant p : garden)
      plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);

    for(int i=0; i < plantsByLifeCycle.length; i++) {
      System.out.println("%s: %s%n", Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
    }
    ```
    - 동작은 되나.. 문제가 많음
    - 배열은 **제네릭**과 호환되지 않음(ITEM.28)
      - 비검사 형변환을 수행해야 함
      - 깔끔한 컴파일이 어려움
    - 배열은 각 **인덱스**의 의미를 모르기 때문에
      - 출력 결과에 **직접 레이블**을 기록해야 함
    - **정확한 정숫값**을 사용한다는 것을, 보증해야 함
      - **정수**는 **열거 타입**과 달리, **타입 안전**하지 않음
      - 잘못된 값을 사용시
        - 잘못된 동작을 수행하거나
        - `ArrayIndexOutOfBoundsException`이 발생함

### 해결방법 - EnumMap
- 위 예시에서 배열은
  - 실질적으로 **열거 타입 상수**를 **값**으로 매핑하는 역할
- `Map`으로 바꿔 사용할 수 있음
- 열거 타입을 키로 사용하도록 설계한 빠른 `Map 구현체`
- `EnumMap`을 사용한 코드
  - `EnumMap`을 사용하여 **데이터**와 **열거 타입**을 매핑한다.
  ```java
  Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle = 
    new EnumMap<>(Plant.LifeCycle.class);
  for (Plant.LifeCycle lc : Plant.LifeCycle.values())
    plantsByLifeCycle.put(lc, new HashSet<>());
  for (Plant p : garden)
    platnsByLifeCycle.get(p.lifeCycle).add(p);
  System.out.println(plantsByLifeCycle);
  ```
  - 짧고 안전, 성능도 원 버전과 동일함
  - 안전하지 않은 **형변환**을 사용하지 않음
  - 맵의 **키**인 **열거 타입**이 그 자체로 출력용 문자열 제공
    - 출력 결과에 직접 레이블을 달 필요가 없음
  - **배열 인덱스**를 계산하는 과정에서, 오류가 날 가능성이 줄어든다.

### EnumMap의 성능
- `ordinal`을 쓴 배열과 비교된다.
  - 그 내부에서 배열을 사용하기 때문
- 내부 구현 방식을 안에서 숨겨서
  - `Map`의 **타입 안전성**과 **배열의 성능**을 가져옴
- `EnumMap`의 생성자가 받는 **키 타입**의 `Class` 객체는
  - **한정적 타입 토큰**
  - `Runtime` 제네릭 타입 정보를 제공(ITEM.33)
- **스트림**(ITEM.45)를 사용해 맵을 관리하면 코드를 줄일 수 있음

### 스트림 사용
- 사용 코드 1
  ```java
  System.out.println(Arrays.stream(garden).collect(groupingBy(p -> p.lifeCycle)));
  ```
  - `EnumMap`을 사용하지 않음
  - `EnumMap`이 아닌, 고유 맵 구현체를 사용했기 때문에
    - `EnumMap`을 사용하기에 얻은 **공간**과 **성능 이점**이 사라짐
- 사용 코드 2
  ```java
  System.out.println(Arrays.stream(garden)
    .collect(groupingBy(
        p -> p.lifeCycle,
        () -> new EnumMap<>(LifeCycle.class), toSet())));
  ```
  - 매개 변수 3개짜리 `Collectors.groupingBy` 메서드는
    - `mapFactory` 매개변수에 원하는 **맵 구현체**를 명시해 호출 가능
  - 단순한 코드에서는 필요 없으나,
    - `Map`을 빈번히 사용하는 프로그램에서는 필요

### 스트림과 EnumMap을 같이 사용했을 때
- **스트림**을 사용하면 `EnumMap`만 사용했을 때와는 다르게 동작
- `EnumMap` 버전은,
  - 언제나 `Plant`의 생애 주기당,
  - 하나씩 중첩 맵을 생성
- **스트림** 버전은
  - 해당 생애주기에 속하는 `Plant`가 있을 때만 생성
- `lazy evaluation`과 동일한 개념 아닐지..

### ordinal을 두번 사용하는 경우
- 두 열거 타입의 값을 매핑하여
  - `ordinal`을 **두 번이나** 쓴 배열들의 배열
- 두 가지 상태(Phase)를 전이(Transition)와 매핑하도록 함
- 배열들의 배열의 인덱스에 `ordinal()`을 사용하는 코드
  ```java
  public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
      MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;

      // Row : from의 ordinal,
      // Column : to의 ordinal
      private static final Transition[][] TRANSITIONS = {
        {null, MELT, SUBLIME},
        {FREEZE, null, BOIL},
        {DEPOSIT, CONDENSE, null}
      };

      public static Transition from(Phase from, Phase to) {
        return TRANSITIONS[from.ordinal()][to.ordinal()]
      }
    }
  }
  ```
  - `compiler`가 `ordinal`과 **배열 인덱스**의 관계를 알 방법이 없음
  - `Phase`나 `Phase.Transition` 열거 타입을 수정하면서
    - `TRANSITIONS`를 함께 수정하지 않거나, 잘못 수정할 경우
    - `Runtime ERROR`
      - `ArrayIndexOutOfBoundsException`
      - `NullPointerException`
      - 또는 비정상 동작할 것
    - 상태의 가짓수가 늘어나면 늘어날 수록
      - 제곱으로 커지며,
      - `null`값도 많아질 것
  - `EnumMap`을 사용하는 것이 좋음
    - `Map`을 단순히 두개 중첩하면 된다.

### EnumMap을 사용한 해결
- 전이 이후의 두 상태를 열거 타입 `Transition`의 입력으로 받아
  - 이 `Transition` 상수들로 중첩된 `EnumMap`을 추가하면 됨
- 예시 코드
  ```java
  public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
      MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
      BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
      SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

      private final Phase from;
      private final Phase to;

      Transition(Phase from, Phase to) {
        this.from = from;
        this.to = to;
      }

      // 맵 초기화
      private static final Map<Phase, Map<Phase, Transition>>
        m = Stream.of(values()).collect(groupingBy(
          t -> t.from, () -> new EnumMap<>(Phase.class),
          toMap(
            t->t.to,
            t->t,
            (x,y) -> y,
            () -> new EnumMap<>(Phase.class))
        ));

        public static Transition from(Phase from, Phase to) {
          return m.get(from).get(to);
        }
    }
  }
  ```
  - `from`과 `to`의 각 상태가 필요하므로, 맵 2개 중첩
  - 안쪽 맵의 경우 **이전 상태 - 전이** 매핑
  - 바깥 맵은 **이후 상태 - 전이** 매핑
  - `Map<Phase, Map<Phase, Transition>>`
    - **이전 상태**(from)에서 **이후 상태**(to)에서 전이로의 맵
    - 이 맵을 초기화 하기 위해 `java.util.stream.Collector(수집기)` 2개를 차례로 사용
  - 첫번째 수집기(`groupingBy`)
    - 전이를 **이전 상태** 기준으로 묶음
  - 두번째 수집기(`toMap`)
    - **이후 상태**를 전이에 대응시키는 `EnumMap`을 생성
    - 병합 함수인 `(x,y) -> y`는
      - 선언만 하고 실제로 사용되지는 않음
      - 사용 이유
        - `EnumMap`을 얻으려면 `MapFactory`가 필요하고,
        - 수집기들은 **점층적 팩터리**(`telescoping factory`)를 제공하기 때문

### 새로운 상태가 추가 되었을 때
- 위 예시에서 새로운 상태 `PLASMA`를 추가할 때
- 이 상태와 연결된 전이는 2개
  - 기체 -> 플라즈마 (IONIZE)
  - 플라즈마 -> 기체 (DEIONIZE)
- 배열로 만든 코드를 수정하려면
  - 새로운 상수를 `Phase`에 1개, `Phase.Transition`에 2개 추가
- 원소 **9개**짜리인 배열들의 배열을
  - **16**개짜리로 교체해야 함
- 원소를 너무 적거나 많이 기입하거나
  - 잘못된 순서로 기입하면
  - `Runtime`에 에러발생 가능성 존재
- `EnumMap` 버전의 코드
  - 상태 목록에 `PLASMA` 추가
  - 전이 목록에 `IONSIZE, DEIONIZE`만 추가하면 된다.
  ```java
  public enum Phase {
    SOLID, LIQUID, GAS, PLASMA;

    public enum Transition {
      MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID), ...
      IONIZE(GAS, PLASMA), DEIONIZE(PLASMA, GAS);

      ...
    }
  }
  ```
  - 나머지는 동일한 코드, 기존 로직에서 잘 처리 된다.
  - 실제 내부에서는
    - **맵**들의 **맵 배열**들의 **배열**로 구현되기 때문에
      - 낭비되는 **공간**, **시간**이 거의 없음

### 코드에서 주의할 점
- 코드를 간략히 하기 위해, 앞 코드에서 **전이**가 없을 때 `null`을 사용함
  - `Runtime`에 `NullPointerException`을 일으키는 안좋은 습관
  - 예외 처리가 필요함

### 결론
- 배열의 **인덱스**를 얻기 위해 `ordinal`을 사용하는 것은 추천하지 않음
  - 대신 `EnumMap`을 사용할 것
- 다 차원 관계는 `EnumMap<..., EnumMap<...>>`으로 사용할 것
- App programer는 `Enum.ordinal`을 사용하지 말아야 한다.


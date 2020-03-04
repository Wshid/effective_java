## [ITEM.58] for보다 for-each문

### 스트림 vs 반복, for문 예시
- 때에 따라 스트림에 적합한 작업이 있으며,
  - 반복에 적합한 작업이 있음
- `for`문 예시 - 컬렉션 순회
  ```java
  for (Iterator<Element> i = c.iterator(); i.hasNext();) {
    Element e = i.next();
    ... // e로 무언가를 함
  } 
  ```
- `for`문 예시 - 배열 순회
  ```java
  for(int = 0; i < a.length; i++) {
    ... // a[i]로 무언가를 함
  }
  ```

### for문이 그다지 좋은 방법이 아닌 이유
- 위 관용구들은 `while`보다는 나으나, 좋은 방법은 아님
- **반복자**와 **인덱스 변수**는 모두 코드를 지저분하게 함
  - 정말 필요한건 **원소**이기 때문
- 쓰이는 **요소 종류**가 늘어나면, 오류가 생길 가능성이 높아짐
  - 1회 반복에서, 반복자가 세번 등장
  - 인덱스는 4번이나 등장하기 때문에
    - **변수**를 잘못 사용할 틈새가 많아짐
  - 잘못되 변수를 사용하였을 때, **컴파일러**가 확인한다는 보장이 없음
- **컬렉션**이나 **배열**이냐에 따라 코드 형태가 달라지므로 유의

### for-each를 통한 해결
- for-each의 정식 명칭
  - **enchanced for statement**
- **반복자**와 **인덱스**를 사용하지 않기 때문에
  - 코드가 정리되고, 오류가 나지 않음
- 하나의 **관용구**로
  - **컬렉션** 및 **배열** 처리 가능
  - 컨테이너에 독립적

### `for-each` 코드
- 코드
  ```java
  for ( Element e : elements ) {
    ... // e로 무언가를 작업
  }
  ```
- `:`은 `in`으로 읽으면 됨
- 위 반복문은
  - "`elements` 안의 각 원소 `e`에 대해"로 해석
- 반복 대상이 **컬렉션**이든 **배열**이든
  - `for-each`문을 사용해도 속도는 동일
    - 사람이 손으로 최적화한 것과 사실상 동일한 코드

### for문 사용시의 버그가 존재하는 코드
- 버그가 있는 코드
  ```java
  enum Suit { CLUB, DIAMOND, HEART, SPADE }
  enum Rank { ACE, DEUCE, THREE, .. }
  ...

  static Collection<Suit> suits = Arrays.asList(Suit.values());
  static Collection<Rank> ranks = Arrays.asList(Rank.values());

  List<Card> deck = new ArrayList<>();
  for (Iterator<Suit> i = suits.iterator(); i.hasNext();)
    for ( Iterator<Rank> j = ranks.iterator(); j.hasNext();)
      deck.add(new Card(i.next(), j.next()));
  ```
- 바깥 컬렉션 `suits`의 반복자에서
  - `next` 메서드가 너무 많이 호출 됨
- `i.next()`를 보면
  - `next`는 `suit` 하나당 한번씩 불려야 하지만, `rank`당 하나씩 불리는 수준
  - 숫자가 바닥날 경우 `NoSuchElementException`이 발생함
- 바깥 컬렉션의 크기가 안쪽 컬렉션의 **배수**일 경우
  - 반복문은 **예외**없이 종료 함
  - 정상적인 결과가 아님
- 동일한 버그가 있는 코드 - 예외는 발생하지 않음
  ```java
  enum Face { ONE, TWO, THREE, FOUR, FIVE, SIX }
  ...
  Collection<Face> faces = EnumSet.allOf(Face.class);

  for(Iterator<Face> i = faces.iterator(); i.hasNext();)
    for(Iterator<Face> j = faces.iterator(); j.hasNext();)
      deck.add(new Card(suit, j.next()));
  ```
  - 에러가 발생하지 않으나, `ONE, ONE` ~ `SIX, SIX`의 여섯쌍만 출력함

### 문제를 해결하기
- for를 이용하여 해결한 코드
  ```java
  for(Iterator<Suit> i = suits.iterator(); i.hasNext();) {
    Suit suit = i.next();
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext();)
      deck.add(new Card(suit, j.next()));
  }
  ```
  - 중간에 저장하여 해결
- `for-each`를 사용한 해결 - 간결
  ```java
  for (Suit suit : suits)
    for (Rank rank : ranks)
      deck.add(new Card(suit, rank));
  ```

### `for-each`를 사용할 수 없는 경우(3)
- 아래 3가지 문제일 경우 `for`문을 사용해야 하며,
  - 언급한 문제에 유의해야 함
#### 파괴적인 필터링(destructive filtering)
- **컬렉션**을 순회하면서
  - 선택한 원소를 **제거**하는 상황일 때
  - **반복자**의 `remove` 메서드를 호출해야 함
- `java8:`, `Collection`의 `removeIf` 메서드를 사용해
  - 컬렉션을 명시적으로 순회하는 일을 피할 수 있음
#### 변형(transforming)
- **리스트**나 **배열**을 순회하면서
  - 그 원소의 **값** 일부 혹은 전체를 **교체**해야 할 때
  - 해당 배열의 **인덱스**를 사용해야 함
#### 병렬 반복(parallel iteration)
- 여러 **컬렉션**을 **병렬**로 순회해야 한다면
  - 각각의 **반복자**와 **인덱스 변수**를 사용하여
  - 명시적으로 **제어**해야 함

### `for-each`와 `Iterable`
- **컬렉션**과 **배열**뿐만 아니라
  - `Iterable` 인터페이스를 구현한 객체면 모두 순회 가능
- `Iterable` 인터페이스는
  - **메서드**가 한가지만 존재
    ```java
    public interface Iterable<E> {
      // 이 객체의 원소를 순회하는 반복자 반환
      Iterator<E> iterator();
    }
    ```
- `Iterable`을 처음부터 구현하기는 까다롭지만
  - **원소들의 묶음**을 표현하는 타입을 작성해야 할때
  - `Iterable`을 구현하는쪽으로 고민 필요
    - `Collection` 인터페이스는 구현하지 않기로 하였을 때도 마찬가지
- `Iterable`을 구현해두면
  - 그 타입을 사용하는 C가 `for-each` 사용할 수 있도록 용이해짐

### 정리
- `for`문과 비교했을때, `for-each`문은
  - 유연, 명료, 버그 예방
  - 성능 저하도 없음
- 가능한 모든 곳에서, `for-each`문을 사용할 것
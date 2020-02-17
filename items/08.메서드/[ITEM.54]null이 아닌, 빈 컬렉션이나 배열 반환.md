## [ITEM.54] null이 아닌 빈 컬렉션이나 배열 반환
### 예시
- 코드
  ```java
  private final List<Cheese> cheeseInStock = ...;

  /**
   * 치즈 목록 반환
   * 단, 치즈가 없을 경우 null 반환
   * */
  public List<Cheese> getCheeses() {
    return cheesesInStock.isEmtpy() ? null
      : new ArrayList<>(cheeseInStock);
  }
  ```
  - 좋은 예가 아님

### null을 반환하면 안되는 이유
- C가 추가적으로 `null`을 처리하는 코드를 넣어주어야 함
  ```java
  List<Cheese> cheeses = shop.getCheeses();
  if(cheeses != null && cheeses.contains(Cheese.STILTON))
    System.out.println("cheese~!");
  ```
- **컬렉션**이나 **배열**같은 컨테이너가 비었을 때,
  - `null`을 반환하는 메서드 사용시,
  - 위와 같은 **방어 코드**가 무조건 필요
- 객체가 0개일 가능성이 거의 없는 상황해서
  - 매우 늦게 오류 발생을 인지하기도 함
- `null`을 반환하게 되면
  - 반환하는 쪽에서도, 이 상황을 특별히 취급해야하기 때문에
  - 코드가 복잡해짐

### null 반환이 낫지 않나?
- 때로는 **빈 컨테이너**를 할당하는데도, 비용이 들기 때문에
  - `null`반환이 낫다는 주장이 있음
- 하지만 두가지 문제점이 존재
  - 성능 분석 결과, 이 할당이 **성능 저하**의 주범이라고 확인되지 않는 한,(ITEM.67)
    - **이 정도의 성능 차이는 미미함**
  - **빈 컬렉션**과 **배열**은
    - 굳이 **할당 없이도** 반환 가능

### 대부분의 상황에서 처리 방법
- 코드
  ```java
  public List<Cheese> getCheeses() {
    return new ArrayList<>(cheesesInStock);
  }
  ```
- 가능성은 적으나,
  - **사용 패턴**에 따라
  - **빈 컬렉션** 할당이, 성능을 떨어뜨릴 수 있음
- 이를 해결하려면
  - 매번 똑같은 **빈 불변 컬렉션**을 반환하기
    - **불변 객체**는 자유롭게 공유해도, 안전함(ITEM.17)

### 빈 불변 컬렉션
- `Collections.emptyList`메서드 예씨
  - 집합이 필요하면 `Collections.emptySet`을,
  - 맵이 필요하면 `Collections.emptyMap`을 사용하면 됨
- 이 역시도 **최적화**에 해당하므로,
  - 꼭 필요할 때만 사용할 것
- 최적화가 필요하다고 판단될 때,
  - 수정 전/후의 성능 측정하기
- 코드 예시
  ```java
  public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? Collections.emptyList()
      : new ArrayList<>(cheeeseInStock);
  }
  ```
- 배열에서도,
  - **`null`을 반환하지말고, 길이가 0인 배열 반환하기**
  - 보통은 단순히 **정확한 길이의 배열**을 반환하면 됨
    - 해당 길이가 `0`일수도 있음
- 코드 예시
  ```java
  public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[0]);
  }
  ```
  - `toArray`에 건넨 길이 `0`의 배열이,
    - 원하는 반환타입(`Cheese[]`)를 알려주는 역할을 한다.
- 위 방식이 성능을 떨어뜨릴 것 같다면,
  - 길이 `0`의 배열을 미리 선언해두고, 이를 반환하면 됨
  - 길이 `0`인 배열은 불변이기 때문
- 배열 불변 예시
  ```java
  private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

  public Cheese[] getCheeses() {
    return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
  }
  ```
  - 최적화 버전의 `getCheeses`는
    - 항상 `EMPTY_CHEESE_ARRAY`를 인수로 넘겨, `toArray`를 호출
  - 따라서 `cheesesInStock` 배열이 비었을 때면
    - 언제나 `EMPTY_CHEESE_ARRAY`를 반환
      - `<T> T[] List.toArray(T[] a)` 메서드의 특징
  - 단순히 성능 개선 목적이라면
    - `toArray`에 넘기는 배열을, 미리 할당하는 것을 추천하지 않음
      - 오히려 성능이 떨어진다는 연구결과 존재
- 성능 저하 예시
  - 배열 미리 할당시, 성능 저하 발생
  ```java
  return CheesesInStock.toArray(new Cheese[cheesesInStock.size()]);
  ```

### 결론
- `null`이 아닌 **빈 배열**이나 **컬렉션**을 반환하기
- `null`을 반환하는 `API`는 사용하기 어려우며,
  - 오류 처리 코드도 늘어남
  - 또한 성능도 좋지 않음
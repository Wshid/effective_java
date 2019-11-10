## [ITEM.35] ordinal 대신 인스턴스 필드

### ordinal 메서드
- 대부분의 열거 타입 상수는
  - 자연스럽게 **하나의 정숫값**에 대응
- `ordinal()`
  - 해당 상수가 그 열거 타입에서 몇 번째 위치인지 반환하는 메서드
  - 열거 타입 상수과 연결된 정수값 필요하면 사용 가능
- 예시 코드
  ```java
  public enum Ensemble {
    SOLO, DUET, TRIO, ...;

    public int numberOfMusicians() {
      return ordinal() + 1;
    }
  }
  ```
  - 동작은 하지만 유지보수하기 매우 좋지 않음

### ordinal을 쓰지 말아야 하는 이유
- **상수 선언 순서**를 변경하는 순간 오동작 한다.
- 이미 사용중인 정수와 **값이 같은 상수는 추가할 방법이 없음**
- **값을 중간에 비울 수 없음**
  - **더미 상수**를 추가하는 등의 불필요한 비용 발생
  - 쓰지 않는 값이 많아질경우, 실용성이 떨어짐

### ordinal의 대안
- 열거 타입 상수에 연결된 값은
  - `ordinal` 메서드로 얻는 것이 아닌,
  - 인스턴스 필드로 저장하면 된다.

### 인스턴스 필드 예시
- 예시 코드
  ```java
  public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), ...;

    private final int numberOfMusicians; // 인스턴스 필드 추가로 인해, 정의된 상수
    Ensemble(int size) { this.numberofMusicians = size; }
    public int numberOfMusicians() { return numberofMusicians; }
  }
  ```

### Enum API 문서에서의 ordinal
- 대부분의 프로그래머는 이 메서드를 쓸 일이 없음
- 이 메서드는 `EnumSet`과 `EnumMap`과 같이
  - 열거 타입 기반의 **범용 자료구조**에 사용할 목적으로 설계 됨
- 이 용도가 아니라면 `ordinal` 메서드를 사용하지 말 것
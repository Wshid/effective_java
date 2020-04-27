## [ITEM.73] 추상화 수준에 맞는 예외

### 갑자기 예외?
- 수행하는 일과 관련이 없는 예외가 발생하면 당황스러움
  - 메서드가 **저수준 예외**를 처리하지 않고,
  - 밖으로 처리할 때 발생하는 상황
- 이는 단순히 프로그래머의 당황뿐 아니라,
  - 내부 구현방식을 노출하여, 윗 레벨 API를 오염시킨다.
- 다음 릴리스의 구현 방식을 바꾸면,
  - 다른 예외가 튀어나와, 기존 클라이언트 프로그램을 깨지게 할 수 있음

### 예외 번역(exception translation)
- 상위 계층에서는
  - **저수준 예외**를 잡아
  - 자기의 **추상화 수준**에 맞는 예외로 바꿔 던져야 함
- 예시 코드
  ```java
  try {
    ... // 저수준 추상화 이용
  } catch (LowerLevelException e) {
    // 추상화 수준에 맞게 번역
    throw new HigherLevelException(...);
  }
  ```

### AbstractSequencialList
- 예외번역의 예시
- `AbstractSequentialList`는 `List` 인터페이스의 **골격 구현**(ITEM.20)
- `List<E>`의 인터페이스 `get` 메서드 명세에 명시된 필수 사항
  ```java
  public E get(int index) { 
    ListIterator<E> i = listIterator(index);
    try {
      return i.next();
    } catch (NoSuchElementException e) {
      throw new IndexOutBoundsException("index : " + index);
    }
  }
  ```

### 예외 연쇄(exception chaining)
- 예외 번역시,
  - **저수준 예외**가 디버깅에 도움이 될 경우
  - `exception chaining`을 사용하는 것이 좋음
- 예외 연쇄란
  - 문제의 근본 원인(cause)인 **저수준 예외**를
  - **고수준 예외**에 실어 보내는 방식
- 별도의 접근자 메서드(`Throwable`의 `getCause` 메서드)를 통해
  - 언제는 저수준 예외를 **꺼내쓸 수 있음**
- 예시 코드
  ```java
  try {
    ... // 저수준 추상화 이용
  } catch (LowerLevelException cause) {
    // 저수준 예외를 고수준 예외에 실어 보내기
    throw new HigherLevelException(cause);
  }
  ```

### 예외 연쇄용 생성자
- **고수준 예외의 생성자**는
  - 예외 연쇄용으로 설계된 상위클래스의 **생성자**에
    - 원인을 건내주어,
    - 최종적으로 `Throwable(Throwable)` 생성자까지 건네지게 한다.
- 예시 코드
  ```java
  class HigherLevelException extends Exception {
    HigherLevelException(Throwable cause) {
      super(cause);
    }
  }
  ```
- 대부분의 표준 예외는 **예외 연쇄용 생성자**를 가지고 있음
- 그렇지 않은 예외라도, `Throwable`의 `initCause` 메서드를 이용해
  - 원인을 직접 찾을 수 있음
- **예외 연쇄**는
  - 문제의 원인을 `getCause`메서드로 프로그램에서 접근할 수 있게 해주며
  - 원인과 고수준 예외의 **스택 추적 정보**를 잘 통합해줌

### 예외 번역의 남용 
- 무턱대고 예외를 전파하는 것보다,
  - 예외 번역이 우수한 방법이긴 하나,
  - **남용하여서는 안됨**
- 가능하면 **저수준 메서드**가 반드시 성공하도록 하여
  - 아래 계층에서는 **예외가 발생하지 않도록**하는 것이 최선
- 때론 **상위 계층 메서드**의 메개변수 값을
  - 아래 계층 메서드로 건네기 전에 **미리 검사**하는 방법도 좋음

### 아래 계층에서의 예외를 피할 수 없을 때
- 상위 계층에서 그 예외를 조용히 처리하여
  - 문제를 `API 호출자`에까지 전파하지 않는 방법
- 이 경우, 발생한 예외는 `java.util.logging` 같은
  - **적절한 로깅기능**을 활용하여 기록해두면 좋음
- 그렇게 해두면
  - 클라이언트 코드와, 사용자에게 문제를 전파하지 않으면서도
  - 프로그래머가 **로그 분석**을 통해 추가 조치가 가능함

### 정리
- 아래 계층의 예외를 예방하거나, 스스로 처리할 수 없고
  - 그 예외를 상위 계층에 그대로 노출하기 곤란하다면 **예외 번역** 사용
- **예외 연쇄**를 이용하면
  - 상위 계층에는 맥락에 어울리는 **고수준 예외**를 던지면서
  - **근본 원인**도 함께 알려주어, 오류 분석하기 쉬움(ITEM.75)
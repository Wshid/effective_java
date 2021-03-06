## [ITEM.71] 필요 없는 검사 예외 사용 회피

### 검사 예외의 사용
- 제대로 활요하면 API와 프로그램의 질을 높일 수 있음
- 결과를 코드로 반환하거나, **비검사 예외**를 던지는 것과 달리
  - **검사 예외**는 발생한 문제를 **프로그래머**가 처리하여
    - 안전성을 높이게 해줌
- **검사 예외**를 과하게 사용하면
  - 쓰기 불편한 API가 됨
- 어떤 메서드가 **검사 예외**를 던질 수 있다고 선언됐다면,
  - 이를 호출하는 코드에서는 `catch`블록을 두어
    - 그 예외를 붙잡아 처리하거나,
    - 더 바깥으로 던져 문제를 전파해야 함
- 어느쪽이든 API 사용자에게 부담을 준다.
- 검사 예외를 던지는 메서드는
  - **스트림 안**에서 직접 사용이 불가(ITEM.48)
  - `java 8:`에서 부담이 더 커짐

### 비검사 예외의 사용
- API를 제대로 사용해도 발생할 수 있는 예외이거나,
  - 프로그래머가 의미 있는 조치를 취할 수 있는 경우라면
  - 위의 부담은 감수할 만하다.
- 하지만 두 경우가 아닐 경우에는, **비검사 예외**를 사용해야 함
- 검사 예외와 비검사 예외 중 어느 것을 선택할지 모를 때,
  - 프로그래머가 그 예외를 어떻게 다룰지 생각해보면 된다.
- 예시
  ```java
  ...
  catch (TheCheckedException e) {
    throw new AssertionError(); // 일어날 수 없음
  }

  // another way
  catch (TheCheckedException e) {
    e.printStackTrace();
    System.exit(1);
  }
  ```
- 더 나은 방법이 없다면 **비검사 예외**를 선택해야 함

### 검사 예외의 부담
- 검사 예외가 프로그래머에게 지우는 부담은
  - 메서드가 **단 하나의 검사 예외**만을 던질 경우 크다
- 이미 다른 **검사 예외**도 던지는 상황에서,
  - 또 다른 **검사 예외**를 추가하는 경우라면
  - 기껏 `catch`문 하나 추가하면 된다.
- 단, 검사 예외가 **단 하나**뿐이라면
  - 오직 그 예외 때문에 `API 사용자`는 `try`블록을 추가하여야 하며
  - **스트림**에서 직접 사용하지 못하게 됨
  - 위 상황이라면, **검사 예외**를 던지지 않는 방법을 고려할 것

### 검사 예외 회피 방법 - Optional
- 적절한 결과 타입을 담은 **옵셔널**을 반환하는 방법(ITEM.55)
- **검사 예외**를 던지는 대신 단순히 **빈 옵셔널**을 반환
- 단점
  - **예외**가 발생한 이유를 알려주는 **부가 정보**를 담을 수 없음
- 예외를 사용하게 되면
  - **구체적인 예외 타입**과 그 타입이 제공하는 메서드를 활용해
    - 부가 정보 제공이 가능(ITEM.70)

### 검사 예외 회피 방법 - 메서드 분리
- 검사 예외를 던지는 메서드를
  - 2개로 쪼개어 **비검사 예외**로 바꿀 수 있음
- 첫번째 메서드 : **예외 던질지 여부**를 `boolean`으로 표현
- 예시
  ```java
  try {
    obj.action(args);
  } catch (TheCheckedException e) {
    ... // 예외 상황에 대처
  }
  ```
- 리팩터링 한 예시
  ```java
  if (obj.actionPermitted(args)) {
    obj.action(args);
  } else {
    ... // 예외 상황에 대처
  }
  ```
- 위 리팩터링을 모든 상황에 적용할 수는 없으나,
  - 적용할 경우 더 쓰기 편한 API 제공이 가능
- 프로그래머가 이 메서드가 성공하리라는걸 안다거나,
  - 실패 시 스레드가 **중단**하길 원한다면, 다음과 같이 작성
  ```java
  obj.action(args);
  ```
- 위 한줄이 주로 쓰일 상황이라면
  - **리팩터링을 권장**
- `actionPermitted`는 **상태 검사 메서드**에 해당하므로
  - **ITEM.69**에서 말한 단점이 그대로 적용됨
    - 리팩터링이 적절하지 않은 경우
      - **외부 동기화** 없이,
        - 여러 스레드가 동시에 접근할 수 있거나,
      - 외부 요인에 의해 **상태가 변할 수 있는 경우**
- `actionPermitted`와 `action` 호출 사이에
  - 객체의 상태가 변할 수 있기 ㄸ깨문
- `actionPermitted`가 `action` 메서드의 작업 일부를
  - **중복 수행**한다면, **성능**상 손해
  - 리팩터링이 적절하지 않음

### 정리
- 꼭 필요한 곳에서 사용한다면
  - **검사 예외**는 프로그램의 안전성을 높임
- 남용할 경우, 쓰기 어려운 API 생성
- API 호출자가
  - **예외 상황**에서 복구할 방법이 없다면 **비검사 예외**를 발생시키기
  - 복구가 가능하고, 호출자가 그를 처리해주길 바란다면
    - 일단 `Optional`을 반환해도 될지 고려
  - `Optional`로 상황을 처리하기에 충분한 정보를 제공할 수 없을 때
    - **검사 예외**를 던지기
## [ITEM.62] 다른 타입이 적절하다면 문자열 사용 회피
### 문자열의 목적
- `String`은 텍스트를 표현하도록 설계
- 문자열을 쓰지 않아야 하는 사례 확인

### 문자열의 잘못된 사용 - 타입 대체
- **문자열**은 **다른 값 타입 대체**에 적절하지 않음
- 파일, 네트워크, 키보드 입력으로 `String`을 사용하나,
  - 입력받을 문자가 실제 **문자열**일 경우만 사용해야 함
- `int`, `float,` `BigInteger`등
- 열거 타입이나 `Boolean`
- **기본 타입**이나 **열거 타입**이나 **적절한 값 타입**이 있다면 사용할 것
  - 없다면 **새로 하나 작성**

### 문자열의 잘못된 사용 - 열거 타입 대체
- 문자열이 **열거 타입**을 대신하기는 부적합
- **ITEM.34**에서와 같이
  - **상수**를 **열거**할때는 **문자열**보다 **열거 타입**이 나음

### 문자열의 잘못된 사용 - 혼합 타입 대체
- **혼합 타입**을 대체하기도 부적합
- 여러 요소가 혼합된 데이터를
  - 하나의 문자열로 표현하지 말 것
- 좋지 않은 예시 코드
  ```java
  String compoundKey = className + "#" + i.next();
  ```
- 위와 같은 예시는 **단점**이 많은 방식
  - 두 요소 구분하는 `#`가 어느 한 요소에서 쓰이면 문제 발생
  - 각 요소를 **개별**로 접근하려면
    - **문자열 파싱**이 일어나야 해서 느리다
  - 오류 가능성도 커짐
  - 적절한 `equals`, `toString`, `compareTo` 메서드를 제공할 수 없음
    - `String`이 제공하는 기능에만 의존
- 차라리 **전용 클래스**를 만드는 편이 좋음
- 이런 클래스는 보통 `private 정적 멤버 클래스`로 선언한다(ITEM.24)

### 문자열의 잘못된 사용 - 권한 표현
- 권한(capacity) 표현에도 적합하지 않음
- 잘못된 예시 : **스레드 지역변수**기능 설계
  - 각 **스레드**가 자신만의 **변수**를 갖게 해주는 기능
  - `java 2`부터 자바에서 지원되었으며, 그 이전에는 직접 구현 필요
  - C가 제공한 **문자열 키**로 **스레드별 지역변수** 식별
  - 코드
    ```java
    public class ThreadLocal {
      private ThreadLocal() {} // 객체 생성 불가

      // 현 스레드의 값을 키로 구분하여 저장
      public static void set(String key, Object value);

      // 키가 가리키는 현 스레드의 값 반환
      public static Object get(String key);
    }
    ```
  - 문제
    - 스레드 구분용 **문자열 키**가 **전역 이름 공간**에서 공유됨
    - 의도된 대로 동작하려면
      - C가 **고유한 키**를 제공해야 함
    - 같은 키가 사용된다면, 의도치 않은 **변수 공유**가 일어남
    - 보안에도 취약 : 악의적으로 동일키 사용 등

### 권한 표현 1차 해결 예시
- 해결한 예시
  - **문자열**대신 **위조 불가 키**를 사용하면 됨
    - 이 **키**를 **권한(capacity)**라고 함
  - 코드
    ```java
    public class ThreadLocal {
      private ThreadLocal() { }

      public static class Key { // capacity
        Key{ }
      }

      // 위조 불가능한 고유키 생성
      public static Key getKey() {
        return new Key();
      }

      public static void set(Key key, Object value);
      public static Object get(Key key);
    }
    ```

### 권한 표현 2차 해결 예시
- 문자열 기반 API의 문제를 해결해주지만, 개선 여지가 있음
- `set`과 `get`은 **정적 메서드**일 필요가 없으므로
  - `Key` 클래스의 **인스턴스 메서드**로 변경
  - 이렇게 할 경우
    - **지역변수**를 구분하는 키가 아닌, **스레드 지역변수**가 된다.
- 결과적으로
  - 지금의 톱레벨인 `ThreadLocal`이 할 일이 없기 때문에, 지운다
  - **중첩 클래스 `Key`**의 이름을 `ThreadLocal`로 변환
- 개선 코드
  ```java
  public final class ThreadLocal {
    public ThreadLocal();
    public void set(Object value);
    public Object get();
  }
  ```
- `get`으로 얻은 `Object`가
  - **실제 타입**으로 형변환해서 사용해야 함
  - 타입 안전하지 않음
- 처음의 문자열 기반 API는
  - **타입 안전 X**
  - `Key`를 사용한 API도 타입안전하기 어려움
- `ThreadLocal`를 **매개변수화 타입(ITEM.29)**로 사용하면 문제 해결
- 매개변수화를 통한 타입 안전한 코드
  ```java
  public final class ThreadLocal<T> {
    public ThreadLocal();
    public void set(T value);
    public T get();
  }
  ```
  - 자바의 `ThreadLocal`과 유사해짐
  - 문자열 기반 API의 문제 해결 및
    - 키 기반 API보다 빠름

### 정리
- 더 적합한 **데이터 타입**이 있거나, 새로 작성할 수 있을 경우
  - **문자열**을 사용하지 X
- 문자열을 잘못 사용하면
  - 번거롭고, 덜 유연하고, 느리며, 오류가능성이 큼
- 문자열을 **잘못 사용**하는 예로
  - **기본타입**, **열거타입**, **혼합타입**이 있음
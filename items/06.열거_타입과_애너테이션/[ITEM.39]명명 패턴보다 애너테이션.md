## [ITEM.39] 명명 패턴보다 애너테이션

### 구분되는 명명 패턴
- 전통적으로 도구나, 프레임워크가 특별히 다뤄야할 **프로그램 요소**에서 쓰임

### 명명패턴, junit 버전3의 단점
- `junit`의 예시
  - 버전 3까지 테스트 메서드 이름을 `test`로만 시작하게 함
- 단점
  - 오타가 나면 안됨
    - `test`로 시작하지 않을경우 인식하지 않음
    - 테스트를 무시하고 지나가게 됨
  - 올바른 프로그램 요소에만 사용된다 보증해야 함
    - 클래스 이름을 `TestSafetyMachanisms`로 지었을 경우
      - `Junit`은 개발자의 의도와는 다르게 별 수행하지 않음
      - 경고 메세지 조차 출력되지 않음
  - 프로그램 요소를 매개변수로 전달할 방법이 없음
    - 특정 예외를 던져야 성공하는 테스트가 있다고 가정
    - 기대한 예외 타입을 테스트 매개변수로 전달해야 함
    - 예외의 이름을 테스트 메서드 이름에 덧붙일 수 있으나
      - 가독성이 떨어지며, 깨지기 쉬움(ITEM.62)
    - 컴파일러는 메서드 이름에 추가된 문자열이 예외인지 알 수도 없음
    - 테스트를 실행하기까지 확인 불가능

### Annotation의 특징
- 위의 문제를 해결할 수있는 방법
- `Junit4`에서도 전면 도입됨
- `Test`라는 이름의 애너테이션 사용시 코드
  ```java
  import java.lang.annotation.*;

  // 테스트 메서드임을 나타내는 애너테이션
  // 매개변수 없는 정적 메서드 전용
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Test{ ... }
  ```
- 메타 애너테이션(meta-annotation)
  - `Test` 에너테이션 타입 선언 자체에도, `@Retention`과 `@Target`을 사용
  - 에너테이션 선언에 다른 애너테이션을 의미
- `@Retention(RetentionPolicy.RUNTIME)`
  - `@Test`가 런타임에도 유지되어야 한다는 표시
  - 이 선언을 생략하게 되면
    - 테스트 도구는 `@Test`를 **인식할 수 없음**
- `@target(ElementType.METHOD)`
  - `@Test`가 반드시 메서드 선언에서만 사용돼야 한다고 알려줌
  - 클래스 선언, 필드 선언등의 **다른 프로그램 요소**에서는 사용 불가
- 매개변수 없는 정적 메서드 전용
  - 컴파일러가 이 제약을 강제할 수 없음
    - 하려면 적절한 애너테이션 처리기를 **직접 구현**해야 함
    - `java.annotation.processing` API
  - 적절한 애너테이션 처리기가 없이
    - 인스턴스 메서드나 매개변수 있는 메서드에 사용할 경우
      - **컴파일**은 되지만, 테스트 도구 **실행 시**에 문제 발생
- `@Test` 애너테이션을 실적용한 모습
  - **마커 애너테이션 (markter-annotation)**
    - 아무 매개변수 없이 단순히 대상에 마킹한다
  ```java
  public class Sample {
    @Test public static void m1() { } // 성공해야 한다.
    public static void m2() { }
    @Test public static void m3() { // 실패
        throw new RuntimeException("실패");
    }
    public static void m4() { }
    @Test public void m5() { } // 잘못 사용한 예, 정적 메서드가 아님
    public static void m6() { }
    @Test public static void m7() { // 실패
        throw new RuntimeException("실패");
    }
    public static void m8() { }
  }
  ```
  - 정적 메서드가 7개, 4개에만 `@Test` 애너테이션 존재
  - `@Test`를 붙이지 않은 4개의 메서드를 테스트 도구는 무시
- `@Test` 메서드가 `Sample` 클래스 의미에 직접적 영향을 주지는 않음
  - 단순히, 추가 정보를 제공할 뿐
  - 대상 코드의 **의미는 그대로**,
    - 애너테이션에 **관심 잇는 도구**에서 **특별한 처리** 수행

### RunTests 예시
- `@Test`의 영향을 이용
    ```java
    import java.lang.reflect.*;

    public class RunTests {
        public static void main(String[] args) throws Exception {
            int tests = 0;
            int passed = 0;
            Class<?> testClass = Class.forName(args[0]);
            for (Method m : testClass.getDeclareMethods()) {
                if (m.isAnnotationPresent(Test.class)) {
                    test++;
                    try {
                        m.invoke(null);
                        passed++;
                    } catch (InvocationTargetException wrappedExc) {
                        Throwable exc = wrappedExc.getCause();
                        System.out.println(m + " 실패: " + exc);
                    } catch (Exception exc) {
                        System.out.println("잘못 사용한 @Test: "+m);
                    }
                }
            }
            System.out.println("성공: %d, 실패: %d%n", passed, tests-passed);
        }
    }
    ```
    - 명령줄로부터 **정규화된 클래스 이름**을 받아
      - 해당 클래스에서 `@Test` 애너테이션이 달린 메서드를 차례로 호출
    - `isAnnotationPresent`
      - 실행할 메서드를 찾아주는 메서드
    - 테스트 메서드가 **예외**를 던질 경우
      - **replication machanism**이 `InvocationTargetException`으로 감싸서 다시 던진다
    - `InvocationTargetException`을 잡아, 원래 예외에 담긴 실패 정보를 추출(`getCause`)후 출력

### 잘못 작성된 테스트 러너
- `InvocationTargetException` 외의 예외가 발생했다면
  - `@Test` 애너테이션을 잘못 사용한 것
- **인스턴스 메서드**, **매개변수가 있는 메서드**, **호출 불가 메서드**에 사용했을 가능성 존재
- 두번째 `catch` 블록에서 감지

### 특정 예외를 던져야만 성공하는 테스트 지원하기
- 새로운 애너테이션 타입이 필요
  ```java
  import java.lang.annotation.*;
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ExceptionTest {
      Class<? extends Throwable> value();
  }
  ```
  - 해당 애너테이션의 매개변수 타입은 `Class<? extends Throwable>`
  - 와일드 카드 타입의 의미
    - `Throwable`을 확장한 클래스의 `Class`객체
      - 모든 예외/오류 타입을 수용한다.
      - **한정적 타입 토큰**(ITEM.33)의 다른 활용 사례
- 애너테이션을 사용하는 실사례
  ```java
  public class Sample2 {
      @ExceptionTest(ArithmeticException.class)
      public static void m1() { // 성공
          int i = 0;
          int i / i;
      }
      @ExceptionTest(ArithmeticException.class)
      public static void m2() { // 실패, 다른 예외 발생
          int[] a = new int[0];
          int i = a[1];
      }
      @ExceptionTest(ArithmeticException.class)
      public static void m3() { } // 실패, 예외는 발생하지 않음
  }
  ```
- 해당 애너테이션을 다룰 수 있도록 테스트 코드를 수정
  ```java
  // main 메서드에 해당
  if(m.isAnnotationPresent(ExceptionTest.class)) {
      tests++;
      try {
          m.invoke(null);
          System.out.println("테스트 %s 실패: 예외를 던지지 않음%n", m);
      } catch (InvocationTargetException wrappedEx) {
          Throwable exc = wrappedEx.getCause();
          Class<? extends Throwable> excType = 
            m.getAnnotation(ExceptionTest.class).value();
          if(excType.isInstance(exc)) {
              passed++;
          } else {
              System.out.printf("테스트 %s 실패: 기대한 예외 %s, 발생한 예외 %s%n",
                m, excType.getName(), exc)
          }
      } catch (Exception exc) {
          System.out.println("잘못 사용한 @ExceptionTest: " + m);
      }
  }
  ```

### 여러 개의 값을 받는 애너테이션 - 다른 방식 구현
- `java8:` 이상에서 지원
- **배열 매개변수**를 사용하는 대신
  - 애너테이션에 `@Repeatable` 메타 에너테이션을 다는 방법
- `@Repeatable`을 단 애너테이션은
  - 하나의 프로그램에 **여러번** 달 수 있음
- 주의할점
  - `Repeatable`을 단 애너테이션을 반환하는 **컨테이너 애너테이션**을 하나 더 정의,
    - `Repeatable`에 **컨테이너 애너테이션**의 `class`객체를 매개변수로 전달해야 함
  - **컨테이너 애너테이션**은 
    - **내부 애너테이션 타입**의 **배열**을 반환하는 `value` 메서드 구현 필요
  - **컨테이너 애너테이션 타입**에는
    - 적절한 보존정책(`@Retention`)과 적용 대상(`@Target`)을 명시해야 함
    - 아닐경우, **컴파일**되지 않음
- 반복 가능한 애너테이션 타입 예시 코드
  ```java
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @Repeatable(ExceptionTestContainer.class)
  public @interface ExceptionTest {
      Class<? extends Throwable> value();
  }

  // 컨테이너 애너테이션
  @Rentention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ExceptionTestContainer {
      ExceptionTest[] value();
  }
  ```
- 배열 코드를 대체하는 반복 가능 애너테이션 적용 코드
  ```java
  @ExceptionTest(IndexOutOfBoundsException.class)
  @ExceptionTest(NullPointerException.class)
  public static void doublyBad() {...}
  ```

### 반복 가능 애너테이션 처리시 주의점
- 반복 가능 애너테이션을 **여러개 달 경우**
  - **하나만 달았을 때**와 구분하기 위해
  - 해당 **컨테이너 애너테이션 타입**이 적용됨
- `getAnnotationsByType` 메서드는 이 둘을 구분하지 않아서
  - 반복 가능 애너테이션과 그 컨테이너 애너테이션을 모두 가져오지만,
  - `isAnnotationPresent` 메서드는 둘을 구분함
- 반복 가능 애너테이션을 여러번 단 다음
  - `isAnnotationPresent`로 **반복 가능 애너테이션**이 달렸는지 검사한다면
    - **그렇지 않다**라고 알려준다.
      - 컨테이너가 달렸기 때문
  - 애너테이션을 여러번 단 메서드를 모두 무시하고 지나감
- `isAnnotationPresent`로 컨테이너 애너테이션이 달렸는지 검사한다면
  - **반복 가능 애너테이션**을 한 번만 단 메서드를 무시하고 지나감
- **달려 있는 수**와 상관 없이 모두 검사하려면
  - 둘을 따로따로 확인해야 함

### 반복 가능 버전 수정 코드
- `RunTests` 프로그램이 `ExeptionTest`의 반복 가능 버전을 사용하도록 수정
  ```java
  if(m.isAnnotationPresent(ExceptionTest.class)
    || m.isAnnotationPresent(ExceptionTestContainer.class)) {
      tests++;
      try {
        m.invoke(null);
        System.out.printf("테스트 %s 실패: 예외를 던지지 않음%n", m);
      } catch (Throwable wrappedExc) {
        Throwable exc = wrappedExc.getCause();
        int oldPassed = passed;
        ExceptionTest[] excTests = m.getAnnotationsByType(ExceptionTest.class);
        for(ExceptionTest excTest : excTests) {
          if(excTest.value().isInstance(exc)) {
            passed++;
            break;
          }
        }
        if(passed == oldPassed)
          System.out.println("테스트 %s 실패: %s %n", m, exc);
      }
    }
  ```
  - 반복 가능 애너테이션을 사용해
    - 하나의 프로그램 요소에 **같은 애너테이션**을 여러번 달 때의 코드 가독성 향상
  - 이 방식으로 코드 가독성 처리가 가능하다면 사용할 것
  - 하지만 애너테이션을 **선언하고 처리하는 부분**에서 코드양 증가
    - 처리 코드가 복잡해 에러날 가능성 존재

### 애너테이션이 명명패턴보다 낫다
- **테스트 애너테이션**으로 할 수 있는 일은 극히 일부
- 다른 프로그래머가 소스코드에 추가 정보를 제공할 수 있는 도구를 만드는 일을 한다면,
  - 적당한 **애너테이션 타입**도 함께 제공할 것
- **애너테이션**으로 할 수 있는 일을 **명명패턴**으로 처리할 이유가 없음

### 결론
- 도구 제작자 외에, 일반 프로그래머가 **애너테이션 타입**을 직접 정의할 일은 없음
- 하지만 **자바 프로그래머**라면, 예외 없이
  - 자바가 제공하는 애너테이션 타입들은 사용해야함(ITEM.40, ITEM.27)
- **IDE**나 **정적 분석 도구**가 제공하는 애너테이션을 사용하면
  - 해당 도구가 제공하는 진단 정보의 품질을 높여줌
- **애너테이션**은 표준이 아니기 때문에
  - 도구를 바꾸거나, 표준이 만들어지면 수정해서 사용해야 함
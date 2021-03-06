## [ITEM.61] 박싱된 기본타입보다 기본타입 사용
### 박싱된 기본 타입
- `int`, `double`, `boolean`과 같은 기본 타입
- `String`, `List`와 같은 참조 타입
- 각각의 **기본 타입**에는 대응되는 **참조 타입**이 존재
  - 이를 **박싱된 기본 타입**이라고 함
  - `int`, `double`, `boolean` -> `Integer`, `Double`, `Boolean`

### 오토박싱과 오토언박싱
- **ITEM.6**에서 처럼
  - **오토박싱**과 **오토언박싱** 때문에
  - 두 타입을 구분하지 않고 사용할 수는 있으나, **차이가 존재**
- 주의해서 사용하여야 함

### 기본 타입과 박싱된 기본 타입의 차이(3)
- **기본 타입**은 **값**만 가지고 있으나,
  - **박싱된 기본 타입**은 `identity`라는 속성이 존재
  - **박싱된 기본 타입**의 두 인스턴스는,
    - **값**이 동일하더라도, 다르게 **식별**될 수 있음
- **기본 타입**의 **값**은 언제나 **유효**하지만,
  - **박싱된 기본 타입**은 `null`을 가질 수 있음
- **기본 타입**이 **박싱된 기본 타입**보다 메모리/시간면에서 **효율적**


### 잘못 구현된 비교자 예시
- `Integer`를 오름차순으로 정렬하는 비교자
  - `compare` 메서드의 리턴
    - `a<b, -`, `a=b, 0`, `a>b, +`
- 코드
  ```java
  Comparator<Integer> naturalOrder =
    (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1);
  ```
- 문제가 없어 보임
  - 왠만한 테스트에는 잘 통과함
  - `Collections.sort`에 원소 백만 개 리스트와, 해당 비교자를 돌려도 정상
  - 리스트에 **중복**이 발생해도 동작
- 심각한 결함
  ```java
  naturalOrder.compare(new Integer(42), new Integer(42));
  ```
  - 두 인스턴스는 값이 동일하기 때문에 `0`을 출력할 것 같지만,
  - 실제로는 `1`을 반환

### 문제의 원인
- `naturalOrder`의 `i < j`는 정상 동작
  - `i`와 `j`가 참조하는 **오토박싱**된 `Integer` 인스턴스는 **기본 타입**으로 변환
  - 이후 `i`가 `j`보다 작은지 평가
    - 작지 않다면, `i==j`를 검사함
- 두번째 검사에서
  - **객체 참조**의 **식별성**을 검사하게 됨
- `i`와 `j`가 다른 `Integer` 인스턴스 라면,
  - 값은 동일 하더라도
  - 결과는 `false`가 되며, 잘못된 결과인 `1`을 리턴한다.
- **박싱된 기본 타입**에 `==` 연산자를 사용하면, 오류가 발생

### 문제를 해결하는 방법
- **기본 타입**을 다루는 비교자가 필요할 때
  - `Comparator.naturalOrder()`를 사용
- **비교자**를 직접 만들면,
  - **비교자 생성 메서드**나
  - **기본 타입**을 받는 **정적 `compare()`메서드**를 사용해야 함(ITEM.14)
- 그렇더라도, 이 문제를 고치려면
  - **지역변수 2개**를 두어
  - **박싱된 `Integer` 매개변수**의 값을
    - **기본 타입** 정수로 저장하여 비교해야 함
  - 비교는 **기본 타입 변수**로 수행

### 오류가 수정된 비교자
- 코드
  ```java
  Comparator<Integer> naturalOrder = (iBoxed, jBoxed) -> {
    int i = iBoxed, j = jBoxed; // Auto-Boxing
    return i <j ? -1 : (i == j ? 0 : 1);
  };
  ```

### 기이한 동작을 하는 코드
- 코드
  ```java
  public class Unbelievable {
    static Integer i;

    public static void main(String[] args) {
      if (i == 42)
        System.out.println("Unbelievable");
    }
  }
  ```
- 위 코드가 `Unbelievable` 결과를 출력하지는 않으나, 기이한 동작 발생
- `i == 42`를 검사할 때, `NullPointerException`이 발생
- 원인은 `i`가 `Integer`이기 때문(`int`가 아닌)
- 다른 **참조 타입** 필드와 마찬가지로
  - `i`의 초기값도 `null`이기 때문
- `i == 42`는 `Interger`와 `int`를 비교하는 코드
- **기본 타입**과 **박싱된 기본 타입**을 혼용한 연산에서는
  - **박싱된 기본 타입**의 박싱이 자동으로 해제됨
- `null` 참조를 언박싱하게 되면 `NullPointerException`이 발생
- 해결 방법
  - `i`를 `int`로 선언하면 됨

### 매우 느린 코드
- 코드
  ```java
  public static void main(String[] args) {
    Long sum = 0L;
    for( long i = 0; i <= Integer.MAX_VALUE; i++) {
      sum += i;
    }
    System.out.println(sum);
  }
  ```
- 지역변수 `sum`을 **박싱된 기본 타입**으로 선언하여 느려짐
- 오류가 경고 없이 컴파일되지만
  - **박싱/언박싱**이 일어나 매우 느려짐

### 박싱된 기본 타입의 사용처
- 컬렉션의 **원소**, **키**, **값**으로 쓰임
  - 컬렉션은 **기본타입**을 사용할 수 X
- **매개변수화 타입**이나 **매개변수화 메서드**의 **타입 매개변수**로 사용
  - **타입 매개변수**로 기본 타입을 지원하지 X
    - `ThreadLocal<int>` 선언 불가
      - `ThreadLocal<Integer>` 선언 가능
- `Reflection`(ITEM.65)를 통해 **메서드** 호출시에도 **박싱된 기본 타입**을 사용

### 결론
- **기본 타입**과 **박싱된 기본 타입**중 선택할 경우, **기본 타입** 사용
  - 간단하고 빠름
- **박싱된 기본 타입** 사용시 신중할 것
- **오토박싱**이 **박싱된 기본 타입**을 사용할 때의 번거로움을 줄이지만
  - **그 위험을 줄이지는 않음**
- 박싱된 기본 타입을 `==`로 비교하면
  - **식별성 비교**가 일어남
- 기본 타입과 박싱된 기본타입을 **혼용**할 경우
  - **언박싱**이 일어나며
  - **언박싱 과정**에서 `NullPointerException` 발생 가능
- **기본 타입**을 박싱하는 작업은
  - 필요 없는 객체를 생성하는 **부작용** 초래
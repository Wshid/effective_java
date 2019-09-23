## [ITEM.26] generic과 raw type
### Generic
- `java5:`
- 제네릭 지원 전까지는, Collection에서 객체 꺼낼 때마다 **형변환**이 필요
    - `Runtime`에 형변환 오류가 발생하기도 함
- 제네릭은
    - `Collection`이 담을 수 있는 type을
    - `compiler`에게 알려준다.
- `compiler`가 타입 검사를 하게 됨으로써
    - 컴파일 과정에서 차단
    - 안전하고 명확한 프로그램 작성 가능

### Generic Class/Interface
- 클래스와 인터페이스 선언에
    - `type parameter`를 사용하는 경우
- `List` Interface의 경우
    - **원소의 타입**을 나타내는 매개변수 **E**로 받게 된다.
    - `List<E>`의 형태
        - 짧게 `List`로도 사용함
- 이 둘을 통틀어
    - **Generic Type**이라고 한다.

### Generic Type
- 각각의 제네릭 타입은, **매개변수화 타입(parameterized type)**을 정의
- `List<String>`에서 `String`을 의미
    -`{클래스/인터페이스 이름}<{타입}>`
- `String`
    - 정규 타입(formal)의 매개변수 `E`에 해당하는 `Actual Type`이다

### raw type
- 제네릭 타입을 정의하면 `raw type`도 함께 정의 된다.
- 정의
    - 제네릭 타입에서 **type parameter**를 전혀 사용하지 않을 때
- `List<E>`
    - raw type : `List`
- 타입 선언에서 **제네릭 타입** 정보가 **전부 지워진것 처럼 동작**
- 제네릭이 도입되기 전 코드와 **호환**되도록 하기 위함

### 과거의 Collection raw type
- 제네릭을 지원하기전 컬렉션 코드
    >```java
    >private final Collection stamps = ...;
    >stamps.add(new Coin(...));
    >```
- `Stamp` 클래스가 아닌 `Coin`클래스를 넣어도,
    - **오류 없이 컴파일**
- 모호한 경고 메세지만 발생
- `Collection`에서 `Coin`을 꺼내기 전까지
    - **오류**를 확인할 수 없음

### Iterator raw type
>```java
>for ( Iterator i = stamps.iterator(); i.hasNext(); ) {
>    Stamp stamp = (Stamp) i.next(); // ClassCastException
>    stamp.cancel();
>}
>```
- 오류는 가능한
    - **즉시**
    - **컴파일** 시간에 발견하는 것이 좋음
- 위 예에서는 `Runtime`때 확인 가능
- `RunTime`때 확인이 가능하다는 것은
    - 문제를 맞는 코드와
    - 원인을 발생시킨 코드가
        - **물리적으로 떨어져 있음을 의미**

### 타입 안정성을 확보한 예시
>```java
>private final Collection<Stamp> stamps = ...;
>```
- `stamps`에는 무조건 Stamp의 인스턴스만 넣어야 함을
    - **Compiler**가 인지할 수 있다.
- **Compiler**는 컬렉션에서 **원소**를 꺼내는 모든 곳에
    - **보이지 않는 형변환** 추가
    - 절대 실패하지 않음을 보장

### raw type을 쓰면 안되는 이유
- **raw type**을 쓰면
    - 제네릭의 장점인 **안전성**과 **표현성**을 잃는다.
- 단순히 **호환성** 때문에 만들어놓은 형태일뿐

### 임의 객체 형태
- `List`같은 raw type이 아닌
    - `List<Object>`의 임의 객체 형태는 괜찮다
- 차이?
    - `List`는 제네릭을 완전히 무시한 형태
    - `List<Object>` 컴파일러에게 모든 타입을 허용한다는 의미
- 매개변수로 `List`를 받는 메서드에
    - `List<String>`을 넘길 수 있지만
- `List<Object>`를 받는 메서드에는 넘길 수 없음
    - **제네릭의 하위 타입 규칙** 때문
- `List<String>`의 경우
    - `List`의 하위 타입
    - `List<Object>`의 하위 타입은 x
- `List<Object>`와는 달리, `List`와 같은 raw type 사용 시, 
    - **타입 안정성**을 잃게 된다.

### Runtime Failed 예시
>```java
>public static void main(String[] args) {
>    List<String> strings = new ArrayList<>();
>    unsafeAdd(strings, Integer.valueOf(42));
>    String s = strings.get(0); // compiler가 자동으로 형변환 코드를 넣는다.
>}
>
>private static void unsafeAdd(List list, Object o) {
>    list.add(o);
>}
>```
- compile은 되지만, raw type인 `List`를 사용하여 경고 발생
- `strings.get(0)`에서 결과를 형변환 할 때,
    - `ClassCastException`이 발생함
    - `Integer` -> `String`으로 변환하려고 하기 때문
- 매개변수를 `List`에서 `List<Object>`로 바꿀 경우
    - `Compile Time`에서 에러 발생

### Unbounded wildcard type(?) : 원소의 타입을 몰라도 되는 raw type
>```java
>static int numElementsInCommon(Set s1, Set s2) {
>    int result = 0;
>    for (Object o1 : s1)
>        if (s2.contains(o1))
>            result++;
>    return result;
>}
>```
- 동작은 하지만, `raw type`을 사용하기 때문에 안전하지 않음
- 대신에 **비한정적 와일드카드 타입(unbounded wildcard type)**을 대신 사용하는 것이 좋음
- **제네릭 타입**을 사용하고 싶으나,
    - 실제 타입 매개변수가 무엇인지, 신경쓰고 싶지 않을 때
    - `?`를 사용하면 된다.
- 제네릭 `Set<E>`의 비한정적 와일드 카드 타입
    - `Set<?>`
        - 어떠한 타입도 담을 수 있는, `범용적인 매개변수화 Set` 타입
    >```java
    >static int numElementsInCommon(Set<?> s1, Set<?> s2) { ... }
    >```

### Set<?>와 Set의 차이
- 와일드 카드 타입은 안전하다
- `raw type Collection`에서는 아무 원소가 넣을 수 있기 때문에
    - **타입 불변식**을 훼손하기 쉽다
- `Collection<?>`에는
    - `null`이외에는 어떤 원소도 넣을 수 없다.
    >```java
    >c.add("verboten"); // 에러 발생
    >// 매개 변수로 받은 Collection<?>에 대해 수정 불가한 것으로 보임
    >```
- 어떤 원소도 넣을 수 없음과 동시에,
    - `Collection`에서 꺼낼 때 객체의 타입도 전혀 알 수 없음
- 제약을 받아들이지 않으려면, 다음을 사용
    - 제네릭 메서드(ITEM.30)
    - 한정적 와일드카드 타입(ITEM.31)

### raw type 사용 금지 예외
- `class literal`에는 raw type을 사용
    - `class literal`에 매개변수화 타입을 사용 못하게 함
        - `List.class, String[].class, int.class` -> 허용 됨
        - `List<String>.class, List<?>.class` -> **허용 x**
- `instanceof`
    - `Runtime`에는 제네릭 정보가 지워지므로,
    - `instanceof` 연산자는
        - **비한정적 와일드 타입** 이외의 매개변수화 타입은 적용 불가
    - `raw type`이든, `?`
        - `instanceof`는 동일 동작
    >```java
    >if (o instanceof Set) { // raw type
    >    Set<?> s = (Set<?>) o; // wildcard type
    >}
    >```
    - `o`의 타입이 `Set`임을 확인 한 후,
        - `Set<?>`로 형변환 해야 한다.
    - **검사 형변환(checked cast)** 이기 때문에
        - 컴파일러 경고 발생 x

### 결론
- **raw type**을 사용하면, `Runtime`에서 에러 발생 가능성 존재
    - 사용하면 안 됨
    - 단순히 호환성 제공 용도
- `Set<Object>` : 어떠한 객체도 저장할 수 있는 **매개변수화 타입**
- `Set<?>` : 모종의 타입 객체만 저장할 수 있는 **와일드카드 타입**
- 위 두개의 raw type인 `Set`은
    - 제네릭 타입 시스템에 속하지 않음
- `Set<Object>`와 `Set<?>`는 안전하지만,
    - raw type인 `Set`은 안전하지 x
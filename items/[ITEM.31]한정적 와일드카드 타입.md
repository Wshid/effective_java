## [ITEM.31] 한정적 와일드카드 타입

### 매개변수화 타입, 불공변
- 매개변수화 타입은 **불공변(invariant)**
- 서로 다른 `Type1`, `Type2`가 존재할 때,
- `List<Type1>`, `List<Type2>`는 서로 하위/상위 타입이 아님
- `List<String>`은 `List<Object>`의 하위타입이 아님
    - `List<Object>`는 어떤 객체든 넣을수 있음
        - `List<String>`은 문자열만 가능
    - `List<String>`은 `List<Ojbect>`가 하는 일을 제대로 수행하지 못함
        - 하위타입이 아님
    - **리스코프 치환 원칙(`ITEM.10`)** 에 위배
        - 어떤 타입에 있어 중요한 속성이라면, 하위 타입에서도 마찬가지로 중요
        - **그 타입의 모든 메서드가 하위 타입에서도 동일하게 작동해야 함**

### 불공변 보다 유연한 방식
- Stack의 코드
    ```java
    public class Stack<E> {
        public Stack();
        public void push(E e);
        public E pop();
        public boolean isEmpty();
    }
    ```
- 일련의 원소를 스택에 추가 하는 메서드 추가
    ```java
    public void pushAll(Iterable<E> src) {
        for (E e : src)
            push(e);
    }
    ```
- `compile`은 되지만, 완벽하진 않음
- `Iterable` src 원소 타입 = `Stack`의 원소 타입
- `Stack<Number>`로 선언 후, `pushAll(intVal)`을 할 경우
    - `Integer intVal`
- `Integer`는 `Number`의 하위 타입이므로, 논리적으로는 동작 해야 함
- 하지만, **매개변수화 타입**은 불공변 이므로, 에러 발생

### 한정적 와일드카드 타입의 필요성
- 특별한 매개변수화 타입 지원
- `pushAll`의 입력 매개변수 타입은
    - `E`의 `Iterable`이 아닌
    - `E`의 하위 타입의 `Iterable`이어야 함
        - `Iterable<? extends E>`

### 한정적 와일드카드 타입을 적용한 pushAll
- 코드
    ```java
    public void pushAll(Iterable<? extends E> src) {
        for (E e : src)
            push(e);
    }
    ```
- `Stack` 및 C에서 활용한 코드도 정상 컴파일
    - 타입 안전하다는 의미

### popAll 메서드
- `Stack`안의 모든 원소를 주어진 **컬렉션**으로 옮겨 담는다.
- 결함이 존재하는 코드 - 와일드카드 타입 사용 x
    ```java
    public void popAll(Collection<E> dst) {
        while(!isEmpty())
            dst.add(pop());
    }
    ```
- **컬렉션**의 원소 타입 == **스택**의 원소 타입
    - 정상 컴파일
- `Stack<Number>`을 `Object`용 컬렉션으로 담는다면, 에러 발생
    - `Collection<Ojbect>`는 `Collection<Number>`의 하위타입이 아니기 때문

### 한정적 와일드카드 타입을 사용하는 popAll
- `popAll`의 입력 매개변수의 타입이
    - `E`의 Collection이 아닌
    - `E`의 상위 타입의 Collection 이어야 함
- 모든 타입은 **자기 자신의 상위타입**
- `Collection<? super E>`
- 개선된 `popAll` 코드
    ```java
    public void popAll(Collection<? super E> dst) {
        while (!isEmpty())
            dst.add(pop());
    }
    ```

### 유연성 극대화
- 유연성을 극대화 하려면
    - **원소의 생산자**나 **소비자**용 **입력 매개변수**에
    - 와일드카드 타입을 사용
- **입력 매개변수**가 **생산자**와 **소비자**의 역할을
    - **동시**에 한다면
    - 와일드 카드 타입 필요 X

### 와일드카드 타입 사용 공식
- **PECS** : 와일드 카드 타입 기본 원칙
    - `producer-extends, consumer-super`
- `Naftalin`과 `Wadler`는 이 공식을
    - `Get and Put Principle`로 부른다.
- 매개변수화 타입 `T`가
    - **생산자**라면 `<? extends T>`
    - **소비자**라면 `<? super T>`
- Stack의 예시에서
    - `pushAll`의 `src` 매개변수는
        - `Stack`이 사용할 `E` instance를 생산
        - `Iterable<? extends E>`
    - `popAll`의 `dst` 매개변수는
        - `Stack`으로부터 `E` `instance`를 소비
        - `Collection<? super E>`

### Chooser 예시
- **ITEM.28**의 `Chooser`를 개선하기
- 기존코드
    ```java
    public Chooser(Collection<T> choices)
    ```
    - 인자로 넘기는 `choices` 컬렉션은,
        - `T` 타입의 값을 **생산**하기 때문에
        - `T`를 확장하는 와일드 카드 타입을 사용한다.
- 개선코드
    ```java
    public Chooser(Collection<? extends T> choices)
    ```
- 코드 변경에 따른 실질적 차이
    - `Chooser<Number>`의 생성자에 `List<Integer>`를 넘긴다면
    - 기존 코드에서는 `compile` 조차 되지 않음
        - `Number`가 `Integer` 상위 타입이지만,
        - 매개변수화 타입은 **invariant** 하기 때문
    - 개선 코드(한정적 와일드카드 타입)에서 문제가 사라짐

### union 메서드 예시
- 기존 코드
    ```java
    public static <E> Set<E> union(Set<E> s1, Set<E> s2)
    ```
- `s1`과 `s2` 모두 `E`의 생산자 이기 때문에, **PEC** 공식에 따라 수정
- 개선 코드
    ```java
    public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2)
    ```
    - 반환 타입 자체는 여전히 `Set<E>`
    - **반환 타입**에는 한정적 와일드카드 타입 **사용 금지**
    - 오히려 유연성을 해친다.
        - C에서 **와일드 카드 타입**을 사용해야 함
- 개선 효과(each java version)
    ```java
    Set<Integer> integers = Set.of(1, 3, 5);
    Set<Double> doubles = Set.of(2.0, 4.0, 6.0);

    // java 8:
    Set<Number> numbers = union(integers, doubles);

    // java 7, 명시적 타입 인수(Explicit type argument)
    Set<Number> numbers = Union.<Number>union(integers, doubles);
    ```
    - 정상 컴파일 된다.
- 클래스 사용자가 **와일드 카드 타입**을 신경써야 하는 경우
    - 해당 **API**에 문제가 있을 가능성이 높음

### parameter vs argument
- 매개변수 vs 인수
- 보통은 명확하게 구분하여 사용하지 않으나,
    - 자바 언어 명세에서 명시적으로 구분
- **매개변수(parameter, formal parameter)**
    - 메서드 **선언**에 정의한 변수
- **인수(argument, actual parameter)**
    - 메서드 호출시 넘기는 실제 값
- 코드상 차이
    ```java
    void add(int value) { ... } // 매개변수 : value
    add(10) // 인수 : 10

    Class Set<T> {...} // 타입 매개변수 : T
    Set<Integer> = ...; // 타입 인수 : Integer
    ```

### max 메서드 예시
- 기존 코드 및 개선 코드
    ```java
    // AS-IS
    public static <E extends Comparable<E>> E max(List<E> list)

    // TO-BE
    public static <E extends Comparable<? super E>> E max(List<? extends E> list)
    ```
- 입력 매개변수 목록 변환부
    - 입력 매개변수에서는 `E` 인스턴스를 생산하기 때문에
    - `List<E>` -> `<List ? extends E> list`
- 매개 타입변수 E
    - `E`가 `Comparable<E>`를 확장,
    - 이때 `Comparable<E>`가 `E` 인스턴스를 소비함
    - `Comparable<E>` -> `Comparable<? super E>`
- `Comparable`은 언제나 소비자 이기 때문에
    - ~~`Comparable<E>`~~ 보다는 **`Comparable<? super E>`** 를 사용하는 것이 좋음
- `Comparator`도 마찬가지
    - ~~`Comparator<E>`~~ 보다는 **`Comparator<? super E>`** 를 사용하는 것이 좋음

### max 처럼 복잡하게 할 이유가 있을까?
- 다음 리스트의 경우, 개선된 `max`로만 커버 가능
    ```java
    List<ScheduledFuture<?>> scheduledFutures = ...;
    ```
- 기존 `max`는 처리 불가능
    - `java.util.concurrent.ScheduledFuture`가 
    - `Comparable<ScheduledFuture>`를 구현하지 않음
- `ScheduledFuture`가 `Delayed`의 하위 인터페이스
    - `Delayed`는 `Comparable<Delayed>`를 확장
- `ScheduledFuture` 인스턴스는
    - 다른 `ScheduledFuture` 인스턴스 뿐만 아니라
    - `Delayed` 인스턴스와의 비교도 가능하기 때문에
        - 기존 `max`가 사용 불가능
- `Comparable(OR comparator)`를 직접 구현하지 않고
    - 직접 구현한 **다른 타입을 확장한 타입**을 지원하기 위해 **와일드 카드**가 필요함

### 복잡한 이유 코드화
- 코드
    ```java
    public interface Comparable<E>
    public interface Delayed extends Comparable<Delayed>
    public interface ScheduledFuture<V> extends Delayed, Future<V>
    ```

### 타입 매개변수 vs 와일드 카드
- 공통되는 부분이 많기 때문에, 둘 중 어느 것을 사용해도 괜찮은 경우가 많음
- swap 예시
    - 리시트에서 명시한 두 인덱스의 아이템 교환
    ```java
    // 타입 매개변수를 사용한 방식
    public static <E> void swap(List<E> list, int i, int j) // List<E> list
    
    // 비한정적 와일드 카드를 사용한 방식
    public static void swap(List<?> list, int i, int j) // List<?> list
    ```
- `public API`라면 **비한정적 와일드 카드** 방식 사용
    - 비교적 간단하기 때문
    - 어떤 리스트를 넘겨도, 명시한 인덱스의 원소 교환 가능
    - 신경 써야할 타입 매개변수도 없음
- 기본 규칙
    - **메서드 선언에 타입 매개변수**가 **한 번만**나올 경우 와일드 카드로 대체할 것
    - 이때, **비한정적 타입 매개변수**라면, **비한정적 와일드 카드**로 대체
    - **한정적 타입 매개변수**일 경우, **한정적 와일드 카드**로 변경

### 비한정적 swap 코드의 문제와 해결방법
- 다음 코드가 컴파일 되지 않음
    ```java
    public static void swap(List<?> list, int i, int j) {
        list.set(i, list.set(j, list.get(i)));
    }
    ```
    - 방금 꺼낸 원소를 리스트에 다시 넣을 수 없다는 에러
    - `List<?>`에서 발생한 문제
        - `List<?>`에는 `null` 이외에는 어떤 값도 넣을 수 없음
- 해결 방법
    - **와일드 카드 타입**의 실제 타입을 알려주는 메서드를
        - `private` 도우미 메서드로 활용
    - 실제 타입을 알기 위해서는
        - **도우미 메서드**는 **제네릭 메서드**이어야 함
    - 해결 코드
    ```java
    public static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }
    
    // 와일드 카드 타입을 실제 타입으로 바꾸어주는 메서드
    private static <E> void swapHelper<List<E> list, int i, int j> {
        list.set(i, list.set(j, list.get(i)));
    }
    ```
    - `swapHelper`의 경우 **리스트**가 `List<E>`임을 알고 있다.
    - 리스트에서 꺼낸 값의 타입은 **항상 `E`**
    - `E` 타입의 값이라면, 이 리스트에 넣어도 안전함을 안다.
    - 메서드 내부가 복잡해 졌으나,
        - 외부에서 **와일드 카드**기반의 선언 유지가 가능
    - `swap`메서드를 호출하는 C는
        - `swapHelper`의 존재를 몰라도 사용 가능
- 도우미 메서드의 시그니쳐는
    - `public API`로 쓰기에는 너무 복잡하는 이유로 버렸던
    - **타입 매개변수로 사용한 swap**의 시그니쳐와 동일함

### 결론
- 조금 복잡하더라도 **와일드 카드 타입**을 적용하면 API가 쉬워짐
- 공통 라이브러리 작성시, **와일드 카드 타입**을 적절히 사용할 것
- **PECS** 공식
    - `producer` : `extends`
    - `consumer` : `super`
- `Comparable`과 `Comparator`는 모두 `consumer`
## [ITEM.30] ASAP generic method
- method도 `generic`으로 보내줄 수 있음
- 매개변수화 타입을 받는 **정적 유틸리티 메서드**는 보통 제네릭
- `Collections`의 알고리즘 메서드는 모두 `generic` 이다.
    - `binarySearch`, `sort`

### 문제가 있는 메서드 예시
- 코드
    ```java
    public static Set union(Set s1, Set s2) {
        Set result = new HashSet(s1); // HashSet(Collection<? extends E>) as a member of raw type HashSet
        result.addAll(s2); // addAll(Collection<? extends E>) as a member of raw type Set 
        return result;
    }
    ```

### 타입 안전하게 변형하기
- 메서드 선언에서의 세 집합(입력 2개, 출력 1개)의 **원소 타입**을 **타입 매개변수**로 명시
- 메서드 안에서도 이 **타입 매개변수**만 사용하게 수정한다.
    - **(타입 매개변수들을 선언하는) 타입 매개변수의 목록은**
    - **메서드의 제한자와 반환 타입 사이에 온다**
- 타입 매개변수 목록 : `<E>`
- 반환 타입 : `Set<E>`

### 제네릭 메서드
- 코드
    ```java
    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }
    ```

### 메서드를 사용하는 프로그램 예시
- 코드
    ```java
    public static void main(String[] args) {
        Set<String> guys = Set.of("톰", "딕", "해리");
        Set<String> stooges = Set.of("래리", "모에", "컬리");
        Set<String> aflCio = union(guys, stooges);
        System.out.println(aflCio);
    }
    ```
    - 해당 프로그램 실행시, `[모에, 톰, 해리, 래리, 컬리, 딕]` 순으로 리턴 됨
        - 원소 순서는 구현 방식에 따라 다름
    - 위 코드의 `union` 메서드는
        - 집합 3개(입력 2개, 출력 1개)의 **타입이 모두 같아야 함**
        - **한정적 와일드 카드 타입(ITEM.31)** 사용시, 더 유연하게 개선 가능

### 불변 객체를 여러타입으로 활용하기 -> Generic Singleton Factory
- `generic`은 `Runtime`에 소거되기 때문에
    - **하나의 객체**를 어떤 타입이로든 **매개변수화**할 수 있음
- 하지만, 요청한 **타입 매개변수**에 맞게,
    - 매번 그 객체의 타입을 바꿔주는
    - **정적 팩터리 메서드**가 필요
- **제네릭 싱글턴 팩터리**
    - `Collections.reverseOrder`와 같은 객체(ITEM.42)
    - `Collections.emptySet`과 같은 **컬렉션**용으로 사용

### 항등 함수를 담은 클래스 만들기
- 항등 함수(Identify function)
    - 입력값을 수정없이 반환하는 특별한 함수
    - 상태가 없다.
- JPL의 `Function.identity`를 사용하면 됨(ITEM.59)
- 항등 함수는 **상태가 없음**
    - 요청할때마다 새로 생성하는 것은 낭비
- `generic`이 실체화 된다면,
    - 타입별로 하나씩 만들어야 하지만
    - 소거 방식이 있기 때문에
        - `Generic Singleton`으로 가능
- `Generic Singleton Factory` 예시
    ```java
    private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

    @SuppressWarnings("unchecked")
    public static <T> UnaryOperator<T> identityFunction() {
        return (UnaryOperator<T>) IDENTITY_FN;
    }
    ```
    - `IDENTITY_FN`을 `UnaryOperator<T>`로 형변환시, 비검사 형변환 경고 발생
    - `T`가 어떤 타입이든
        - `UnaryOperator<Object>`는 `UnaryOperator<T>`가 아니기 때문
    - 항등함수는, 입력값을 그대로 리턴하기 때문에,
        - `T`가 어떤 타입이든, `UnaryOperator<T>`를 사용해도 안전 함

### Generic SingleTon을 활용한 예시
- 코드
    ```java
    public static void main(String[] args) {
        String[] strings = {"삼베", "대마", "나일론"};
        UnaryOperator<String> sameString = identityFunction();
        for(String s : strings)
            System.out.println(sameString.apply(s));

        Number[] numbers = {1, 2.0, 3L};
        UnaryOperator<Number> sameNumber = identityFunction();
        for(Number n : numbers) 
            System.out.println(sameNumber.apply(n));
    }
    ```
    - **Generic Singleton**을 `UnaryOperator<String>`과 `UnaryOperator<Number>`로 사용

### 재귀적 타입 한정(Recursive type bound)
- 자기 자신이 들어간 표현식을 사용하여 **타입 매개변수**의 **허용 범위 한정**
- 드문 케이스
- 타입의 **자연적 순서**를 정의하는 `Comparable` 인터페이스(ITEM.14)에서 사용됨
    ```java
    public interface Comparable<T> {
        int compareTo(T o);
    }
    ```
- 타입 매개변수 `T`는,
    - `Comparable<T>`를 구현한 타입이 비교할 수 있는 **원소 타입**을 정의
- 실제로 모든 타입은 **자신과 동일한 타입의 원소**와만 비교 가능
    - `String`은 `Comparable<String>`을 구현
    - `Integer`는 `Comparable<Integer>`를 구현
- `Comparable`을 구현한 **원소의 컬렉션**을 입력받는 메서드는
    - 주로 그 원소들의
    - `sort`, `search`, `min`, `max` 등으로 사용
- 재귀적 타입 한정을 사용하여, 상호 비교 가능을 표시한 코드
    ```java
    public static <E extends Comparable<E>> E max(Collection<E> c);
    ```
    - `<E extends Comparable<E>>`
        - 모든 타입 `E`는, **자기 자신**과 **비교**할 수 있다

### 재귀적 타입 한정 예시 - 최대값 구하기
- 코드
    ```java
    public static <E extends Comparable<E>> E max(Collection<E> c) {
        if(c.isEmpty())
            throw new IllegalArgumentException("Collection is Empty!");
        
        E result null;
        for(E e : c)
            if(result == null || e.compareTo(result) > 0)
                result = Objects.requireNonNull(e);
        
        return result;
    }
    ```
    - 컬렉션에 담긴원소의 자연적 순서에 따라 비교
    - 컴파일 오류, 경고 발생하지 않음
    - 위 메서드에 `Empty Collection`을 건네면
        - `IllegalArgumentException`이 발생하기 때문에
        - `Optional<E>`를 반환하도록 고치는 것을 추천

### 재귀적 한정 타입의 활용법
- 관용구, 와일드 카드를 사용한 변형(ITEM.31)
- simulated self type 관용구(ITEM.2)

### 결론
- **Generic Type**과 마찬가지로,
    - C에서 **입력 매개변수**와 **반환 값**을
    - 명시적으로 형변환하는 메서드보다,
        - **Generic Method**가 안전하다
- **형변환**없이 사용하는 편이 좋음
    - 이를 사용하려면 **제네릭 메서드**를 사용
- **형변환**을 해줘야 하는 기존 메서드를 **제네릭 메서드**로 변경하기
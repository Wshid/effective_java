## [ITEM.32] generic & varargs
- **가변인수(varargs)** 메서드(ITEM.53)
    - java 5:
- generic과 varargs는 잘 어우러지지 않음

### 가변인수의 특징
- C가 메서드에 넘기는 **인자의 개수**를 조절할 수 있음
    - 구현 방식에 허점이 있음
- **가변 인수 메서드**를 호출할 때
    - 가변 인수를 담기위한 **배열**이 만들어짐
    - 이 배열이 C에게 노출 됨
- `varargs` 매개변수에 `generic`이나, **매개변수화 타입**이 포함되면
    - 컴파일 경고 발생

### 컴파일 경고
- **실체화 불가 타입**
    - 타입 관련 정보 : **Runtime** < **Compile Time**
- 모든 제네릭과 매개변수화 타입은 실체화 되지 않음
- 메서드 선언시에, **실체화 불가 타입**으로 `varargs` 매개변수 선언시,
    - 컴파일 경고 발생
- **가변인수 메서드**를 호출할 때,
    - `varargs` 매개변수가 **실체화 불가 타입**으로 추론되면
    - 그 호출에 대해서도 경고 발생
- 경고 내용
    ```bash
    warning: [unchecked] Possible heap pollution from parameterized vararg type List<String>
    ```
- **매개변수화 타입**의 변수가, **타입이 다른 객체**를 참조하면
    - `heap pollution`이 발생한다.
- 다른 타입 객체를 참조하는 상황에서는
    - 컴파일러가 **자동 생성한 형변환**이 실패할 수 있음
    - 제네릭 타입 시스템이 약속하는 타입 안전성이 무너짐

### generic & varargs, 타입 안전성 붕괴
- 예시 코드
    ```java
    static void dangerous(List<String>... stringLists) {
        List<Integer> intList = List.of(42);
        Object[] objects = stringLists;
        objects[0] = intList; // heap pollution
        String s = stringLists[0].get(0); // ClassCastException
    }
    ```
    - 형변환이 보이지 않아도, 인수를 건네 호출 시,
        - `ClassCastException`이 발생함
    - 마지막줄에, 보이지 않는 형변환이 발생하기 때문
    - 타입 안전성이 깨진다
        - `generic varargs 배열 매개변수`에 값을 저장하는 것은 안전 X

### 제네릭 배열은 안되지만, varargs는 코딩이 되는 이유?
- 제네릭 배열을 프로그래머가 직접 생성 불가
- 제네릭 `varargs` 매개변수 메서드를 선언할 수는 있음
- 이유?
    - **제네릭**이나 **매개변수화 타입의 varargs** 매개변수를 받는 메서드가 실무에서 유용
    - 언어 설계자가 이 모순을 수용하기로 함
- JPL의 메서드 : 그래도 타입 안전함
    - `Arrays.asList(T...a )`
    - `Collections.addAll(Collection<? super T> c, T... elements)`
    - `EnumSet.of(E first, E... rest)`

### @SafeVarargs
- 제네릭 가변인수 메서드 작성자가
    - C에서 발생하는 경고를 숨길 수있게 됨
- 메서드 작성자가
    - **그 메서드가 타입 안전함을 보장**
- 컴파일러는 그를 믿고, 메서드가 안전하지 않을 수 있다는 경고를 하지 않음
- 함부로 사용하면 안 됨

### 메서드가 안전한지 확신하는 방법
- **가변 인수 메서드**호출 시
    - `varargs` 매개변수를 담는 **제네릭 배열**이 만들어진다
- 메서드가 이 **배열**에
    - **아무것도 저장하지 않고**(매개변수를 덮어쓰지 않고)
    - **배열의 참조가 외부 노출이 되지 않는다면**(신뢰할 수 없는 코드가 배열 접근 불가)
    - 타입 안전함
- `varargs` 매개변수 배열이 호출자로부터
    - 그 메서드로 **순수하게 인자들을 전달하는 일만** 한다면 메서드는 안전함
    - `varargs`의 목적으로만 쓰임을 의미

### 매개변수 배열에 저장을 하지 않아도
- `varargs` 매개변수 배열에 **아무것도 저장하지 않고도**
    - 타입 안전성이 깨질 수 있음
- 예시
    ```java
    static <T> T[] toArray(T... args) {
        return args;
    }
    ```
    - 메서드가 반환하는 **배열의 타입**은
        - 이 메서드에 **인수**를 넘기는 **Compile Time**에 결정 됨
    - 그 시점에는 **Compiler**에게 충분한 정보가 주어지지 않음
        - **타입을 잘 못 판단할 수 있다**
- `varargs` 매개변수 배열을 그대로 반환할 시
    - `heap pollution`을 호출한 쪽의 **callStack**까지 전이 될 수 있음
- 예시2 - `pickTwo`
    ```java
    static <T> T[] pickTwo(T a, T b, T c) {
        switch(ThreadLocalRandom.current().nextInt(3)) {
            case 0: return toArray(a, b);
            case 1: return toArray(a, c);
            case 2: return toArray(b, c);
        }
        throw new AssertionError(); // 도달 불가
    }
    ```
    - T타입 인수 3개를 받아
        - 2개를 무작위 리턴
    - **제네릭 가변인수**를 받는 `toArray` 메서드 호출하는 점만 빼면, 위험 하지 않음
    - 이 메서드를 `Compiler`는
        - `toArray`에 넘길 `T` 인스턴스 2개를 담을 `varargs` 매개변수 배열 코드 생성
        - 코드가 만든 배열 타입은 `Object[]`이다
            - `pickTwo`에 어떤 타이브이 객체를 넘기더라도, 담을 수 있기 때문
        - `toArray`가 돌려준 배열은, 그대로 C에게 전달 됨
    - `pickTwo`는 `Object[]` 타입 배열을 반환
- `pickTwo`를 활용하는 `main`
    ```java
    public static void main(String[] args) {
        String[] attributes = pickTwo("Good", "Fast", "Cheep");
    }
    ```
    - `Compile`은 되지만,
        - 실행시, `ClassCastException` 발생
    - **`pickTwo`의 반환값을 `attributes`에 저장하기 위해 `String[]`로 형변환 하기 때문**
    - `Object[]`는 `String[]`의 하위 타입이 아니기 때문에, **형변환**이 **실패** 함
    - `heap pollution`을 찾기 힘들다
        - 원인인 `toArray`로부터 두 단계 떨어짐
        - `varargs`의 매개변수 배열은, **실제 매개변수**가 저장된 후 **변경된 적도 없음**

### 제네릭 매개변수 배열에 타메서드 접근 금지와 예외
- **제네릭 `varargs` 매개변수 배열에**
    - **다른 메서드가 접근하도록 허용하면 안전하지 않다.**
- 하지만 예외가 존재
    - `@SafeVarargs`로 annotate된 또 다른 `varargs` 배열에 넘기는 것은 안전
    - 단순히 배열 내용의 **일부 함수**만 호출하는 **일반 메서드**에 넘기는 것도 안전
        - `varargs`를 받지 않는 일반 함수

### varargs를 안전하게 사용하는 예시
- 예시 코드
    ```java
    @SafeVarargs
    static <T> List<T> flatten(List<? extends T>... lists) {
        List<T> result = new ArrayList<>();
        for(List<? extneds T> list : lists)
            result.addAll(list);
        return result;
    }
    ```
    - 임의 개수의 리스트를 인수로 받아
        - 받은 순서대로 모든 원소를 리스트로 옮겨 반환
    - `@SafeVarargs`가 있기 때문에
        - 선언하는 쪽과, 사용하는 쪽 모두 경고를 내지 않음

### @SafeVarargs를 사용하는 규칙
- **제네릭**이나 **매개변수화 타입**의
    - `varargs` 매개변수를 받는 모든 메서드에 `@SafeVarargs`를 달기
- 사용자를 헷갈리게 하는 컴파일러 경고를 막을 수 있음
- 안전하지 않은 `varargs` 메서드를 작성하면 안된다.
- 제네릭 `varargs` 매개변수를 사용하며, `heap pollution` 경고가 뜬다면
    - **진짜 안전한지 꼭 검증할 것**
- 이 두 조건을 만족하면 안전
    - **`varargs` 매개변수 배열에 아무것도 저장하지 않음**
    - **배열(혹은 복제본)을 신뢰할 수 없는 코드에 노출하지 않음**
- `@SafeVarargs`는 재정의 할 수 없는 메서드에만 달아야 함
    - 재정의한 메서드가 안전할지 보장할 수 없기 때문
    - `java8:` : 정적메서드, final instance 메서드
    - `java9:` : 정적메서드, final instance 메서드, private instance 메서드

### @SafeVarargs외의 해결 방안
- **ITEM.28**을 참고하기
    - `varargs` 매개변수를 `List` 매개변수로 변환
- 개선한 `flatten` 메서드
    ```java
    static <T> List<T> flatten(List<List<? extends T>> lists) {
        List<T> result = new ArrayList();
        for (List<? extends T> list : lists)
            result.addAll(list);
        return result;
    }
    ```
    - 제네릭 `varargs` 매개변수 -> `List`
    - `List.of`(정적 팩터리 메서드)를 활용하면
        - 임의 개수의 인수를 넘길 수 있음
            - `List.of`에 `@SafeVarargs`가 선언되어 있기 때문
        ```java
        audience = flatten(List.of(friends, romans, countrymen));
        ```
- `Compiler`가 메서드의 **타입 안전성**을 검증할 수 있음
- `@SafeVarargs`를 직접 개발자가 달지 않아도 됨
- 단, `C`의 코드가 복잡해지며, 속도가 조금 느려질 수 있음
- `varargs` 메서드를 안전하게 작성하기 불가능할 때 사용 가능
    - `toArray`의 `List`버전 : `List.of`
- `pickTwo`에 `List.of`적용 예시
    ```java
    static <T> List<T> pickTwo(T a, T b, T c) {
        switch(ThreadLocalRandom.current().nextInt(3)) {
            case 0: return List.of(a, b);
            case 1: return List.of(a, c);
            case 2: return List.of(b, c);
        }
        throw new AssertionError();
    }
    ```
    ```java
    public static void main(String[] args) {
        List<String> attributes = pickTwo("Good", "Fast", "Cheep");
    }
    ```
    - 결과 코드는 배열없이 **제네릭**만 사용함 => 타입 안전

### 결론
- **가변인수**와 **제네릭**은 같이 사용할 때 유의
- **가변인수**기능은 **배열**을 **노출**하여, 추상화가 완벽하지 못함
- **배열**과 **제네릭**의 타입 규칙이 다름
- 제네릭 `varargs` 매개변수는
    - **타입 안전하지 않음**
    - 하지만 허용 됨
- 메서드에 제네릭(혹은 매개변수화된) `varargs`를 사용하고자 한다면
    - **메서드**가 **타입 안전**한지 확인
    - `@SafeVarargs`
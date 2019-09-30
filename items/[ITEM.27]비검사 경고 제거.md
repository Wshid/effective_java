## [ITEM.27] 비검사 경고 제거
### 제네릭의 컴파일러 경고
- 경고 종류
    - 비검사 형변환 경고
    - 비검사 메서도 호출 경고
    - 비검사 매개변수화 가변인수 타입 경고
    - 비검사 변환 경고

### 경고 확인
```java
Set<Lark> exaltation = new HashSet();
```
- 위와 같은 잘못된 코드
- `javac` 명령에, `-Xlint:uncheck`옵션 추가시, 컴파일러 경고 확인 가능

### 다이아몬드 연산자
- `java7:`
- 컴파일러가 올바른 실제 타입 매개변수 추론
>```java
>Set<Lark> exaltation = new HashSet<>();
>```

### SuppressWarnings 어노테이션 사용
>```java
>@SuppressWarnings("unchecked")
>```
- **경고를 제거할 수는 없지만, 타입 안전하다고 확신할 수 있다면 사용**
- 다만, 타입 안전함을 검증하지 않은 채, 경고를 숨기면 안 됨
- 경고 없이 컴파일은 되겠지만,
    - `Runtime`에서는 `ClassCastException` 발생
- 안전하다고 검증된 **비검사 경고**를 숨기지 않고 그냥 두면,
    - 진짜 문제를 알리는 **새로운 경고** 인지 불가
- 제거하지 않은 수많은 거짓 경고 속에
    - **새로운 경고**가 묻힐 수 있음
- `@SuppressWarnings`
    - 개별 지역 변수 선언부터, 클래스 전체까지 어떤 선언에도 달 수 있음
    - 하지만, **ASAP, 좁은 범위에 할당할 것**
        - 변수 선언, 짧은 메서드, 생성자 등
    - 심각한 경고를 놓칠 수 있기 때문에, **클래스 전체 적용 x**
- 한 줄이 넘는 메서드, 생성자에 달린 `@SuppressWarnings`는,
    - 지역변수 선언 쪽으로 이동
    - 지역변수를 새로 선언할 수도 있으나.. 이점이 있다.
- `toArray` 예시
    ```java
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(elements, size, a.getClass());
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }
    ```
- 위 코드를 컴파일 하면, `return ... Arrays.copyOf(elements, ...)`에서 경고 발생
- 어노테이션은 **선언**에만 달 수 있다.
    - `return`문에는 `@suppressWarnings`를 다는 것이 불가능
- **반환 값**을 담을 지역변수를 하나 선언하고,
    - 그 변수에 **어노테이션**을 달 것
- 지역변수를 초기화 해서, `@SuppressWarnings`의 범위 좁히기
    ```java
    public <T> T[] toArray(T[] a) {
        if (a.length < size)) {
            // 생성 배열 및 매개변수 배열의 타입이 모두 T[]
            // 올바른 형변환
            @SuppressWarnings("unchecked") T[] result = 
                (T[]) Arrays.copyOf(elements, size, a.getClass());
            return result;
        }
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }
    ```
- `@SuppressWarnings("unchecked")` 어노테이션을 사용할 때는,
    - **그 경로를 무시해도 안전한 이유를 주석으로 남겨야 함**

### 결론
- 비검사 경고는 중요하기때문에 무시하면 x
- 모든 비검사 경고는 `Runtime`에 `ClassCastException`이 발생할 수 있음
- 경고를 제거할 방법을 못 찾았을 때,
    - 해당 코드의 **타입 안전함**을 증명하고,
    - 가능한한 범위를 좁혀 `@suppressWarnings("unchecked")` 어노테이션으로 경고를 숨길 것
    - 경고를 숨기기로 한 **근거**를 **주석**으로 남길 것

## [ITEM.28] 배열보다 리스트
### 배열과 제네릭의 차이
- diff 1
    - 배열은 **공변(covariant)**이다.
        - `Sub`가 `Super`의 **하위 타입**일 때,
        - `Sub[]`는 `Super[]`의 **하위 타입**이다.
    - 제네릭은 **불공변(invariant)**이다.
        - 다른 타입 `Type1`, `Type2`가 있을 때,
        - `List<Type1>`은 `List<Type2>`의 하위 타입 x, 상위 타입 x
- diff 2
    - **배열**은 **실체화(reify)** 된다.
        - **배열**은 `Runtime`에도 자신이 담기로 한 **원소의 타입**을 인지하고 확인 함
        - `Long`배열에 `String`을 넣으려 하면 `ArrayStoreException`이 발생함
    - **제네릭**의 경우 **타입 정보**가 `Runtime`에는 소거(erasure)된다.
        - 타입을 **Compile Time**에만 검사
        - `RunTime`에는 알 수 없음
        - **소거**는 제네릭이 지원되기 전의
            - **레거시 코드**와의 호환성을 위함

### 문법상 허용되는 코드
- Runtime Failed
    ```java
    Object[] objectArray = new Long[1];
    objectArray[0] = "타입이 달라 넣을 수 없음"; // ArrayStoreException
    ```
- Compile Failed
    ```java
    List<Ojbect> ol = new ArrayList<Long>(); // 호환되지 않음
    ol.add("타입이 달라 넣을 수 없음");
    ```
- `Long`형 저장소에 `String`을 넣는 것을 불가능
- **배열**의 경우 `Runtime`때 확인 가능
- **리스트**의 경우 `Compile Time` 때 확인 가능

### 배열과 제네릭
- 잘 어울리지 못함
- 배열은 **제네릭 타입**, **매개변수화 타입**, **타입 매개변수**로 사용 불가
    - `new List<E>[]`, `new List<String>[]`, `new E[]`로 사용 불가
    - 컴파일 할 때, **제네릭 생성 오류**를 발생 시킴
- 제네릭 배열 생성이 허용되지 않는다.(`Compile`되지 않음)
    ```java
    List<String>[] stringLists = new List<String>[1]; // 1
    List<Integer> intList = List.of(42); // 2
    Object[] objects = stringLists; // 3
    objects[0] = intList; // 4
    String s = stringLists[0].get(0); // 5
    ```
    - `1`이 허용된다고 가정,
        - `2`에서 원소 하나인  `List<Integer>` 생성
        - `3`에 `1`에서 생성한 `List<String>` 배열 할당
            - 배열은 `covariant`하기 때문에, 문제가 발생하지 않음
        - `4`는 `2`에서 생성한 `List<Integer>`의 인스턴스를 **Object** 배열의 첫 원소로 저장
            - 제네릭은 **소거**방식으로 구현되므로, 성공
            - `Runtime`에는 `List<Integer>`의 인스턴스 타입은
                - 단순 `List`
                - `List<Integer>[]`의 인스턴스 타입은
                    `List[]`가 된다.
            - 따라서 `4`에서도 `ArrayStoreException`을 발생시키지 않음
    - `List<String>` 인스턴스를 담는다고 선언한 `sringLists`의 문제
        - 해당 배열에는 `List<Integer>` 인스턴스가 담겨 있다.
        - `5`에서 해당 배열의 첫 원소 꺼내기
        - `compiler`에서 원소를 자동으로 `String` 형변환
        - 원소는 `Integer`이기 때문에 `ClassCastException` 발생
    - 해당 문제를 방지하려면, `1`에서 `compile error`가 발생행 ㅑ함

### 실체화 불가 타입(non-refiable type)
- `E`, `List<E>`, `List<String>`
- 실체화 되지 않아,
    - `Runtime`에는 `CompileTime`보다 `Type`정보를 적게 가진다.
- 소거 메커니즘 때문에 
    - 매개변수화 타입 가운데
    - 실체화 될 수 있는 타입은 `List<?>`, `Map<?,?>`와 같은 `비한정적 와일드카드` 타입(ITEM.26)
- `비한정적 와일드카드 타입`으로 만들 수는 있으나, 유용하게 쓸 일이 없음

### 배열을 제네릭으로 만들수 없어 불편한 점
- `Generic Collection`에서
    - 자신의 원소 타입을 담은 **배열**을 반환하는게 보통 불가능 함
        - `ITEM.33`에서 해결 방법 존재
- **Generic Type**과 **varargs method**(가변인수 메서드, ITEM.53)를 함께 사용시,
    - 해석하기 어려운 경고 메세지 발생
- 가변 인수 메서드를 호출할 때마다
    - 가변인수 매개변수를 담을 배열이 만들어 짐
    - 그 배열의 원소가 **실체화 불가 타입**이라면 경고 발생
    - `@SafeVarargs`로 해결 가능(ITEM.32)

### 제네릭 배열 생성 오류, 비검사 형변환 경고 해결법
- 대부분 `E[]` -> `List<E>`를 사용하면 해결 됨
- **타입 안정성**과 **상호운용성**

### Chooser 예시
- 컬렉션 안의 원소 중 하나를 무작위로 선택/반환 하는 `choose`메서드
- 제네릭을 사용하지 않을 때
    ```java
    public class Chooser {
        private final Object[] choiceArray;

        public Chooser(Collection choices) {
            choiceArray = choices.toArray();
        }

        public Object choose() {
            Random rnd = ThreadLocalRandom.current();
            return choiceArray[rnd.nextInt(choiceArray.Length)];
        }
    }
    ```
    - `choose` 메서드 호출 시마다, `Object`를 형변환 해주어야 함
    - 타입이 다른 원소 존재시, `형변환 에러`발생
- 제네릭으로 수정한 코드
    ```java
    public class Chooser<T> { 
        private final T[] choiceArray;

        public Chooser(Collection<T> choices) {
            choiceArray = choices.toArray();
        }
        // choose 메서드 동일
    }
    ```
    - 코드 컴파일시, 에러 발생
        - `choices.toArray`
    - 에러 해결방법
        - `choiceArray = (T[]) choices.toArray();`
    - 또다른 경고 발생
        - `T`의 타입을 알 수 없으므로,
        `Runtime`일 때 안전할 수 없다는 경고
- **제네릭**에서는 **원소의 타입 정보가 소거**되어 **Runtime**에는 무슨 타입인지 알 수 없음
- 위 코드도 동작은 할 것

### 비검사 형변환 경고 제거 코드
```java
public class Chooser<T> { 
    private final List<T> choiceLists;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }

    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }
}
```
- 코드 양도 늘고, 속도도 느려질 것
- 하지만 `Runtime`때 `ClassCastException`은 발생하지 x

### 결론
- **배열**과 **제네릭**에는 다른 타입 규칙 적용됨
    - **배열**은 공변, 실체화
        - `Runtime`에 `타입 안전`하다
        - `CompileTime`에는 `타입 불안전`
    - **제네릭**은 불공변, 타입정보 소거
        - `Runtime`에 `타입 불안전`
        - `CompileTime`에는 `타입 안전`
- 배열과 제네릭을 섞어 사용하기는 어려움
- 둘을 섞어쓰다가 `Compile` 오류나, 경고 발생시
    - `배열` -> `List` 고려

    
- **

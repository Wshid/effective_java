## Java Functional Programming
- [refer](https://junsday.tistory.com/37)
- [refer2](https://jinbroing.tistory.com/229)
- 자바에서의 FP 차이

### 특징
- immutable
- First class
- Lambda & Closure
    - closure
        - 자신이 생성될 때의 scope에서 알 수 있는 변수를 기억하는 함수
- Higher-order Function
    - 함수를 인자로 받거나, 리턴함

### Concept
- mutable -> immutable, side effect 제거
- 모든 것은 객체 -> 1급 객체(First Class)
    - 함수를 값으로 할당할 수 있음
    - 파라미터로 전달 및 결과 값으로 반환 가능
- 코드 간결, 로직 집중
    - Lambda, Stream과 같은 API 사용,
    - BoilerPlate 제거
        - 변경 없이 계속하여 재 사용할 수 있는 저작품
- 동시성 작업을 쉽고 안전하게

### Java8 FP
#### Functional Interface
- `@FunctionalInterface`
- 하나의 `abstract method`를 가지는 인터페이스
```
@FunctionalInterface
public interface Runnable{
    public abstract void run();
}
```

#### Lambda
- 일반적으로 생각하는 람다 식
- `(int x, int y) -> x+y`
- `(x, y) -> x+y`

### Method Reference
- 이름을 가진 메소드들에 대한 표현방법
```
String::valueOf
    x -> String.valueOf(x)
Object::toString
    x -> x.toString
x::toString
    () -> x.toString()
ArrayList::new
    () -> new ArrayList<>()
```

### Closure
- lambda는 body 외부에 정의된 non-static 변수/ 개체에 접근 가능
    - Capturing
- 람다 표현식은 오직 로컬 변수와 인자로 던져진 블록에서만 접근 가능

### Stream
- spark with scala에서 보았던 Transformation과 Action에 대한 내용
- filter, map, flatMap, peel, distinct, sorted, limit, substream
- forEach, toArray, reduce, collect, min, max, ...


### FP Pattern
- `Function<T, P>` : `<T> -> <P>`
- `Consumer<T>` : `<T> -> void`
- `Predicate<T>` : `<T> -> Boolean`
- `Supplier<T>` : lazy evaluation
    - 작업 지연 혹은 특정 시점에서만 작업될 수 있도록 할 때 사용


## [ITEM.20] 추상클래스보다 인터페이스
- java가 제공하는 다중 구현 메커니즘
    - `interface`와 `abstract class`
- java8 부터 interface도 `default method`를 지원
    - 인스턴스 메서드 구현 형태 제공 가능
- 둘 간의 큰 차이
    - 추상 클래스가 정의한 타입을 구현하는 클래스
        - 추상 클래스의 하위 클래스가 되어야 한다
- 자바는 단일 상속만 지원
    - 따라서 추상 클래스 방식은,
    - 새로운 타입 정의 제약 존재
- 인터페이스의 경우
    - 선언한 메서드를 모두 정의하고, 일반 규약만 잘 지키면 
    - 다른 상속을 한 클래스라도, 같은 타입으로 취급 됨

### 추상 클래스 대비 인터페이스의 장점
- **기존 클래스에도 쉽게 새로운 인터페이스 구현 가능**
    - 인터페이스가 요구하는 메서드를 추가
    - 클래스 선언에서 `implements` 구문만 추가 하면 됨
    - JPL
        - `Comparable`, `Iterable`, `AutoCloseable` 인터페이스 추가 시
        - 많은 기존 클래스가 해당 인터페이스를 구현해서 들어옴
    - **기존 클래스**에 **추상 클래스**를 끼워 넣는 것은 어려움
        - 두 클래스가 같은 추상 클래스 확장을 원할 때
        - **추상 클래스**는 무조건 두 클래스의 공통 조상이어야 함
            - **클래스 계층 구조**에 혼란 야기
        - 새로 추가된 추상 클래스의 모든 자손이 강제 상속을 해야하기 때문
- **인터페이스는 mixin 정의에 적합하다**
    - mixin
        - 클래스가 구현할 수 있는 타입
        - mixin을 구현한 클래스에 원래의 `주된 타입`외에 특정 선택적 행위를 제공한다고 선언
    - `Comparable`은
        - 자신을 구현한 클래스의 instance 끼리
        - 순서 비교 가능한 mixin-interface
    - 대상 타입의 주된 기능에 선택적 기능을 혼합한다는 의미
    - 추상 클래스로는 정의 불가능
        - 기존 클래스를 덧씌울 수 없기 때문
        - 클래스 계층 구조상 mixin의 위치가 애매함
- **인터페이스로 계층 구조가 없는 type framework를 만들 수 있다**
    - type을 계층적으로 정의하면 많은 개념 구조적 표현 가능
    - 현실적으로 구분이 어려울 수 있음
    - 조합 폭발(combinatorial explosion)
        - 클래스로 여러 케이스에 대한 상속이 필요하다고 할때,
        - 지원해야할 조합의 수가 `2^n`개가 되는 현상
        - 인터페이스 사용으로 해결 가능
    - 거대한 클래스 계층 구조에서, 공통 기능을 정의해 논 타입 정의가 불가능
        - 매개변수 타입만 다른 메서드가 많아지는 현상 발생 가능
- **래퍼 클래스 관용구(ITEM.18)과 함께 사용시, 안전하고 강력하다**
    - 타입을 추상 클래스로 정의 시,
    - 타입에 기능 추가하는 방법은 상속만 존재
    - 상속해서 만든 클래스는, 래퍼 클래스보다 활용도가 떨어짐
- **구현 방법이 확실할 때, default method로 제공하기**
    - 디폴트 메서드 제공시 상속하려는 사람을 위해
    - `@implSpec` javadoc tag를 붙여 문서화(ITEM.19)
    - deafult method 제약
        - 많은 interface가 `equals`와 `hashCode` 같은 `Object` 메서드 제공
        - 하지만 이를 default method로 제공하면 안됨
        - interface는 인스턴스 필드를 가질 수 없음
            - `public`이 아닌 `static 멤버`도 가질 수 없음
                - `private static` 은 가능함
        - 개발자가 만들지 않은 인터페이스에서는 default method를 추가할 수 없음

### 추상 골격 구현(skeletal implementation) 클래스
- 인터페이스와 추상 클래스의 장점을 모두 취하는 방법
- 인터페이스로는 타입을 정의
    - 필요하면 default method도 함께 제공
- 골격 구현 클래스로 나머지 메서드 구현
- 효과
    - 단순히 골격 구현 확장을 통해, 인터페이스를 구현하는 데 필요한 일이 끝남
- **Template Method Pattern**

### 관례상 골격 구현 클래스 인터페이스 표현
- `Interface` 이름이라면,
    - 골격 구현 클래스의 이름은 `AbstractInterface`
- Ex, Collection Framework의 핵심 컬렉션 인터페이스의 골격 구현
    - `AbstractCollection`, `AbstractSet`, `AbstractList`, `AbstractMap`
- `Abstract` 접두를 활용한다.
- 골격 구현을 제대로 했다면,
    - 인터페이스로 나름의 구현을 만들려는 프로그래머의 비용 절감 가능

### Example
>```
>    static List<Integer> intArrayAsList(int[] a) {
>        Objects.requireNonNull(a);
>
>        // 다이아몬드 연산자, java 9부터 이렇게 사용 가능
>        return new AbstractList<>() { // :java8, AbstractList<Integer>
>            @Override
>            public Integer get(int i) {
>                return a[i]; // AutoBoxing(ITEM.6)
>            }
>
>            @Override public Integer set ( int i, Integer val){
>                int oldVal = a[i]; // AutoUnBoxing
>                a[i] = val; // AutoBoxing
>                return oldVal;
>            }
>
>            @Override public int size () {
>                return a.length;
>            }
>        };
>    }
>```
- List 구현체를 반환하는 정적 팩터리 메서드
- AbstractList를 골격 구현으로 사용
- int 배열을 받아, `Integer` instance의 리스트 형태로 보여주는 **Adapter**
- `int` <-> `Integer` 사이의 변환은 성능이 좋지 않음
- 익명 클래스(ITEM.24) 사용

### 골격 구현 클래스의 장점
- 추상 클래스처럼 구현을 도와준다
- 추상 클래스로 타입을 정의할 때의 제약 조건에서 자유로움
- 골격 구현 확장으로 `interface` 구현이 거의 끝남 // 꼭 이렇게 할 필요는 없음
- 구조상 골격 구현이 어려울 경우
    - interface를 직접 구현해야 함
- interface가 직접 제공하는 default method의 이점은 유지 됨
- 골격 구현 클래스의 우회적 사용
    - `interface`를 직접 구현한 클래스에서
    - 해당 골격 구현을 확장한 `private` 내부 클래스 정의
    - 각 메서드 호출을 내부 클래스의 `instance`에 전달
    - (ITEM.18)의 래퍼클래스와 유사
        - **시뮬레이트한 다중 상속(simulated multiple inheritance)**
            - 다중 상속의 많은 장점 제공
            - 단점 회피

### 골격 구현 작성은 쉽다
- interface에서 다른 메서드의 구현에 사용되는 `기반 메서드` 선정
    - `기반 메서드`, 골격 구현에서의 `추상 메서드`
- 기반 메서드를 사용해 구현할 수 있는 메서드 -> `default method`로 제공
    - 단, `equals`, `hashcode`와 같은 `Object method`의 경우,
    - `default method`로 제공하면 안됨
- `interface`의 메서드 모두가
    - `기반 메서드`와 `default method`가 된다면,
    - **골격 구현 클래스**를 별도로 만들 필요 x
- 기반 메서드나, default method로 만들지 못한 메서드가 남는다면,
    - interface를 구현하는 골격 구현 클래스를 하나 만든다
    - 남은 메서드를 작성해서 넣음
- 골격 구현 클래스에는
    - 필요하면 `public`이 아닌 필드와 메서드를 추가해도 됨

### Example, 골격 구현 작성
- `Map.Entry` Interface
- `getKey`, `getValue`는 확실히 기반 메서드
    - 선택적으로 `setValue` 포함 가능
- `equals`와 `hashCode`의 동작 방식 정의 되어 있음
- `Object` 메서드들은 `default method`로 제공하면 안되기 때문에,
    - 해당 메서드들은 모두 골격 구현 클래스에 구현
- `toString`도 기반 메서드를 사용해 구현
>```
>public abstract class AbstractMapEntry<K, V> implements Map.Entry<K, V> {
>
>    // 변경 가능한 엔트리는 이 메서드를 반드시 정의
>    @Override
>    public V setValue(V value) {
>        throw new UnsupportedOperationException();
>    }
>
>    // Map.Entry.equals의 일반 규약 구현    
>    @Override
>    public boolean equals(Object o) {
>        if (o == this)
>            return true;
>        if (!(o instanceof Map.Entry))
>            return false;
>        Map.Entry<?, ?> e = (Map.Entry) o;
>        return Objects.equals(e.getKey(), getKey()) &&
>                Objects.equals(e.getValue(), getValue());
>    }
>    
>    // Map.Entry.hashCode 일반 규약 구현
>    @Override
>    public int hashCode() {
>        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
>    }
>
>    @Override
>    public String toString() {
>        return getKey() + "=" + getValue();
>    }
>}
>```
- `Map.Entry` Interface나 그 하위 인터페이스로는
    - 이 골격 구현 제공이 불가능
- `default method`는 `equals`, `hashCode`, `toString`과 같은
    - `Object` 메서드 재정의가 불가능하기 때문
- 골격 구현은 기본적으로 **상속**해서 사용하는 것을 가정
    - `ITEM.19`에서의 **설계 및 문서화** 지침을 모두 따라야 함
- `default method`, `별도의 추상 클래스`등 동작 방식을 잘 정리해야 함

### Simple Implementation
- 단순 구현은 골격 구현의 작은 종
- `AbstractMap.SimpleEntry`
- 골격 구현과 같이 상속을 위해 `인터페이스`를 구현한 것
    - 단, 추상 클래스가 아님
- 동작하는 가장 단순한 구현
    - 그대로 쓰거나, 필요에 맞게 확장해도 됨

### 결론
- 다중 구현용 타입 `interface`가 제일 적합
- 복잡한 인터페이스일 경우
    - 골격 구현과 함께 제공하는 방법 고려
- 골격 구현은 **ASAP** interface의 `default method`로 제공
    - 인터페이스를 구현한 모든 곳에서 활용할 수 있도록 하는 것이 중요
- **ASAP**
    - `interface`에 걸려있는 구현상의 제약 때문에
    - 추상 클래스로 제공하는 경우가 흔하기 때문
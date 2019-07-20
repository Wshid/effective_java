## [ITEM.13] clone 재정의

### Cloneable의 문제
- 복제해도 되는 클래스임을 명시하는 용도
- [mix-in](https://stonzeteam.github.io/%EC%BD%94%EB%93%9C-%EC%9E%AC%EC%82%AC%EC%9A%A9%EC%9D%84-%EC%9C%84%ED%95%9C-Mixin/) interface
-  `clone()` 메서드가 선언된 곳 
    - Clonable이 아닌 `Object`
    - protected
- `Cloneable`을 구현하는 것만으로는 `clone` 메서드를 호출할 수 없음
- `clone` 메서드의 구현방법, 가능한 다른 선택지는?

### Cloneable 인터페이스
- Object의 `protected` 메서드인 `clone`의 동작방식을 결정
- `Cloneable`을 구현한 클래스의 인스턴스에서 `clone` 호출 시,
    - 그 객체의 필드를 하나씩 복사하여 반환
    - 그렇지 않은 클래스는 `CloneNotSupportedException` 반환
- 인터페이스를 구현한다는 것은
    - 일반적으로 해당 클래스가 그 인터페이스에서 정의한 기능을 제공한다고 선선하는 행위
    - Cloneable의 경우에는 상위 클래스의 `protected` 메서드의 동작방식을 변경

### Clonable을 구현한 클래스
- clone 메서드를 public으로 제공
- 사용자는 당연히 복제가 제대로 이뤄질꺼라 기대하지만..
- 이를 만족시키려면,
    - 해당 클래스와 모든 상위 클래스는 복잡하고, 강제할 수 없음
    - 허술하게 기술된 프로토콜을 지켜야만 함
    - 깨지고 쉽고, 위험하고, 모순적인 매커니즘

### clone 메서드의 일반 규칙
- 객체의 복사본을 생성해 반환한다.
    - `복사`는 객체를 구현한 클래스에 따라 다름
- `x.clone() != x` : true
- `x.clone().getClass() == x.getClass()` : true, 일반적으로 성립
- `x.clone().equals(x)` : true, 일반적으로 성립, 필수는 아님
- `x.clone().getClass() == x.getClass()`
    - 이 클래스와 (Object를 제외한) 모든 상위 클래스가 이 관례를 따른다면 `true`
    - 관례상, 이 메서드가 반환되는 객체는 `super.clone` 호출해야함
- 관례상, 반환된 객체와 원본 객체는 독립적이어야 함
    - `super.clone`으로 얻은 객체의 필드 중 하나 이상을 반환 전에 수정 필요

### 생성자와 유사
- 강제성이 없다는 점만 빼면 생성자 연쇄(constructor chaining)와 유사
- `clone` 메서드가 `super.clone`이 아닌, `생성자를 호출해 얻는 인스턴스`를 반환해도
    - 컴파일러는 에러를 뱉지 않음
    - 하지만, 이 클래스의 하위 클래스 `super.clone`을 호출한다면
        - 잘못된 클래스 객체 생성
            - 하위 클래스의 `clone` 메서드가 동작하지 않을 것
            - 물론 `clone` 재정의 클래스가 `final`이면 상관 없음
                - 하위 클래스가 없으므로
            - `final` 클래스의 `clone` 메서드가 `super.clone`을 호출하지 않으면
                - `Cloneable`을 구현할 이유 없음
- `final` 클래스
    - 더 이상 확장이 불가능한 클래스

### 제대로 된 상위클래스, clone 메서드
- 제대로 동작하는 clone 메서드를 구상한 상위 클래스를 상속
    - `Cloneable`을 구현한다면,
    - `super.clone` 호출
        - 완벽한 복제본, 클래스의 정의된 모든 필드의 값이 원본과 동일
        - 모든 객체의 필드가 `primitive` 하거나 `immutable`이라면 이상적
- `PhoneNumber` 예시
    >```
    >@Override public PhoneNumber clone() {
    >    try {
    >        return (PhoneNumber) super.clone();
    >    } catch (CloneNotSupportedException e) {
    >        throw new AssertionError(); // 일어나지 않을 일
    >    }
    >}
    >```
- 해당 `clone` 메서드가 동작하려면, `PhoneNumber` 클래스 선언에 `Cloneable`을 선언해야 함
    - `Object::clone()` => Object 리턴
    - `PhoneNumber::clone()` => PhoneNumber 리턴
        - covarient return typing(공변 반환 타이밍) => `권장`
            - 재정의한 메서드의 반환 타입은,
                - 상위 클래스의 메서드가 반환하는 타입의 하위 타입일 수 있다.
            - 재정의 메서드 반환 타입 < 상위 클래스 메서드 반환 타입
        - C가 형변환 할 필요가 없음
- `super.clone`의 try-catch
    - `Object::clone` 메서드에서 `CloneNotSupportedException` 선언은 되어 있는 상태
    - `CloneNotSupportedException`은 비검사예외(unexpected exception)였어야 함을 의미

### 복잡한 가변객체 복사 - 1
- `clone` 메서드가 단순히 `super.clone`을 리턴할 경우
    - 내부 객체중 복사가 제대로 이뤄지지 않을 수 있음
- `clone`은 사실상 생성자와 동일한 효과를 낸다
- 개선한 코드 : `clone`의 재귀적 호출
    >```
    >@Override public Stack clone() {
    >    try {
    >        Stack result = (Stack) super.clone();
    >        result.elements = elements.clone(); // private Object[] elements;
    >        return result;
    >    } catch (CloneNotSupportedException e){
    >        throw new AssertionError();
    >    }
    >}
    >```
- 배열과 같은 객체는 제대로 복사가 안이뤄지는 것으로 보임
    - 동일한 배열을 참조한다고 함
- `elements.clone`의 결과를 `Object[]`로 형변환할 필요는 없음
    - 배열의 clone은 타입이 모두 같게 리턴된다.
    - 배열의 복제시에, 배열의 `clone`을 사용, 권장
- `elements`가 `final`이었다면 앞 동작은 작동하지 않음
    - `final`은 새로운 값 할당 불가하기 때문
- `Cloneable` 아키텍처에서의 모순
    - `가변 객체`를 참조하는 필드는 `final`로 선언하라 // 충돌
    - 복제할 수 있는 클래스를 위해, 일부는 `final`을 해제해야 함

### 복잡한 가변객체 복사 - 2
- 해시 테이블용 clone의 문제
- `clone`의 재귀적 호출로는 해결 안되는 문제
- 해시테이블의 구조
    - 버킷들의 배열
    - 버킷
        - key-value의 쌍을 담는 연결 리스트의 첫 엔트리 참조
- 잘못된 clone 예시
    >```
    >@Override public HashTable clone() {
    >    try {
    >        HashTable result = (HashTable) super.clone();
    >        result.buckets = buckets.clone();
    >        return result;
    >    } catch (CloneNotSupportedException e) {
    >        throw new AssertionError();
    >    }
    >}
    >```
- 복제본은 자신만의 버킷 배열을 가지나,
    - 배열의 원본과 같은 연결리스트를 참조할 가능성이 있음 => 문제 발생
- clone 예시
    >```
    >...
    >public class HashTable implements Cloneable{
    >    ...
    >    Entry deepCopy() {
    >        return new Entry(key, value, next == null ? null : next.deepCopy());
    >    }
    >
    >    @Override public HashTable clone() {
    >        try{
    >            HashTable result = (HashTable) super.clone();
    >            result.buckets = new Entry[buckets.length];
    >            for (int i = 0 ; i < buckets.length ; i++)
    >                if (buckets[i] != null)
    >                    result.buckets[i] == buckets[i].deepCopy();
    >            return result;
    >        } catch (CloneNotSupportedException e){
    >            throw new AssertionError();
    >        }
    >    }
    >}
    >...
    >```
- `HashTable::Entry`는 `deep copy`를 지원
- 버킷 배열을 순회하면서, 비어있지 않는 버킷에 대해 `deep copy` 수행
- 버킷이 너무 길지만 않다면 정상 작동
- 연결리스트를 복제하는 방법으로는 좋지 않음
    - 재귀 호출때문에, 리스트의 원소 수만큼
        - 스택 프레임 소비
    - 리스트가 길면 `stack overflow` 발생
- 재귀호출이 아닌 iterator 사용하기
    >```
    >Entry deepCopy() {
    >    Entry result = new Entry(key, value, next);
    >    for(Entry p = result ; p.next != null; p = p.next)
    >        p.next = new Entry(p.next.key, p.next.value, p.next.next);
    >    return result;
    >}
    >```

### 복잡한 가변객체 복사 - 3
- `super.clone`을 호출
- 원본 객체의 상태를 다시 생성하는 고수준 메서드 호출
- HashTable 예시
    - buckets 필드를 새로운 버킷 배열로 초기화
    - 원본 테이블에 담긴 모든 key-value에 대해
        - 복제본 테이블의 `put(key, value)`를 호출하여 채우기
- 고수준 api(`ex) put`) 사용시, 우아한 코드 작성 가능
    - 단, `속도가 느림`
    - `Cloneable`의 `필드 단위 객체 복사`를 `우회`는 방법이기 때문에
        - 전체 `Cloneable` 아키텍쳐와는 어울리지 않는 방식

### 재정의 메서드와 clone
- 생성자에서는 `재정의 될 수 있는 메서드의 호출`이 없어야 함
    - clone도 동일
        - 하위 클래스의 복제 과정에서, 상태 교정을 할 수 없게 됨
            - 복제 상태가 달라질 가능성이 높아짐
- `put(key, value)` 메서드가 `final` 이거나, `private`이어야 함
- `Object::clone`의 경우 `ClassNotSupportedException`을 던진다고 선언하지만,
    - 재정의 메서드의 경우 그렇지 않음
- `public`인 `clone` 메서드는
    - **`throws` 절을 없애야 함**
    - 검사 예외를 던지지 않아야, 메서드 사용시 편하기 때문

### 상속용 클래스와 Cloneable
- 상속해서 쓰기 위한 클래스 설계방식(ITEM.19)에서
    - **`Cloneable`을 구현하면 안됨**
- `Object`를 상속할 때처럼
    - `Cloneable`의 구현 여부를 하위 클래스에서 판단
- 또는, `clone`을 동작하지 않게 구현한 이후
    - 하위 클래스에서 재정의 하지 못하도록 구현
        >```
        >@Override
        >protected final Object clone() throws CloneNotSupportedException {
        >    throw new CloneNotSupportedException();
        >}
        >```

### 안전클래스와 동기화
- `Cloneable`을 구현한 `thread-safe` 클래스 작성시,
    - `clone` 메서드 역시 동기화 필요
- `Object::clone`의 경우 동기화 제공 x
- `super.clone`외에 호출할 일이 없더라도, `clone` 재정의 및 동기화 필요

### 요약
- `Cloneable`을 구현한 모든 클래스는 `clone`을 재정의 해주어야 함
    - access control : `public` 사용
    - return type : `Class 자신`
- `super.clone` 호출 이후, 필드를 적절히 수정
    - 객체 내부 `깊은 구조`에 숨어있는 모든 `가변 객체` 복사
    - 복제본이 가진 `객체 참조` 모두가 `복사된 객체`를 가리키게 끔
- 내부복사는 주로 `clone`을 `재귀 호출` 하지만,
    - 이 방식이 항상 최선이지 않음
- `primitive`, `immutable`의 참조만을 갖는 클래스이라면,
    - 필드 수정이 필요 없음
    - 단, `일련번호`나 `고유 ID`의 경우 수정 필요
- `Cloneable`을 이미 구현한 클래스를 **확장**하려면 `clone`이 잘 작동하도록 구현
    - 그렇지 못하는 상황일 경우
    - `복사 생성자`와 `복사 팩터리` 방법 사용

### 복사 생성자/복사 팩터리
- 복사 생성자
    - 자신과 같은 클래스의 인스턴스를 인수로 받는 생성자
        >```
        >public Yum(Yum yum){ ... };
        >```
- 복사 팩터리
    - 복사 생성자의 정적 팩터리 버전
        >```
        >public static Yum newInstance(Yum yum) { ... };
        >```
- 복사 생성자/팩터리는 `Cloneable/clone` 방식보다 이점이 많음
    - 모호성이 다분한 객체 생성 매커니즘을 사용하지 않음
        - 생성자를 쓰지 않고 객체를 생성하는 방식 x
    - 정상적인 `final` 문법과 충돌하지 않음
    - 불필요한 검사 예외, 형변환이 필요 x
- 해당 클래스가 구현한 `interface` 타입의 인스턴스를 인수로 받을 수 있음
- 관례상, 모든 범용 컬렉션 구현체는
    - `Collenction`이나 `Map` 타입을 받는 생성자 제공
- 변환 생성자 / 변환 팩터리
    - 변환 생성자(conversion constructor), 변환 팩터리(conversion factory)
    - 인터페이스 기반 복사 생성자/복사 팩터리
    - C는 구현 타입에 얽매이지 않고, 복제본의 타입을 결정할 수 있음
        - `HashSet s`의 경우 `TreeSet` 타입으로 복제 가능
        - clone으로는 불가능 하지만, 변환 생성자로 간단히
            - `new TreeSet<>(s)`로 처리 가능

### 결론
- 새로운 인터페이스를 만들 때, 절대 `Cloneable` 확장 X
    - 또한, 새로운 클래스도 이를 구현 X
- `final`의 경우 `Cloneable`을 구현해도 위험이 크지 않으나,
    - `성능 최적화` 관점에서 검토 이후, 드물게 허용
- 복제 원칙
    - 복제 기능은 `생성자`와 `팩터리`를 이용하기
- 배열은 `clone` 메서드로 복사하는게 좋음
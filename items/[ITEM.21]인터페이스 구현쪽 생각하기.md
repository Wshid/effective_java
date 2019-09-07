## [ITEM.21] 인터페이스 구현쪽 생각하기
- `:java7`, 기존 구현체 깨뜨리지 않고,
    - 인터페이스에 메서드 추가 방법이 없엇음
- 인터페이스에 메서드 추가시,
    - 보통은 `compile error` 발생
    - 추가된 메서드가, 우연히 기본 구현체에 존재할 가능성이 낮기 때문
- `java8`, 기존 인터페이스에 **메서드 추가**가 가능해짐

### default method
- default method 선언 시,
    - interface 구현 이후,
    - default 메서드를 정의하지 않은 모든 클래스에서, deafult 구현이 사용 됨
- 자바에서도 기존 인터페이스에 메서드 추가하는 방법 존재
    - 기존 모든 구현체와 완벽히 연동되지 않음
- `:java7`
    - 모든 클래스가
    - 현재의 인터페이스에 새로운 메서드가 추가될 일이 없다고 설계되었기 때문
- `default method`는 구현 클래스에 대해 아무것도 모른 채
    - 합의 없이 무작정 추가되는 것

### Collection interface - default method
- 핵심 `Collection interface`에 다수의 default method가 추가 됨
    - `lambda`를 사용하기 위함
- JPL의 default method는
    - 범용적, 대부분의 상황에서 잘 동작
- **하지만 모든 상황에서 불변식을 해치지 않는 default mehtod는 작성하기 어려움**

### removeIf 메서드
- 이 메서드는 주어진 boolean 함수(predicate)가 `true`를 반환하는 모든 원소 제거
- default 구현
    - 반복자를 이용, 순회
    - 각 원소를 인수로 넣어 `predicate` 호출
    - `true` 반환시, 반복자의 `remove` 메서드 호출, 원소 제거
- 코드 예시(`removeIf`)
    >```
    >default boolean removeIf(Predicate<? super E> filter) {
    >    Objects.requireNonNull(filter);
    >    boolean result = false;
    >    for(Iterator<E> it = iterator(); it.hasNext();) {
    >        if (filter.test(it.next())){
    >            it.remote();
    >            result = true;
    >        }
    >    }
    >    return true;
    >}
    >```
- 범용적이긴 하나, 현존하는 모든 `Collection` 구현체에 적용되진 않음
    - `org.apache.commons.collections4.collection.SynchronizedCollection`
        - `java.util`의 `Collections.synchronizedCollection` static factory method가 반환하는 객체와 동일
        - apache 버전의 경우, c가 제공한 객체로 `Lock`을 거는 기능이 추가 제공됨
        - 모든 메서드에서 주어진 **Lock**객체로 동기화 한 후,
            - 내부 컬렉션 객체에 기능을 위임하는 `Wrapper Class`(ITEM.18)
- `SynchronizedCollection` 클래스, `removeIf` 메서드 재정의 x
    - 이 클래스를 `java8`과 같이 사용시
    - `removeIf`의 default 구현을 물려 받는다면
    - 규약이 깨진다.
    - **모든 메서드의 호출을 알아서 동기화 하지 못함**
        - `removeIf`의 구현은 동기화에 대해 알지 못하므로, `Lock`객체를 사용할 수 없기 때문
    - `SynchronizedCollection` 인스턴스를
        - 여러 스레드가 공유하는 환경에서
        - 한 스레드가 `removeIf` 호출 시
        - `ConcurrentModificationException`이 발생하거나,
            - 예기치 않은 다른 결과 발생 가능성 존재
    - JPL의 회피 방법
        - 구현한 interface의 `default method`를 재정의,
        - 다른 메서드에서 `default method`를 호출하기 전에 필요한 작업 정의
        - `Collections.synchronizedCollection`이 반환하는 `package-private` 클래스
            - `removeIf`를 재정의
            - 이를 호출하는 다른 메서드는
                - `default` 구현을 호출하기 전, 동기화 진행
    - JPL에 속하지 않은 **제3의 기존 컬렉션 구현체**
        - 언어 차원의 interface 변화에 맞춰 수정될 수 없었음
    
    ### default method의 맹점
    - default method는 `compile`에 성공하더라도, 기존 구현체에 `runtime`오류 발생 가능성 존재
    - `java8`은 `Collection 인터페이스`에 많은 default method 추가,
        - 그 결과 기존에 짜여진 많은 자바 코드가 영향 받음
    
    ### default method 주의
    - 기존 인터페이스에 `default method`로 새 메서드 추가하는 일은 피해야 함
        - 꼭 필요한 경우가 아니라면
    - 추가하려는 `default method`가 기존 구현체와 충돌이 없을지도 고려해야 함

    ### default method의 장점
    - interface를 새로 만드는 경우라면
        - 표준적인 메서드 구현을 제공하는데 유용한 수단
    - interface를 더 쉽게 구현할 수 있도록 해줌(ITEM.20)
    - default method는 `interface`로부터
        - **메서드를 제거하거나**
        - **기존 메서드의 signiture를 수정하는 용도는 아님**
        - 기존 c를 망가뜨리는 원인

    ### 결론
    - `default mehotd`라는 도구가 생겼더라도
        - **인터페이스를 설계할 때 주의해야 함**
    - `default method`로 기존 인터페이스에 새로운 내용 추가시,
        - 그만큼의 위험 존재
    - 새로운 인터페이스라면, 그만큼 테스트를 거쳐야 함
        - **서로 다른 방식으로 3가지 이상**
    - 각 인터페이스의 인스턴스를,
        - 다양한 작업에 활용하는 c도 여러개 만들어야 함
    - **인터페이스를 release한 이후에도 결함을 수정하는게 가능할 수도 있으나**
        - **그를 기대하면 안된다.**
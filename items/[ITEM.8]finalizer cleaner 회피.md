## [ITEM.8] finalizer cleaner 회피
### 두 가지 객체 소멸자
- finalizer
    - 예측할 수 없으며, 상황에 따라 위험할 수 있음
    - 기본적으로 **쓰지 말아야** 함
- cleaner
    - 여전히 예측할 수 없음. 일반적으로 불필요

### 왜 쓰지 말아야 할까?
- finalizer와 cleaner 모두 즉시 수행되는 보장이 없음
    - 제 때 실행되어야 하는 작업이 불가능
- 파일 닫기를 실행 시킬 경우
    - 에러가 발생할 확률이 높음
        - sys상에 동시 파일 열기 수가 정해져 있기 때문
- GC 알고리즘에 의해 좌우됨
- 인스턴스의 자원 회수가 제멋대로 지연될 수 있음
- finalizer 스레드는 타 app thread보다 우선순위가 낮아, 실행될 기회가 낮음
    - cleaner의 경우 자신이 수행할 스레드 제어는 가능함
- 수행 시점 및 수행 여부조차 보장하지 않음  
    - 프로그램 life-cycle에 관련없는 작업에서 절대 finalizer/cleaner 의존 금지
        - db와 같은 공유 자원에서 lock 걸어놓고 죽는다면..
- `System.gc`, `System.runFinalization`
    - f/c의 실행 가능성을 높이나, 보장하지는 않음
- finalize 동작 중 발생한 예외
    - 무시, 처리 작업이 남았어도 중단됨
    - error stack 조차 출력하지 않음
    - cleaner는 이 문제는 해결됨
- f/c의 성능문제
    - `AutoCloseable` 생성 및 해제 : `12ns`
        - `try-with-resources`로 종료
    - `finalizer`로 생성 및 해제 : `550ns`
- finalizer 보안 문제
    - 생성자나 직렬화 과정(`readObject`, `readResolve`)에서 예외 발생 시,
    - 생성 되다 만 객체에서 악의적인 하위 클래스의 finalizer 동작
        - 정적 필드에 f의 자신 참조, GC가 수집하지 못할 수 있음
    - 객체 생성을 막으려면 생성자에서 예외 던지면 됨
        - 하지만 finalizer 사용시 불가능
    - `final` 클래스는 하위 클래스 생성 불가 // 안전함
    - `final`이 아닌 클래스, finalizer 공격 방어시,
        - 아무일도 하지 않는 `finalize` 메서드를 만들어 final로 선언해야함

### 대안
- `AutoCloseable`을 구현하고, `close` 메서드 호추
    - 일반적으로 예외 발생하더라도, `try-with-resources` 구문 사용
- 각 인스턴스는 자신이 닫혔는지를 추적하는 것이 좋음
    - `close` 메서드에서 이 객체의 비유효함을 필드에 기록
    - 객체가 닫힌 후 close 호출시 `IllegalStateException` 발생

### f/c의 사용처
- 자원의 소유자가 `close`를 호출하지 않을 상황에 대비(안전망 역할)
    - 즉시 호출은 아니더라도 언젠간.. 이므로
    - `FileInputStream`, `FileOutputStream`, `ThreadPoolExecutor`
- Native peer와 연결된 객체
    - 일반 자바 객체가 native method를 통해 기능을 위임한 native object를 의미
    - `native peer`는 자바 객체가 아니므로, GC는 존재를 모름
    - 이 때 f/c를 사용
        - 단, 성능 저하 감수
        - native peer가 심각한 자원을 가지고 있지 않을때
            - 감수할 수 없다면 `close` 메서드 사용

### 결론
- cleaner(finalize, java :8)는 안전망 역할, 중요하지 않은 네이티브 자원 회수용으로만 사용
- 불확실성, 성능 저하 유의
## [ITEM.01] 생성자 대신 정적 팩터리 메서드
- static factory method
```
public static Boolean valueOf(boolean b){
    return b ? Boolean.TRUE : Boolean.FALSE;
}
```
- `boolean` 을 `Boolean 객체 참조`로 변환
- client에 `public 생성자`이외에 정적 팩터리 메서드 제공 가능

### 정적 팩터리 메서드(Static Factory Method)의 장점
#### 이름을 가진다
- 생성자와의 차이 비교 : 값이 소수인 BigInter의 반환
    - `BigInteger(int, int, Random)`
    - `BigInteger.probablePrime` // 훨씬 직관적
#### 호출될 때마다 인스턴스를 새로 생성하지 x
- `immutable class`를 미리 만들거나
- 새로 생성한 instance를 캐싱하여 사용 가능
- `Boolean.valueOf(boolean)` : 객체를 아예 생성하지 않음
- Flyweight pattern과 유사
- 열거 타입의 경우에도 인스턴스가 하나만 만들어짐을 보장함
#### 반환 타입의 하위 타입 객체를 반환할 수 있다. -> 잘 이해되지 않음
- Java8 이후부터 인터페이스에 정적 메서드 선언 가능
    - 인터페이스는 public 정적 멤버만 허용함 => 제한 사항은 있다는 의미
#### 입력 매개변수에 따라 매번 다른 클래스의 객체 반환 가능
- `EnumSet` : public 생성자 없이 정적 팩터리만 제공
    - 원소의 수가 64개 이하이면 `RegularEnumSet`을, 그 이상이면 `JumboEnumSet` 인스턴스 반환
- 클라이언트는 팩터리에 건너주는 객체의 클래스에 의존되지 않음 // 알 필요도 없음
#### 정적 팩터리 메서드 작성 시점에서, 반환할 객체의 클래스가 존재하지 않아도 된다.
- `Service Provider Framework`의 근간
    - JDBC, ...
    - 3가지 컴포넌트(+1)
        - `서비스 인터페이스(Service Interface)` : 구현체의 동작 정의
            - `Connection(in JDBC)`
        - `제공자 등록 API(Provider Registration API)` : 제공자가 구현체를 등록할 때 사용
            - `DriverManager.registerDriver(in JDBC)`
        - `서비스 접근 API(Service Access API)` : 클라이언트가 서비스의 인스턴스를 얻을 때 사용
            - `DriverManager.get(in JDBC)`
        - `+서비스 제공자 인터페이스(Service Provider Interface)` : 서비스 인터페이스의 인스턴스를 생성하는 팩토리 객체 설명
            - `Driver`
    - C는 SA-API를 사용시 구현체 조건 명시 가능
- 브리지 패턴 - 서비스 제공자 프레임워크 패턴의 변형
    - SA-API는 공급자가 제공하는 것보다  ...

### 정적 패터리 메서드 단점
#### 정적 팩터리 메서드만 제공할 경우, 하위 클래스를 만들수 없음
- 상속을 하려면 public이나 protected 생성자가 필요
- 컬렉션 프레임워크의 유틸리티 구현 클래스는 상속할 수 없음
#### 프로그래머가 찾기 힘들다
- 정적 패터리 메서드의 명명 방식
    - `from` : 매개변수를 받아 해당 타입의 인스턴스 반환
    - `of` : 여러 매개변수를 받아, 적합한 타입의 인스턴스 반환
    - `valudOf` : from과 of의 더 자세한 버전
    - `instance` | `getInstance` : 매개변수로 명시한 인스턴스를 반환, 하지만 같은 인스턴스인지는 보장 x
    - `create` | `newInstance` :
        - instance/getInstance와 동일, 매번 새로운 인스턴스를 생성하여 반환
    - `getType`
        - `getInstace`와 동일, 생성할 클래스가 아닌, 타클래스의 팩터리 메서드 정의할 때 사용
        - `Type`은 팩터리 메서드가 반환할 객체 타입
            - `FileStore fs = Files.getFileStore(path)`
    - `newType`
        - `newInstace`와 동일, 생성할 클래스가 아닌, 타클래스의 팩터리 메서드 정의
        - `Type`은 팩터리 메서드가 반환할 객체 타입
    - `type`
        - getType과 newType의 간결버전
            - `List<Complaint> litany = Collections.list(legacyLitany);`
### 결론?
- 정적 팩터리 메서드와 public 생성자는 각각의 장단점이 있음
- 정적 팩터리 메서드가 유리한 점이 많음
    - 무조건 public 생성자 습관은 개선할 것

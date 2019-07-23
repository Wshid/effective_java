## [ITEM.15] 접근 권한 최소화
- 클래스의 내부 정보를 얼마나 잘 숨겼는가
- 오직 api을 사용하여 타 컴포넌트간 통신이 이루어 지도록 해야함

### 정보은닉의 장점
- 개발속도 향상
    - 컴포넌트 병렬 개발 가능
- 관리비용 저하
    - 컴포넌트 파악이 쉬움
    - 타 컴포넌트 교체 비용 저하
- 성능 최적화에 도움
    - 특정 컴포넌트만 최적화 가능
- 소프트웨어 재사용성 향상
    - 독립적이기 때문
- 큰 시스템 제작 난이도 저하
    - 개별 컴포넌트 테스트 가능

### 접근제어자 사용 원칙
- 모든 클래스와 멤버의 접근성을 좁히기
- public 
    - api 형태 사용
    - 하위호환을 위한 지속적인 관리 필요
- private-protected
    - 패키지 외부에서 사용할 일 없을 때
    - C에 의존적이지 않음
- 한 클래스에서만 사용하는 `package-private` top level 클래스나, 인터페이스
    - 사용하는 클래스에 `private static`으로 중첩시킬 것
    - top level로 두었을 때,
        - 같은 패키지의 모든 클래스가 접근 가능하나,
    - `private static`으로 설정하면
        - 바깥 클래스 하나에서만 접근 가능함
- `public` 필요가 없으면 `package-private` top level 클래스로 좁히기
    - `public` : API 용도
    - `package-private` : 내부 구현

### 접근 제어 4가지
- private
    - 멤버를 선언한 톱레벨 클래스에서만 접근 가능
- package-private
    - 멤버가 소속된 패키지 내 모든 클래스 접근 가능
    - `default`
        - 단, `interface`는 public
- protected
    - `package-private`의 접근 범위 포함
    - 멤버 선언 클래스의 하위 클래스 접근 가능
- public
    - 모든 곳 접근 가능

### 유의사항
- 공개 API를 설계한 후,
    - 그 외에 모든것은 `private`으로 하기
    - 동일 패키지에 다른 클래스 접근시, `package-private` 선언
- 권한 풀어주는 행위가 많을 경우, 컴포넌트 분해 고려
- `private`, `package-private` 멤버는 `공개 API`에 영향을 주지 않음
    - 단, `Serializable`을 구현한 클래스는
        - 의도치 않게 공개 API가 될 수 있음(`ITEM.86,87`)
- `public` 클래스에서 멤버의 접근 수준을
    - `package-private` -> `protected`로 변경시
        - 접근 대상 범위가 매우 넓어짐
- `public` 클래스의 `protected` 멤버는 공개 API
    - 계속 지원되어야 함
    - 내부 동작방식을 외부 공표해야 할 수도 있음
- **`protected` 멤버의 수는 적을수록 좋음**

### 멤버 접근성 방해 제약
- 상위 클래스의 메서드 재정의 시
    - 그 접근 수준을 상위 클래스보다 좁게 설정 불가
- `리스코프 치환원칙`을 따르기 위함
    - 상위 클래스의 인스턴스는
        - 하위 클래스의 인스턴스로 대체 사용 가능해야 함
- 클래스가 인터페이스를 구현하는 것은 이 규칙의 특별한 예
    - 이 클래스는 인터페이스가 정의한 모든 메서드를 `public`으로 선언해야 함

### 테스트 목적으로의 접근 제어 확장?
- 적당한 수준까지는 넓혀도 괜찮음
    - `private` -> `package-private`
    - 그 이상은 허용되지 않음
- 테스트 만을 위해
    - 클래스, 인터페이스, 멤버를 공개 API로 선언해서는 안 됨
- 테스트 코드를 테스트 대상과 동일한 패키지에 두면
    - `package-private`로 처리 가능

### `public`클래스의 인스턴스 필드는 되도록 `public`이 아니어야 한다.
- 필드가 `가변 객체`를 참조하거나,
    - `final`이 아닌 인스턴스 필드를 public으로 선언하면
        - 필드에 담는 값 제한 불가
- 해당 필드와 관련된 모든 것은 `불변식` 보장 불가
- 필드가 수정될 때, 다른 작업이 불가능 하므로
    - public 가변 필드를 갖는 클래스는
        - `thread-safe`하지 않음
- 필드가 `final`이면서 `불변 객체` 참조 시에도 동일한 문제
- 내부 구현을 변경 하고 싶어도
    - `public` 필드를 없애는 방식은 불가
- 이 문제는 `정적 필드`에서도 동일
    - 예외 조건
        - 해당 클래스가 표현하는 `추상 개념`을 완성하는데
            - 필요한 구성요소로써의 상수라면
            - `public static final` 필드로 공개 가능
        - 관례상 이 상수의 이름은 `대문자 알파벳` 및 `_` 사용
        - 이 필드는 반드시 `기본 타입 값`이나 `불변 객체`를 참조 해야함
            - 가변 객체를 참조한다면 `final`이 아닌 필드에 적용되는 모든 불이익이 적용됨
            - 다른 객체를 참조하지는 못하지만,
                - 참조된 객체 자체는 수정 가능하기 때문

### 배열 참조시의 문제
- 길이가 0이 아닌 배열은 모두 변경 가능
    - 클래스에서
        - `public static final` 배열 필드를 두거나
        - 이 필드를 반환하는 접근자 메서드를 제공하지 말 것
    - 그러지 않을 시, C에서 배열 내용 수정이 가능함
    - 보안 허점이 존재하는 코드
        >```
        >public static final Thing[] VALUES = {...};
        >```
    - `private` 배열 필드의 참조를 반환

### 배열 참조 해결 방법
- `public` -> `private`, public 불변 리스트 추가
    >```
    >private static final Thing[] PRIVATE_VALUES = {...};
    >public static final List<Thing> VALUES = 
    >    Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
    >```
- `public` -> `private`, 복사본을 반환하는 public 메서드 추가
    >```
    >private static final Thring[] PRIVATE_VALUES = { ... };
    >public static final Thing[] values() {
    >    return PRIVATE_VAUES.clone();
    >}
    >```
- C의 요구사항에 따라 위 두가지 방법중 하나 선택


### 암묵적 접근 수준
- `Java 9`, module system이라는 개념 도입
- 모듈, 패키지들의 묶음
    - 모듈에 속하는 패키지 중 export 할 것을
        - `module-info.java` 파일에 선언(관례상)
- `protected` 혹은 `public` 멤버라도,
    - 패키지를 공개하지 않았다면, 모듈 외부에서 접근 불가
    - 모듈 내부에서는 `exports`로 선언 여부에 관련 x
- 모듈을 활용하여
    - 클래스를 외부에 공개하지 않으면서도,
    - 모듈을 이루는 패키지 사이에서 공유 가능
- `public` 클래스의 `public, protected` 멤버와 연관됨
    - 암묵적 접근 수준은 각각 `public`, `protected`와 같으나,
        - 그 효과가 모듈 내부로 한정 됨

### 암묵적 접근 수준 주의사항
- 모듈의 `JAR`파일을
    - 자신의 모듈 경로가 아닌
    - `CLASSPATH`에 두면,
    - 패키지는 모듈이 없는 것처럼 행동함
- 모듈의 공개 여부와 상관 없이
    - `public` 클래스가 선언한 `public`, `protected` 멤버를
    - 모듈 밖에서 접근 가능하게 됨
- 이러한 접근권한은 `JDK` 자체가 예시
    - 자바 라이브러리에서 공개하지 않은 패키지는
    - 모듈 밖에서 사용할 수 없기 때문

### 프로그래밍 시의 유의점
- 모듈의 장점을 누리려면
    - 패키지를 모듈 단위로 묶고
    - 모듈 선언에 패키지들의 의존성 명시 필요
    - 소스 트리 재배치
    - 모듈 안으로부터(모듈 시스템을 적용하지 않는) 일반 패키지의 모든 접근에 특별한 조치 필요

### 결론
- 접근성을 최소화 할 것
- 꼭 필요한 것만 `public`으로 선언
- `public`클래스는
    - 상수용 `public static final` 필드 외에, 어떠한 `public`을 가져서는 안됨
- `public static final`이 참조하는 객체가 불변인지 확인할 것
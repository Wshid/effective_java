## [ITEM.22] 인터페이스는 타입 정의 용도
- 인터페이스는
    - 자신을 구현한 클래스의 **인스턴스**를 참조할 수 있는
    - 타입 역할을 한다.
- 클래스가 어떤 인터페이스를 구현한다는 것
    - 자신의 인스턴스로 무엇을 할 수 있는지
    - C에게 표현하는 것

### 상수 인터페이스
- 메서드 x
- `static final` 필드로만 이루어짐 
- 이 상수들을 사용하려는 클래스에서는
    - 정규화된 이름(qualified name)을 피하고자 인터페이스를 구현하고자 함
- 예시
    >```
    >public interface PhysicalConstants {
    >    static final double AVOGADROS_NUMBER = 6.022...;
    >    static final double BOLTZMANN_CONSTNATS = 1.380...;
    >    static final double ELECTRON_MASS = 9.109...;
    >}
    >```
- `상수 인터페이스 안티 패턴` -> `인터페이스`를 잘못 사용한 예
- 클래스 내부에서 사용하는 상수는
    - 외부 인터페이스가 아니라,
    - 내부 구현에 해당 됨
- 상수 인터페이스를 구현하는 것
    - 내부 구현을 클래스의 API로 노출하는 행위
- 클래스가 어떤 상수 인터페이스를 사용하든, 사용자에게 의미가 없어짐

### 상수 인터페이스의 부정적 효과
- 사용자의 혼란
- C의 코드가 내부 구현에 해당하는 상수에 종속 됨
- 다음 release에 상수를 쓰지 않아도,
    - 바이너리 호환성을 위해 상수 인터페이스를 구현해야 함
- final이 아닌 클래스가 상수 인터페이스 구현 시,
    - 하위 클래스의 이름 공간이
    - 인터페이스가 정의한 상수로 오염 된다.
- `java.io.ObjectStreamConstants`
    - JPL에도 잘못 구현된 예가 많음

### 상수 공개 목적시의 사용 방법
- 특정 클래스, 인터페이스에 강하게 연관된 상수일 경우,
    - 해당 클래스나, 인터페이스 자체에 추가해야 함
- 모든 숫자 기본 타입의 `boxing class`가 대표적 예시
    - `Integer`, `Double`에 선언 된
    - `MIN_VALUE`, `MAX_VALUE` 상수
- 열거 타입으로 나타내기 적합하다면,
    - 열거 타입을 만들어 공개(ITEM.34)
- 인스턴스화 할 수 없는 유틸리티 클래스(ITEM.4)에 담아 공개

### 유틸리티 클래스 예시
>```
>package effectivejava.item22.constantutilityclass;
>
>public class PhysicalConstants {
>    private PhysicalConstants() { } // 인스턴스화 방지
>
>    public static final double AVOGADROS_NUMBER = 6.022...;
>    public static final double BOLTZMANN_CONST = 1.380...;
>    public static final double ELECTRON_MASS = 9.109_383_56e-31;
>}
>```
- 숫자에 `_` 붙는 표현
    - java7부터 허용되는 문법
    - 숫자 리터럴 값에는 영향은 없음, 읽기만 편함
    - 고정 소수점, 부동 소수점 등
        - 5자리 이상일 때, 밑줄로 표기하면 편함
    - 십진수 리터럴의 경우도 세자리씩 묶어 표현하면 편함
        - 정수, 부동소수점 상관 없음

### 유틸리티 클래스 사용시 유의점
- 유틸리티 클래스에 정의된 상수를 C에서 사용시,
    - 클래스의 이름까지 함께 명시해야 함
        - `PhysicalConstants.AVOGADROS_NUMBER`
- `static import` 사용시, 그 이름도 생략 가능
```
import static effectivejava...constantutilityclass.PhysicalConstants.*;

public class Test {
    double atoms(double mols) {
        return AVOGADROS_NUMBER * mols;
    }
    ...
}
```

### 결론
- 인터페이스는 **타입을 정의하는 용도**로만 사용해야 함
- 상수 공개용 수단으로 사용하지 말 것
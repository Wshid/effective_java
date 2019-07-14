## [ITEM.10] equals 명시
- equals 메서드는 재정의 하기 쉬움
    - 하지만 따라야할 문제가 많음
- 기본적으로만 비교시, 자기 동일 인스턴스 여부만 확인

### equals 재정의시, 문제가 되는 경우
- 각 인스턴스가 본질적으로 고유
    - 값을 표현 하는것이 아니라, 동작하는 개체를 표현하는 클래스
    - `Thread` 클래스
        - Object.equals가 알맞게 구현
- 논리적 동치성(logically equality) 검사 경우가 x
    - `java.util.regex.Pattern`의 경우 equals를 재정의
        - 두 `Pattern`의 인스턴스가 같은 인스턴스 인지 검사 => 논리적 동치성 검사
            - 같은 정규표현식을 나타내는지 검사
    - 원하는 방식이 아닐경우, `Object.equals`로만 검사가 가능
- 상위 클래스에서 재정의한 equals가 하위 클래스에도 잘 적용될 경우
    - `Set 구현체`
        - `AbstractSet`이 구현한 equals를 상속받아 사용
    - `List 구현체`
        - `AbstractList` 구현 메서드 상속 받음
    - `Map`도 마찬가지
- private class | package-private, equals 메서드 호출할 일이 없음
    - equals를 방지하는 방법
    ```
    @Override public boolean equals(Object o){
        throw new AssertionError(); // 호출 금지!
    }
    ```

### equals를 재정의 해야할 경우
- 객체 식별성(Object Identity)이 아닌 `논리적 동치성` 확인시에 사용
    - 객체 식별성 : 두 객체가 물리적으로 같은지 여부
- 상위 클래스의 equals가 `논리적 동치성`을 비교하도록 재정의되지 않았을 때
    - 주로 `값 클래스`가 해당됨
        - `값 클래스` 여도, 같은 인스턴스가 둘 이상 만들어지지 않는다면 재정의 하지 않아도 됨
        - `Enum`

### equals 메서드 재정의 규칙
- 반사성(reflexivity)
    - if `x != null`, `x.equals(x) == true`
    - 자기 자신과 같아야 함
- 대칭성(symmetry)
    - if `x,y != null`
        - `x.equals(y) == true` then `y.equals(x) == true`
    - 두 객체는 서로에 대해 동치 여부 판단시, 동일한 답을 해야함
- 추이성(transitivity)
    - if `x,y,z != null`
        - `x.equals(y) == true`, `y.equals(z) == true` then `x.equals(z) == true`
- 일관성(consistency)
    - if `x,y != null`,
        - `x.equals(y)` 반복 호출시, 모두 true | false
- not null
    - if `x != null`, `x.equals(null) == false`


### 대칭성 위배
- 대소문자 구별, 잘못된 예시
```
public final class CaseInsensitiveString{
    private final String s;
    
    public CaseInsensitiveString(String s){
        this.s = Objets.requireNonNull(s);
    }

    @Override public boolean equals(Object o){
        if(o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(
                (CaseInsensitiveString) o).s);
            )
        // 한 방향으로만 동작하는 코드
        if (o instanceof String)
            return s.equalsIgnoreCase((String) o);
        return false;
    }
    ...
}
```
- `toString` 메서드는 원본 문자열 대소문자 그대로,
- `equals`에서는 대소문자를 무시함
- 위배 예시
    ```
    CaseInsentiveString cis = new CaseInsentiveString("Polish");
    String s = "polish";

    // 대칭성 위배
    cis.equals(s); // True
    s.equals(cis); // False, String은 CaseInsentiveString을 모르기 때문
    ```

### 대칭성 위배 해결 코드
```
@Override public boolean equals(Object o){
    return o instanceof CaseInsensitiveString &&
        ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
}
```
- `CaseInsensitiveString`과 `String`의 연동성 해제

### 추이성 위배 예시
- 상위 클래스에 없는 새로운 필드
    - 하위 클래스에 추가 하는 상황
    - equals 비교에 영향을 주는 정보라면
- x,y 좌표 비교 예시
```
@Override public boolean equals(Object o){
    if(!(o instanceof Point))
        return false;
    Point p = (Point) o;
    return p.x == x && p.y == y;
}
```
- 좌표 속성에 색상 입히기
```
public class ColorPoint extends Point {
    private final Color color;

    public ColorPoint(int x, int y, Color color){
        super(x,y);
        this.color = color;
    }
    ...
}
```
- equals의 구현
```
@Override public boolean equals(Object o){
    if(!(o instanceof ColorPoint))
        return false;
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```
- 일반 Point와 ColorPoint 비교시, 결과가 다를 수 있음
```
Point p = new Point(1,2);
ColorPoint cp = new ColorPoint(1, 2, Color.RED);

p.equals(cp); // true
cp.equals(p); // false
```
- `ColorPoint.equals`가 `Point`랑 비교시에 Color를 무시한다면?
```
@Override public boolean equals(Object o){
    if(!(o instanceof Point)) return false;

    // o가 Point면 color를 무시하고 색상 비교
    if(!(o instanceof ColorPoint))
        return o.equals(this);

    return super.equals(o) && ((ColorPoint) o).color == color;
}
```
- 대칭성은 지키지만, 추이성이 깨짐
```
ColorPoint p1 = new ColorPoint(1,2,Color.RED);
Point p2 = new Point(1,2);
ColorPoint p3 = new ColorPoint(1,2, Color.BLUE);

p1.equals(p2) // true
p2.equals(p3) // true
p1.equals(p3) // false // 색상을 고려하기 시작했기 때문
```
- 잘못하면 무한 재귀의 문제
    - `StackOverflowError`

### 추이성 문제 해결 방법
- `descrete class`를 확장해 새로운 값을 추가하면서
    - `equals 규약`을 만족시킬 방법은 없음
    - 추상화의 이점을 지키려면..
- 다른 해결 방법?
    - `equals` 내부의 `instanceof` 검사를 `getClass`검사로 바꾸기
    - 값도 추가하면서 `descrete class` 상속도 가능하지 않나?
```
// 리스코프 치환 원칙 위배(Liskov subsititution principle X)
@Override public boolean equals(Object o){
    if (o == null || o.getClass() != getClass())
        return false;
    Point p = (Point) o);
    return p.x == x && p.y == y;
}
```
- 같은 구현의 클래스 객체와 비교할 때만 true를 반환
- 리스코프 치환 원칙
    - 어떤 타입에 있어서 중요한 속성이라면, 그 하위타입에서도 중요하다.
    - 타입의 모든 메서드가 하위 타입에서도 똑같이 동작해야 함

### 우회 방법
- descrete class의 하위 클래스에서 값을 추가할 방법은 없지만..
- 상속 대신 composition을 사용하기
- `Point` 대신 `Point`를 `ColorPoint`의 private 필드로 두고,
    - `ColorPoint`의 같은 위치의 일반 `Point`를 반환하는 `View` 메서드를 `public`으로 추가하기
- ColorPoint
```
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color){
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    // colorpoint의 point view 반환
    public Point asPoint(){
        return point;
    }

    @Override public boolean equals(Object o){ ... }
}
```

### decreate class를 확장해 값을 추가한 클래스
- `java.sql.Timestamp`
    - `java.util.Date`를 확장하여 `nanoseconds` 필드 추가
    - `Timestamp`의 equals는 대칭성 위배
    - `Date` 객체와 한 컬렉션에 넣거나, 섞어 사용하면
        - 비정상동작
    - 사용시 주의 해야 함

### 일관성
- 클래스가 불변이든, 가변이든
    - `equals`의 판단에 신뢰할 수 없는 자원이 개입되면 X
    - `java.net.URL`
        - `equals`
            - 주어진 URL과 매핑된 호스트의 ip 주소를 이용하여 비교
            - 호스트 이름 -> ip주소, 네트워크를 통해야 함
            - 그 결과가 변동될 수 있음
        - 실제 문제가 많이 발생
    - 하위 호환성을 잘 생각해야 함
    - equals는 항시 메모리에 존재하는 객체만을 사용한 `deterministic`한 계산만 수행할 것

### Not null
- 모든 객체는 null과 같지 않아야 함
- 묵시적 null 검사
```
@Override public boolean equals(Object o){
    if(!(o instanceof MyType)){
        return false;
    }
    MyType mt = (MyType) o;
    ...
}
```
- `instanceof`, 첫번째 피연산자가 null이면 `false` 반환


### equals 메서드 구현 방법
- `==`연산자를 사용하여, 입력이 자기 자신의 참조인지 확인
    - 자기 자신과 비교할 경우 `true`
    - 성능 최적화 용도
- **`instanceof` 연산자로, 입력이 올바른 타입인지 확인**
    - 올바른 타입 = `equals`가 정의한 클래스
        - 그 클래스가 구현한 interface일 수 있음
            - `interface`의 경우 자신을 구현한 클래스끼리도 비교 가능하도록 해야 함
                - `equals` 규약 수정 필요
            - `interface`를 구현한 클래스라면, `equals`에서 해당 interface를 사용해야함
                - 클래스가 아닌
            - Set, List, Map, Map.Entry 가 해당
    - 이 조건만 만족하면, 아래 두 조건은 자동적으로 충족
- 입력을 올바른 타입으로 형변환
- 입력 객체와, 자신이 대응되는 `핵심 필드`들이 모두 일치하는지 확인
    - 모든 필드가 일치하면 `true`, 하나라도 다르면 `false`
    - `interface`를 사용했다면,
        - 입력의 필드값을 가져올 때도, 
            - 해당 인터페이스의 메서드를 사용해야 함
    - `class`를 사용했다면,
        - 접근 권한에 따라 직접 필드에 접근 가능

### 유의할 점
- `primitive` type(except `float`, `double`)
    - `==`연산자로 비교
- 참조 타입
    - `equals` 메서드
- `float`, `double`
    - `Float.compare(float, float)`
    - `Double.compare(double, double)`
        - `Float.NaN`, `-0.0f` 과 같은 부동 소수값을 다루어야 하기 때문
    - `Float.equals`, `Double.equals` 메서드를 사용하지 말 것
        - autoboxing이 발생할 수 있음
- 배열
    - 원소 각각을 위 세가지 내용에 따라 비교
    - 모든 원소가 `primitive`하다면, `Array.equals` 메서드 중 하나를 사용함
- null을 정상값 취급할 경우
    - `Object.equals(Object, Object)`로 비교
        - `NullPointerException` 발생 예방
- 복잡한 필드의 클래스 비교 
    - `CaseInsensitiveString`처럼,..
    - 표준형(canonical form)을 저장한 후, 표준형끼리만 비교하도록 하기
    - 불변 클래스에 적합
    - 가변 객체라면, 값이 바뀔 때마다
        - `canonical form`을 업데이트 해주어야 함
- 필드 비교 순서 => 성능
    - 다를 가능성이 크거나
    - 비교하는 비용이 저렴한 필드 먼저 비교
    - 동기화용 lock 필드와 같이
        - `객체의 논리적 상태`와 관련없는 필드 비교 x
    - 파생 필드(핵심 필드로부터 계산 가능한)를 비교하는게 더 빠를 수 있다.
        - 파생 필드가 객체 전체의 상태를 대표하는 경우

### equals 구현 후 검증 작업
- 대칭적인가
- 추이성이 있는가
- 일관성이 있는가
- 단위 테스트
- `반사성`, `Not null`의 경우는 케이스가 많이 않음

### PhoneNumber 클래스 용 equals 메서드
```
public final class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum){
        this.areaCode = rangeCheck(...)
        ...
    }

    private static short rangeCheck(...)

    @Override public boolean equals(Object o) {
        if( o == this )
            return ture;
        if(!(o instanceof PhoneNumber))
            return false;
        PhoneNumber pn = (PhoneNumber) o;
        return pn.lineNum == lineNum 
            && pn.prefix == prefix 
            && pn.areaCode == areaCode;
    }
    ...
}
```

### 주의 사항
- `equals`를 재정의 할 때는 `hashCode` 반드시 재정의 하기
    - [ITEM.11] 참고
- 너무 복잡하게 해결하지 말기
    - 필드의 동치성 검사로만 해결 가능
    - `alias`를 사용한 비교하지 말 것
- `Object`외의 타입을 매개로 받는 `equals` 메서드 선언 금지
    - 잘못된 예
    ```
    // 무조건 입력은 Object이어야 함
    // 무엇이 문제인지 에러가 발생하지 않음(false positive)
    public boolean equals(MyClass o){ ... }
    ```
    - `Object.equals`를 재정의 한것이 아님
        - 단순히 다중정의
    ```
    // 컴파일 에러 발생, 무엇이 문제인지는 확인 가능함
    @Override public boolean equals(MyClass o){ ... }
    ```

### AutoValue Framework
- 구글이 개발
- equals를 작성하고 테스트 하는 툴
- 클래스에 `annotation`만 추가하면, `AutoValue`가 메서드를 알아서 처리
- 대부분의 IDE가 동일 기능을 제공하나 `AutoValue`보다 못하다고 함


### 결론
- 꼭 필요한 경우가 아니면 `equals 재정의`를 하지 말 것
- 재정의 시에는,
    - 클래스의 핵심 필드 모두를 정확히 비교,
    - 다섯가지 규약을 지켜가며 비교할 것
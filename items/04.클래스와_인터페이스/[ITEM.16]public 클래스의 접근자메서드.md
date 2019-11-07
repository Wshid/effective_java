## [ITEM.16] public 클래스의 접근자 메서드
- 클래스를 생성하면
    - private field
    - public getter method 를 사용

### package-private, private
- `public` 클래스와 다르게, 데이터 필드를 노출해도 문제 없음
- 클래스가 표현하려는 추상 개념만 표현하면 됨
- `private` 중첩 클래스의 경우, 수정범위가 좁음
    - 클래스를 포함하는 외부 클래스로까지 제한

### JPL의 접근 제어 규칙 에러
- `public` 클래스의 필드를 직접 노출하지 말아야 하는 규칙
- 어기는 케이스
    - `java.awt.package`의 `Point`, `Dimension` 클래스
- 이 클래스를 따라 설계하지 말 것

### public 클래스의 불변 필드
- 단점이 줄어들긴 하나, 좋은 생각은 아님
- API를 변경하지 않고는 표현방식 변경 불가
- 필드를 읽을 때 부수작업을 수행할 수 없음
- 단, 불변식은 보장 가능
    >```
    >public final class Time {
    >    private static final int HOURS_PER_DAY = 24;
    >    public final int hour;
    >    public final int minute;
    >    ...
    >}
    >```
- 각 인스턴스가 유효한 시간임을 표현함

### 결론
- `public` 클래스는 절대 `가변 필드`를 직접 노출하면 안 됨
- 불변 필드일 경우 노출해도 조금은 안전하나, 안심할 수는 없음
- `package-private` 클래스나, `private` 중첩 클래스는
    - 필드를 노출하는 편이 더 나을 수 있음
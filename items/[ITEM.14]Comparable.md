## [ITEM.14] Comparable

### compareTo method
- `Comparable` Interface의 유일한 메서드
- `Object`의 메서드 X
- `Object::equals`와의 차이
    - 동치성 비교 및 `순서`까지 비교 가능
    - Generic

### Comparable의 구현
- `Comparable`의 구현은
    - 인스턴스들간의 `natural order`가 있음을 의미함
- 따라서 `Comparable`의 구현 되면
    - `Arrays.sort(a);`와 같이 정렬이 가능함
- 검색, 극단값 계산, 자동정렬되는 컬렉션 관리가 가능
- 예제 : 중복 제거후 알파벳 순 정렬
>```
>public class WordList {
>    public static void main(String[] args){
>        Set<String> s = new TreeSet<>();
>        Collections.addAll(s, args);
>        System.out.println(s);
>    }
>}
>```
- `JPL`의 모든 `값 클래스`와 `Enum`은 모두 `Comparable`을 구현함
>```
>public interface Comparable<T>{
>    int compareTo(T t);
>}
>```

### compareTo 메서드 일반 규약
- 이 객체와 주어진 객체의 순서 비교
    - 음의 정수 : 주어진 객체보다 `작을 때`
    - 동일 : 같을 때
    - 양의 정수 : 주어진 객체보다 `클 때`
    - 비교할 수 없을 때 : `ClassCastException`
- sgn(표현식) : 수학에서의 부호 함수
    - 음수, 양수, 0 => -1, 1, 0으로 표현
- 규약(4)
    - `Comparable`을 구현한 클래스는 모든 `x`,`y`에 대해
        - `sgn(x.compreTo(y))` == `-sgn(y.compareTo(x))`
        - `x.compare(y)`가 예외 발생시 그 반대도 성립해야 함
    - 추이성 보장
        - `x.compareTo(y) > y.compareTo(z)`이면
            - `x.compareTo(z) > 0`
    - `Comparable`을 구현한 클래스는 모든 `z`에 대해
        - `x.compareTo(y) == 0`이면
            - `sgn(x.compareTo(z)) == sgn(y.compareTo(z))` 이다.
    - `(x.compareTo(y) == 0) == (x.equals(y))`
        - 필수는 아니지만, 지키는 것은 좋음
        - 지키지 못할경우, 다음과 같이 명시
            - `해당 클래스의 순서는 equals 메서드와 일관되지 않음`

### 유의 사항
- 타입이 다른 객체를 신경쓰지 않아도 됨
    - `ClassCastException`을 발생시킨다.
- 다른 타입의 비교를 허용할 경우는
    - 공통 인터페이스를 매개로 이루어짐
- `compareTo` 규약을 지키지 못하면, 비교를 활용하는 클래스와 같이 사용 불가
- 비교 클래스 예시
    - `TreeSet`, `TreeMap` : 정렬된 컬렉션
    - `Collections`, `Arrays`: 검색과 정렬 알고리즘을 활용하는 유틸 클래스
- `equals`와 동일하게 **반사성**, **대칭성**, **추이성** 을 만족해야 함
- 기존 클래스를 확장한 구체 클래스에서
    - 새로운 값 컴포넌트를 추가했다면, `compareTo` 규약을 지킬수 x
    - 우회방법
        - `Comparable`을 구현한 클래스를 확장해서, 값 컴포넌트를 추가할 때,
            - 확장하는 대신
                - 독립된 클래스 생성
                - 이 클래스에 원래 클래스의 인스턴스를 가리키도록 하는 필드 구성
                - 뷰 메서드 작성
                    - 내부 인스턴스를 반환하는 메서드
            - 바깥 클래스에 `compareTo` 메서드 구현 가능
- `compareTo`의 순서와 `equals`의 결과가 일관되지 않아도, 동작은 하나
    - collection을 사용할 경우 예상치 못할 동작 발생 가능성 존재
        - Collection, Set, Map
            - 내부적으로는 동치성 비교시, `equals`가 아닌 `compareTo`를 사용하기 때문

### `compareTo`와 `equals` 충돌
>```
>new BigDecimal("1.0");
>new BigDecimal("1.00");
>```
- `equals`
    - HashSet이 다르게 형성, 다른 인자로 판단
- `TreeSet`
    - TreeSet에서는 원소를 하나만 갖는다.
    - `compareTo`를 할 경우, 위 두 값은 같게 처리 됨

### 작성 요령
- `Comparable`은 `Type`을 인수로 받는 `Generic Interface`
    - `compareTo` 메서드의 인수 타입은
    - `compile time`에 정해진다.
- 입력 인자의 타입을 확인하거나, 형변환 필요 x
    - 인수의 타입이 잘못되었을 경우, `compile` 자체가 되지 않음
- `null` 인자 => `NullPointerException`
- `객체 참조` 필드 비교시 `compareTo` 메서드를 재귀적으로 호출
    - `Comparable`을 구현하지 않은 필드 | 표준이 아닌 순서로 비교해야 한다면
        - `Comparator`를 대신 사용
            - 직접 만들거나, 자바 구현체를 사용
- 작성 예시
    >```
    >public final class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    >    public int compareTo(CaseInsensitiveString cis) {
    >        return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s);
    >    }
    >}
    >```
    - `CaseInsensitiveString`의 참조는 `CaseInsensitiveString`와만 비교 가능
        - `Comparable`을 구현할 때 일반적으로 따르는 패턴
- `compareTo` 메서드에서 관계 연산자 `<`, `>` 사용하는 방식
    - 오류 발생 가능
    - 추천하지 않음
- 클래스의 핵심 필드가 여러개일 경우,
    - `비교 순서`가 중요함
    - 비교 결과가 0이 아닐경우 종료 됨, 즉시 리턴
        >```
        >public int compareTo(PhoneNumber pn) {
        >    int result = Short.compare(areaCode, pn.areaCode);
        >    if (result == 0) {
        >        result = Short.compare(prefix, pn.prefix);
        >        if(result == 0)
        >            result = Short.compare(lineNum, pn.lineNum);
        >    }
        >    return result;
        >}
        >```
- `JAVA8`에서 `Comparable` Interface가
    - 일련의 `비교자 생성 메서드(comparator construction method)`와
    - 연쇄적으로 비교자 생성 가능
    - 우아하게 코딩을 할 수 있으나,
        - 약간의 성능 저하 존재(`-10%`)
    >```
    >private static final Comparator<PhoneNumer> COMPARATOR = 
    >    comparingInt((PhoneNumber pn) -> pn.areaCode)
    >        .thenComparingInt(pn -> pn.prefix)
    >        .thenComparingInt(pn -> pn.lineNum);
    >
    >public int compareTo(PhoneNumber pn) {
    >    return COMPARATOR.compare(this, pn);
    >}
    >```
    - `static import` 사용시, 이름만으로 바로 호출 가능
    - 위 클래스 초기화 시, 비교자 생성 메서드 2개를 이용
    - `comparingInt`
        - key extractor function을 인수로 받음    
            - 객체 참조 --매핑--> `int` type key
        - 키를 기준으로 순서를 정하는 비교자를 반환, 정적 메서드
        - lambda를 인수로 받아,
            - areaCode를 기준으로 정렬한 `Comparator<PhoneNumber>` 반환
        - `(PhoneNumber pn)`으로 캐스팅 해주어야 함
    - `thenComparingInt`
        - `Comparator`의 인스턴스 메서드
        - input : int 키 추출자 함수
        - return : 비교자 
        - 연달아 호출이 가능함
        - 캐스팅을 안해준 이유는, 이정도는 추론 가능하기 때문

### Comparator의 보조 생성 메서드
- `long`, `double`
    - `compareInt`, `thenComparingInt`의 변형 메서드 존재
- `short`
    - `int`용 버전을 사용하면 됨
- `float`
    - `double`용을 이용하여 수행
- 객체 참조용 비교자 생성 메서드 존재
    - `comparing` 이라는 static method 2개가 다중정의 됨
        - way1 : `(키 추출자)`를 받아, 키의 순서를 이용함
        - way2 : `(키 추출자, 추출할 키를 비교할 비교자)`를 받아 사용
    - `thenComparing` 인스턴스 메서드, 3개 다중정의
        - way1 : `(비교자)`, 비교자로부터 순서 비교
        - way2 : `(키 추출자)`, 키의 자연적 순서로 비교
        - way3 : `(키 추출자, 비교자)`
- `값의 차`를 비교하여 비교하는 예시 => **추이성 위배**
    >```
    >static Comparator<Object> hashCodeOrder = new Comparator<>() {
    >    public int compare(Object o1, Object o2){
    >        return o1.hashCode() - o2.hashCode;
    >    }
    >}
    >```
- 대안 1 : `static compare`를 활용한 비교자
    >```
    >static Comparator<Object> hashCodeOrder = new Comparator<>() {
    >    public int comapre(Object o1, Object o2){
    >        return Integer.compare(o1.hashCode(), o2.hashCode());
    >    }
    >}
    >```
- 대안 2 : `비교자 생성 메서드`를 활용한 비교자
    >```
    >static Comparator<Object> hashCodeOrder = 
    >    Comparator.comparingInt(o -> o.hashCode());
    >```

### 결론
- 순서를 고려해야 하는 `값 클래스` 작성시,
    - `Comparable` interface를 구현하기
        - 인스턴스 쉽게 정렬, 검색, 비교 가능하는 `Collection`을 사용 가능하도록
- `compareTo`에서 `<`나 `>`와 같은 비교연산자 사용 x
- 비교연산자 대신에,
    - boxing된 기본 클래스가 제공하는 `static compare method`
    - `Comparator` interface가 제공하는 `비교자 생성 메서드`를 사용할 것
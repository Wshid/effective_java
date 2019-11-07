## [ITEM.06] 불필요 객체 생성 회피
- 동일한 기능의 객체를 매번 생성 보다는 **객체 하나 재사용**

### String 예시
>```
>String s = new String("aaa"); // 매번마다 생성
>String s = "aaa"; // 하나의 인스턴스를 공유
>```

### 생성자와 팩터리 메서드 차이
- 생성자는 호출할 때마다 새로운 객체
- 팩터리 메서드는 아님

### 비싼 객체
- 생성 비용이 **비싼 객체**의 경우 캐싱하여 재사용하는 것을 권장
    - 물론 비싼 객체인지 판별하기는 어려움
>```
>static boolean isRomanNumeral(String s){
>    return s.matches("정규표현식!")
>}
>```
- `String.matches`
    - 정규표현식을 사용하여 문자열 형태를 확인하는 쉬운 방법
    - 성능이 중요할경우, 반복 사용하기엔 적합 X
    - 내부적으로 만드는 `Pattern` instance의 경우
        - 한번 쓰고 버려져, 바로 GC 대상이 됨
- `Pattern`
    - 입력받은 regex에 대한 유한 상태 머신(finite state machine)을 만든다
        - 인스턴스 생성 비용이 높음
- 개선하는 방법?
    - `Pattern` 인스턴스를 클래스 초기화(정적 초기화) 과정에서 캐싱, 이후 재사용
        - Pattern instance는 immutable 하다
    >```
    >public class RomanNumerals{
    >    private static final Pattern ROMAN = Pattern.compile("정규표현식!");
    >
    >    static boolean isRomanNumeral(String s){
    >        return ROMAN.matcher(s).matches();
    >    }
    >}
    >```
    - `isRomanNumeral`이 반복 호출 될 경우, 성능 향상이 일어남
    - `isRomanNumeral`을 한번도 초기화 하지 않는다면 괜히 사용한 꼴
        - `lazy initialization`을 통해 극복을 할 수 있으나, 권하지 않음
            - 코드가 복잡해지지만, 성능 개선이 잘 일어나지 않기 때문

### 어댑터(adapter)
- View라고도 부름
- 실제 작업은 뒷단에, 어댑터는 인터페이스 역할
- 어댑터는 뒷단 객체만 관리하면 됨
- 뒷단 객체 하나당 어댑터 하나만 만들어주면 된다.

### Map의 예시
- `Map` interface의 `KeySet` 메서드
    - Map 객체 안의 키 전부를 담은 `Set`을 리턴한다.
- KeySet을 호출할때마다 같은 Set 인스턴스 반환?
    - 반환된 Set이 가변이더라도, 반환된 인스턴스 자체의 기능은 모두 같기 때문
    - 반환한 객체 중 하나 수정시, 다른 모든 객체가 같이 바뀜
    - 여러개 관리할 필요가 없음
        - 똑같은 Map instance 대변하기 때문

### 불필요한 객체 반환, Auto Boxing
- primitive type과 boxing type을 섞어 쓸때 상호 변환해주는 기술
- 기본 타입과 그에 대응하는 박싱 타입을 유사 처리 해주지만, 없애는 건 아님
- 의미는 같으나, 성능에서는 같지 않음
>```
>private static long sum(){
>    Long sum = 0L;
>    for(long i = 0; i <= Integer.MAX_VALUE; i++)
>        sum += i;
>    
>    return sum;
>}
>```
- `long`이 아닌 `Long`으로 선언
    - 불필요한 `Long`인스턴스 약 231개 생성
    - long 타입인 i가 Long 타입인 sum에 더해질 때마다 생성
- 박싱된 기본 타입보다는 **기본 타입**을 사용하고,
    - 의도치 않은 오토박싱이 숨어들지 않도록 주의

### 객체 생성을 피하라?
- 그 의미는 아님
- 프로그램의 명확성, 간결성, 기능을 위해 객체 추가는 필요한 일
- JVM에서 미사용 객체 회수 작업은 그리 큰 작업은 아니기 때문

### 잘못된 객체 풀의 생성
- 아주 무거운 객체가 아닌 가벼운 객체를 관리하는 객체 풀 생성 -> X
- db 연결과 같은 무거운 작업에만 object pool을 사용
- 오히려 가독성이 떨어지고, 메모리 증가 및 성능 저하 유발
- 가벼운 객체를 다룰때는 jvm GC가 더 빠름

### 방어적 복사(defensive copy)
- 이와는 대조적인 내용
- 새로운 객체를 만들어야 한다면, 기존 객체를 사용하기
- 피해 정도
    - `방어적 복사가 필요한 상황에서의 재사용 피해` > `필요 없는 객체 반복 생성`
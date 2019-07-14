## [ITEM.11] hashCode 재정의
- `equals`를 재정의한 클래스 모두에서
    - `hashCode`도 재정의 해주어야 함

### hashcode도 재정의 해야 하는 이유
- hashCode 일반 규약 때문
    - `HashMap`이나 `HashSet`의 컬렉션 원소로 비교시 문제 발생

### 규약 in Object 명세
- `equals` 비교에 사용되는 정보가 변경되지 않았다면,
    - app이 실행되는 동안
        - 그 객체의 `hashCode` 메서드는
            - 몇번을 호출해도, 동일한 값을 반환해야 함
    - app 재실행 이후에는 상관 없음
- `equals(Object)`가 두 객체를 같다 판단시,
    - 두 객체의 `hashCode`도 동일한 값 반환
    - `hashCode` 재정의를 잘 못했을때, 크게 문제가 되는 부분
    - `논리적으로 같은 객체`는, `같은 해시코드`를 반환해야 함
- `equals(Object)`가 두 객체를 다르다 판단시,
    - `hashCode`가 동일할 필요는 없음
    - 단, `hashCode`도 다른 값을 반환하면, hashTable의 성능이 좋아짐

### hashCode 재정의 문제
- 물리적으로 다른 두 객체는, 논리적으로 같을 수 있음
- `Object`의 기본 hashCode의 경우, 이 둘이 전혀 다르다고 판단할 수 있음

### 최악의 hashCode 구현
```
@Override public int hashCode() { return 42; }
```
- 동치인 모든 객체에서 동일한 해시코드 반환
- 모든 객체에 대해 동일한 값
    - 하나의 버킷, 연결리스트 형태
- 평균 수행시간이 `O(n)`으로 느려짐

### 이상적인 해시 함수?
- 서로 다른 instance 들을
    - 32비트 정수 범위에 균일하게 분배

### 좋은 hashCode 작성법
1. int 변수 `result`를 선언한 후, `c`로 초기화
    - `c` : 해당 객체의 첫번째 핵심 필드를 `2.a`로 계산한 방식
        - 핵심 필드 : equals 비교에 사용되는 필드
2. 해당 객체에 `나머지 핵심 필드`, `f` 각각에 대해 다음 작업 수행
    - a. 해당 필드의 해시코드 `c` 계산
        - 기본 타입 필드일 경우 -> `Type.hashCode(f)`
            - `Type`은 기본 타입의 박싱 클래스를 의미
        - 참조 타입 필드 &&
            - 클래스의 `equals` 메서드가 이 필드의 equals를 재귀적으로 호출 및 비교시,
                - 이 필드의 hashCode를 재귀적으로 호출
            - 계산이 복잡할 경우,
                - 필드의 표준형을 만들어, 표준형의 `hashCode`를 호출함
                - 필드의 값이 null이면 0을 사용함
        - 필드가 배열일 경우,
            - 핵심 원소 각각을 별도의 필드처럼 다룸
            - 위 규칙을 재귀적으로 적용해, 각 핵심원소의 해시코드를 적용한 뒤, `2.b` 방식으로 갱신
            - 배열에 핵심 원소가 하나도 없다면 단순 상수(0 추천)
            - 배열에 모든 원소가 핵심 원소라면 `Arrays.hashCode` 사용
    - b. `2.a`에서 계산한 해시코드 `c`로 result 갱신
        - `result = 31 * result + c;`
3. result 반환

### 방법 및 원리
- 메서드가 동치인 인스턴스에 대해 똑같은 해시코드를 반환하는가
- 단위 테스트 작성
    - `equals`와 `hashCode` 메서드를 `AutoValue`로 생성시, 건너 뜀
- 동치인 인스턴스가 다른 해시코드 반환시, 원인 파악 필요
- 파생 필드의 경우, 해시코드 계산에서 제외
- `equals` 비교에 사용되지 않은 필드는 `반드시` 제외해야 함
- `2.b`에서 `31 * result`는 필드 곱하는 순서에 따라
    - `result`가 달라짐
    - 클래스에 비슷한 필드가 여럿일 때, 해시효과가 높아짐
- `31`인 이유?
    - 소수(prime)이면서, 홀수이기 때문
        - 짝수고 overflow가 발생한다면 정보를 잃는다.
    - 연산 최적화
        - `(31 * i)` == `(i<<5) - i`

### 예시
```
@Override public int hashCode(){
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```
- 비결정적(undeterministic)인 요소가 없음
    - 동치 PhoneNumber 인스턴스는 같은 hashCode를 가진다.

### 해시 충돌이 더 적은 방법
- `Guava`의 `com.google.common.hash.Hashing`

### Object.hash
- 임의의 개수만큼 객체를 받아 hashCode를 구함
- 속도는 위 방법보다는 느림
- 성능이 민감하지 않는 상황에서만 사용
```
@Override public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

### 유의 사항
- 클래스가 불변이고, hashCode 계산 비용이 클 경우, 
    - 새로 계산보다는 `캐싱`을 사용할 것
- 타입의 객체가 주로 해시의 키로 들어갈 경우,
    - 인스턴스가 만들어질 때 해시코드를 계산해 주어야 함
- `lazy initialization`
    - 해시의 키로 사용되지 않을경우
    - hashCode가 처음 호출될 때 계산하는 방법
    - 스레드를 안전하게 만들어야 함
    ```
    private int hashCode;

    @Override public int hashCode() {
        int result = hashCode;
        if (result == 0){
            result = Short.hashCode(areaCode);
            result =  31 * result + Short.hashCode(prefix);
            result = 31 * result + Short.hashCode(lineNum);
            hashCode = result;
        }
        return result;
    }
    ```
    - hashCode의 필드의 초기값은, 흔히 생성되는 객체의 해시코드와는 달라야 함
- 해시코드를 계산할 때, 핵심필드를 생략하면 안됨
    - 해시 품질이 나빠져, 해시테이블의 성능 저하가 심함
- hashCode가 반환하는 값의 생성 규칙을, `API 사용자`에게 자세히 공표하지 말아야 함
    - 그래야 C가 이 값에 의지 하지 않음
    - 추후에 계산 방식 변경이 가능함

### 결론
- equals 재정의 시, hashCode도 재정의 해야 함
- 재정의한 hashCode는 `Object`의 API문서 기술된 내용을 따라야 함
- 서로 다른 인스턴스라면, 해시코드도 다르게 구현되어야 함
- `AutoValue`를 사용한다면 자동 해결됨
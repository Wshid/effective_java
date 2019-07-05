## [ITEM.7] 다 쓴 객체 참조 해제

### 문제가 있는 코드
- GC가 있다고 해서 신경 안쓰면 안됨
    - 관리하지 않으면 OOM 발생
- 간단한 스택 프로그램
```
public Stack(){
    elements = new Object[16];
}

public Object pop(){
    if(size == 0){
        throw new EmptyStackException();
    }
    return elements[--size];
}

private void ensureCapacity(){
    if(elements.length == size){
        elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```
- 스택을 증가 시키고 줄일때, 스택에서 꺼내진 객체를 GC가 회수하지 않음
    - 다 쓴 참조(obsolete reference)를 가지기 때문
        - 영원히 참조하지 않을 참조
- 이 코드에서는 `elements` 배열의 외부 영역을 의미함
    - 활성 영역 : size보다 작은 인덱스
    - 활성 영역 밖 : size보다 큰 인덱스를 말한다.
- GC가 회수하지 못한 객체는
    - 그를 참조하는 모든 객체가 회수해가지 못함
    - 전체적으로 성능에 악영향

### 해결방법
- `null` 사용하기
- `pop` 메서드의 개선
```
public Object pop(){
    if (size==0){
        throw new EmptyStackException();
    }
    Object result = elments[--size];
    elements[size] = null; // 다 쓴 객체를 해제하기
    return result;
}
```
- `null`로 초기화
    - 만약 실수로 `null`처리된 부분을 사용할 시 `NullPointException` 처리가 가능함


### 무조건 null 처리는
- 너무 많이 사용하면 코드가 더럽혀짐
- 객체 참조를 null 처리하는 경우는 예외 상황이어야 함
- 가장 좋은 방법?
    - `obsolete object`를 유효 범위(scope) 밖으로 밀어내기

### Stack 코드의 취약점
- Stack 코드가 메모리 누수에 취약했던 이유?
    - 스택이 자기 메모리 관리를 직접 하기 때문
    - elements 배열로 관리
        - 활성 영역은 잘 사용되나,
        - 비활성 영역은 GC가 알 수 없음
- 일반적으로 **자기 메모리를 직접 관리하는 클래스**는
    - **메모리 누수**에 유의해야함
    - 원소를 다 사용했을 경우, 즉시 null 처리

### 캐시 관리
- **캐시**도 메모리 누수를 일으킴
    - 캐시 외부에서 key를 참조하는 동안만 엔트리가 캐시를 필요로 하는 상황일 때,
        - `WeakHashMap`을 사용하여 캐시 구성
            - 다 쓴 엔트리는 즉시 제거 됨
- 캐시 만들때
    - 엔트리의 유효기간을 정확히 정의하기 위해
        - 시간이 지날수록 **엔트리의 가치를 떨어뜨림**
        - 쓰지 않는 엔트리를 청소하기
    - `ScheduledThreadPoolExecutor` 같은 백그라운드 스레드 활용 하거나
    - 캐시에 새 엔트리를 추가할 때 부수작업 처리
        - `LinkedHashMap`의 `removeEldestEntry` 메서드를 사용
    - 복잡한 캐시를 만들때는 `java.lang.ref`를 활용하면 됨

### 리스너와 콜백, 누수
- 리스너, 콜백도 누수를 일으킴
- C가 callback 등록만 하고 해제를 하지 않으면, 콜백은 계속 쌓여감
    - 이 때 `weak reference`로 지정시, GC가 가져갈 수 있음
    - `WeakHashMap`에 키로 저장하는 등의 방법

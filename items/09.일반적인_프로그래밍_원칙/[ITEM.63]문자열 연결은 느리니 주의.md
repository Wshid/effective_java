## [ITEM.63] 문자열 연결은 느리니 주의
### 문자열 연결 연산자
- `+`로 문자열을 연결할 수 있음
- **한줄짜리 출력**또는 **작고 크기가 고정된 객체의 문자열 표현**은 상관 없으나,
- 그 외의 경우에 **성능저하**가 발생
- 문자열 연결 연산자로
  - 문자열 `n`개를 잇는 시간은 `n^2`에 비례
- 문자열은 **불변**(ITEM.17)이기 때문에,
  - 두 문자열을 연결할 경우
  - 양쪽의 내용을 모두 **복사**해야 하기 때문

### 문자열 연결을 잘못 사용한 예시
- 코드
  ```java
  public String statement() {
    String result = "";
    for(int i = 0; i < numItems(); i++)
      result += lineForItem(i); // 문자열 연결
    return result;
  }
  ```
- `item`이 많을 경우 심각하게 느려질 위험성 존재

### 문자열 연결을 바르게 사용한 예시
- `String` 대신 `StringBuilder`를 사용
- 코드
  ```java
  public String statement2() {
    StringBuilder b = new StringBuilder(numItems() * LINE_WIDTH);
    for (int i = 0; i < numItems(); i++)
      b.append(lineForItem(i));
    return b.toString();
  }
  ```

### 두 예시의 성능 비교
- `java 6:`, 문자열 연결 성능을 많이 개선하였으나, 여전히 메서드 성능차이는 큼
- `item(n) = 100, lineForItem.length = 80` 상황일 때
  - `statement2`가 6.5배 빠르다.
- `statement`의 수행시간은
  - `item`의 **제곱**에 비례
- `statement2`는
  - `item`에 **선형**으로 증가
- `statement2`에서
  - 전체 결과를 담기에 충분한 **크기**로 **초기화**
  - 기본값을 줘도 빠르긴 하나, 크기 지정시 더 좋은 효과 발생


### 결론
- 많은 문자열을 연결할 때 **문자열 연결 연산자**(+)를 사용하지 말 것
- 대신 `StringBuilder::append`를 사용
- **문자 배열**을 사용하거나,
  - 문자열을 연결하지 않고 **하나씩**처리하는 대안도 존재
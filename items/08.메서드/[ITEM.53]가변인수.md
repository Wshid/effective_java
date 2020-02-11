## [ITEM.53] 가변인수
### 가변 인수 메서드
- `varargs` 메서드는
  - 명시한 타입의 인수를 **0**개 이상 받을 수 있음
- 가변인수 메서드를 호출하면
  - 가장 먼저 **인수의 개수**와 길이가 같은 **배열**을 만들고
  - **인수**들을 이 **배열**에 저장하여, 가변인수 메서드에 건넨다.

### 예시
- int 인수들의 합을 계산하는 메서드
  ```java
  static int sum(int... args) {
    int sum = 0;
    for (int arg : args)
      sum += arg;
    return sum;
  }
  ```
- 인수가 1개 이상일 때
  - 최소값 등을 구할 때, 최소 원소가 1개 이상이어야 함
  - **인수 개수**는 **run time**에 자동 생성된 **배열의 길이**로 알 수 있음
  ```java
  static int min(int... args) {
    if(args.length == 0)
      throw new IllegalArgumentException("인수가 1개 이상 필요합니다");
    int min = args[0];
    for(int i=1; i<args.length; i++)
      if (args[i] < min)
        min = args[i];
    return min;
  }
  ```

### 예시의 문제
- 인수를 0개만 넣어 호출하면
  - **compile time**이 아닌 **run time**에 실패
  - 코드의 경우도 지저분함
- `args` 유효성 검사를 명시적으로 해야 함
- `min`의 초기값을 `Integer.MAX_VALUE`로 설정하지 않고는
  - `for-each` 구문을 사용할 수 없음

### 예시 개선
- 매개변수를 2개 받도록 하는 방법
- 첫번째는 **일반 매개변수**, 두번째는 **가변 매개 변수**
- 코드
  ```java
  static int min(int firstArg, int... remainingArgs) {
    int nim = firstArg;
    for (int arg : remainArgs)
      if(arg < min)
        min = arg;
    return min;
  }
  ```

### 가변인수의 사용처
- **인수 개수**가 정해지지 않았을 때 유용
- `printf`와 가변인수는, 한 묶음으로 java에 도입 됨
  - 이 때, 핵심 리플렉션(ITEM.65)도 재정비 됨
- `printf`와 **리플렉션** 모두 **가변인수**에 영향 받음

### 성능적인 문제
- 가변인수가 문제가 될 수 있음
- **가변 인수 메서드**는 호출될 때마다
  - **배열**을 새로 생성하고 **초기화**

### 가변인수의 유연성이 필요할 때 사용하는 패턴
- 사례
  - 해당 메서드 호출의 95%가 인수를 3개 이하로 사용한다
  - 인수가 0개인것 부터, 4개인것 까지 5개를 다중정의 하는 상황
  - 코드
    ```java
    public void foo() { }
    public void foo(int a1) { }
    public void foo(int a1, int a2) { }
    public void foo(int a1, int a2, int a3) { }
    public void foo(int a1, int a2, int a3, int... rest) { } 
    ```
  - 마지막 **다중정의 메서드**가 인수 4개 이상인, 5%의 호출을 견딘다.
  - 메서드 호출 중 단 5%만이 배열 생성
  - 대부분의 **성능 최적화**와 마찬가지로
    - 보통때는 별 이득이 없으나, 필요한 특수상황에서 이점
- `EnumSet`의 **정적 팩터리**도
  - 위와 같인 기법을 사용하여
  - **열거 타입 집합 생성 비용**을 최소화 한다
- `EnumSet`은 비트 필드(ITEM.36)을 대체하면서, **성능**을 유지해야 하기 때문


### 결론
- **인수 개수**가 일정하지 않은 **메서드**를 정의할 때, **가변인수**가 반드시 필요
- 이 메서드를 정의할 때
  - **필수 매개변수**는 **가변 인수**앞에 두고
  - **가변인수**사용시, **성능 문제**까지 고려해야 함
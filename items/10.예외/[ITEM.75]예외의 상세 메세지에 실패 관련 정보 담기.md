## [ITEM.75] 예외의 상세 메세지에 실패 관련 정보를 담기

### stack strace
- 예외를 잡지 못해 프로그램이 실패할 경우 발생
- 스택 추적은
  - 예외 객체의 `toString`메서드를 호출해 얻는 문자열
- 보통은 **예외의 클래스 명**뒤에 상세 메세지가 붙는 형태
- 이 정보가 다음과 같은 사람이 얻을 수 있는 유일한 정보
  - 실패 원인을 분석해야 하는 프로그래머
  - 사이트 신뢰성 엔지니어(SRE, Site Reliability Engineer)
- 더군다나, 해당 실패를 재현하기 어렵다면
  - 더 자세한 정보를 얻기 어렵거나 불가능
- 예외의 `toString`메서드에 **실패 원인**에 관한 정보를
  - 가능한 한 많이 담아 반환하는 일은 아주 중요함
- 사후 분석을 위해 **실패 순간의 상황**을 정확히 포착해
  - 예외 상세 메세지에 담아야 함

### 실패 순간 포착
- 실패 순간을 포착하려면
  - 발생한 예외에 관여된 **모든 매개변수**와 **필드의 값**을 실패 메세지에 담아야 함
- `IndexOutOfBoundsException`의 상세 메세지는
  - 범위의 **최소값/최대값**, 그 범위를 벗어낫다는 **인덱스 값**을 담아야 함
  - 위 정보는 실패에 대한 많은 것을 알려줌
- 단, 보안과 관련한 정보는 주의해서 다룰 것
  - 문제를 진단하고 해결화는 과정에서, 스택 추적 정보를 많은 사람들이 볼 수 있음
  - 상세 메세지에 **비밀번호**나 **암호 키**같은 정보까지 담아서는 안 됨
- 관련 정보를 담을 때, 관련 데이터를 모두 담아야 하나, 장황할 필요는 없음
  - 문제를 분석하는 사람은 **스택 추적**뿐 아니라, 관련 문서 및 소스코드를 함께 살펴 봄
  - 스택 추적에는, 예외가 발생한 파일 이름 / 줄번호, 스택에서 호출한 다른 메서드 등이 기록되어 있는게 보통
  - 문서와 소스코드에서 얻을 수 있는 정보는 길게 작성해야 의미 없음

### 예외의 상세 메세지 vs 오류 메세지
- 예외의 상세 메세지와, 최종 사용자에게 보여줄 **오류 메세지**를 혼동해서는 안 됨
- 최종 사용자에게는 친절한 안내 메세지를 보여줘야 하지만
  - 예외 메세지는 가독성 보다 **담긴 내용**이 훨씬 중요
- **예외 메세지**의 주 소비층은
  - 문제를 분석해야할 프로그래머와 SRE 엔지니어 이기 때문
- 또한 최종 사용자용 메세지는 현지어로 번역해주기도 하지만
  - 에러 메세지는 그럴 일이 없음

### 필요한 정보를 예외 생성자에서 받기
- 실패를 적절히 포착하려면
  - 필요한 정보를 **예외 생성자**에서 모두 받아서
  - 상세 메세지까지 미리 생성해놓는 방법도 좋음
- 현재의 `IndexOutOFBoundsException` 생성자는 `String`을 받지만
  - 다음과 같이 구현해도 좋음
- 예시 코드
  ```java
  /**
    * IndexOutOfBoundsException을 생성
    * @param  lowerBound 인덱스의 최솟값
    * @param  upperBound 인덱스의 최댓값 + 1
    * @param  index 인덱스의 실제값
    * */
  public IndexOutOfBoundsException(int lowerBound, int upperBound, int index) {
    // 실패 포착하는 상세 메세지 생성
    super(String.format(
      "min : %d, max : %d, index : %d", lowerBound, upperBound, index
    ));

    // 프로그램에서 이용할 수 있도록 실패 정보 저장
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.index = index;
  }
  ```
- `java 9`에서는 `IndexOutOfBoundsException`에 **정수 인덱스**값을 받는 생성자가 추가됨
  - 하지만 **최소값**과 **최댓값**을 받지는 않음
- 위 예시코드 처럼 작성할 경우
  - 프로그래머가 던지는 예뇌는 자연스럽게 실패를 잘 포착함
- 고품질의 **상세 메세지**를 만들어내는 코드를
  - **예외 클래스 안**으로 모아주는 효과 존재
  - 클래스 사용자가 **메시지를 만드는 작업**을 중복하지 않아도 됨

### 예외와 접근자 메서드
- **ITEM.70**에서 언급한 내용
- **예외**는 실패와 관련한 정보를 얻을 수 있는
  - **접근자 메서드**를 적절히 제공하는 것이 좋음
    - 위 예시에서는 `lowerBound, upperBound, index`가 적당함
- 포착한 실패 정보는
  - **예외 상황 복구**시에 유용할 수 있으므로
  - **접근자 메서드**는 비검사 예외보다는 **검사 예외**에서 더 중요함
- **비검사 예외**의 상세 정보에
  - 프로그램적으로 접근하길 원하는 프로그래머는 드물 것
- 하지만
  - `toString이 반환한 값에 포함된 정보를 얻어올 수 있는 API를 제공하자`라는 일반 원칙을 따르는 관점에서
  - **비검사 예외**라도 상세 정보를 알려주는 **접근자 메서드**를 제공하라고 권할 수 있음
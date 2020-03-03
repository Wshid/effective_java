## [ITEM.56] 공개된 API요소와 문서화 주석
- API를 쓸모있게 하려면, 작성된 문서도 있어야 함

### javadoc
- 전통적으로 API문서는, 사람이 작성하기 때문에
  - 코드 변경시, 직접 수정해주어야 함
- `javadoc`이라는 유틸리티가 해당 작업을 도와줌
- javadoc은
  - **소스코드 파일**에서
    - **문서화 주석(doc comment)** 라는 특수한 형태로 기술된 설명을 API 문서로 변환

### 문서화 주석을 작성하는 방법
- 공식 언어 명세에 속하지는 않지만,
  - 비공식적으로 업계 표준 API로 사용됨
- 해당 규칙은 **문서화 주석 작성법(How to Write Doc Comments)** 웹페이지에 기술 됨
  - `java 4:`로 갱신되지 않는 페이지지만, 가치는 존재함
- 자바 버전이 올라가면서, 추가된 중요한 javadoc 태그
  - `java 5` : `@literal`, `@code`
  - `java 8` : `@implSpec`
  - `java 9` : `@index`

### API 문서화 가이드
- API를 올바르게 문서화 하려면
  - 공개된 **모든 클래스**, **인터페이스**, **메서드**, **필드** 선언에
  - **문서화 주석**을 달아야 함
- **직렬화 할 수 있는 클래스**일 경우
  - 직렬화 형태(ITEM.87)에 관해서도 기술
- 문서화 주석이 없다면,
  - `javadoc`도 공개용 API 요소의 선언만 나열하는게 전부
- **기본 생성자**에는 문서화 주석을 달 방법이 없으므로,
- **공개 클래스**는 절대 **기본 생성자**를 사용 x
- **유지보수**까지 고려한다면,
  - 공개되지 않은 클래스, 인터페이스, 생성자, 메서드, 필드에도 **문서화 주석**을 달아야 함

### 메서드용 문서화 주석
- 메서드용 문서화 주석에는
  - 해당 **메서드**와 C사이의 규약을 명료하게 기술해야 함
- **상속용**으로 설계된 클래스(ITEM.19)가 아니라면
  - 해당 메서드가 어떻게 동작하는지가 아니라
  - **무엇을 하는지**를 기술하여야 함
  - **`how`가 아닌 `what`을 기술**
- 문서화 주석에는
  - C가 해당 메서드를 **호출**하기 위한 **전제조건(precoondition)**을 모두 나열해야 함
  - 메서드가 성공적으로 수행한 후에 만족해야하는 **사후 조건(postcondition)**을 나열해야 함
- **전제 조건**은 `@throws` 태그로 **비검사 예외**를 선언하여 **암시적**으로 기술
- **비검사 예외** 하나가 **전제 조건** 하나와 연결됨
- `@param` 태그를 사용하여
  - 그 조건에 영향받는 **매개변수**에 기술할 수 있음

### 부작용 문서화
- 부작용
  - **사후 조건**으로 명확히 나타나지는 않지만
    - **시스템 상태**에 어떠한 변화를 가져오는 것
  - 예시
    - `bg` thread를 시작시키는 메서드 등

### 메서드의 contact(계약) 기술
- 모든 매개변수에 `@param` 태그
- 반환 타입이 `void`가 아닐경우, `@return` 태그
- 발생할 가능성이 있는 모든 예외에 `@throws` 태그(ITEM.74)
- 코딩 표준에서 허락할 경우
  - `@return`태그의 설명 == 메서드 설명, `@return` 생략 가능

### @param과 @return
- `@param`과 `@return` 태그의 설명은
  - 해당 매개변수가 뜻하는 **값**이나
  - **반환값**을 설명하는 명사구 사용
- 드물게는 **명사구**대신 **산술표현식**을 사용하기도 함
- 예시
  - `BigInter`의 API문서
    - `@throws` 태그의 설명은
      - `if`로 시작해 해당 예외를 던지는 조건을 설명하는 절이 따름
- 예시 단락
  ```java
  /**
    * 이 리스트에서 지정한 원소의 위치 반환
    * <p> 이 메서드는 상수 시간에 수행됨을 보장하지 않음
    * 구현에 따라 원소의 위치에 비례해 시간 걸릴 수 있음
    * 
    * @param  index 반환할 원소의 인덱스, 0 이상이고 리스트 크기보다 작아야 함
    * @return 이 리스트에서 지정한 위치의 원소
    *        영문 번역시, the element at the specified position in this list
    * @throws IndexOutOfBoundsException index가 범위를 벗어나면,
    *         즉, ({@code index < 0 || index >= this.size()}) 이면 발생
    */
    E get(index);
  ```
- 문서화 주석에 `HTML 태그`를 사용
  - `javadoc` 유틸리티는, 문서화 주석을 `HTML`로 변환하기 때문
- 드물지만 jabadoc에 `HTML Table`을 집어넣는 사람도 있음

### @throws의 `{@code}` 태그
- 해당 태그의 효과
  - 태그로 감싼 내용을 **코드용 폰트**로 렌더링
  - 태그로 감싼 내용에 포함된 `HTML`요소나, 다른 `javadoc` 태그를 무시
- `HTML` 메타문자인 `<` 등을, 별다른 처리 없이 바로 사용할 수 있음
- 문서화 주석에 여러줄로 된 **코드 예시**를 넣으려면
  - `{@code}` 태그를 다시 `<pre>` 태그로 감싸면 됨
  - `<pre>{@code ... 코드 ...}</pre>` 형태
- `HTML`의 탈출 메타 문자를 쓰지 않아도,
  - 코드의 **줄바꿈이 유지됨**

### this list의 의미
- `@return`에 기술된 것과 같이 `this list`는 관례적으로 사용
- 인스턴스 메서드의 문서화 주석에 쓰인 `this`는 호출된 메서드가 자리하는 객체를 의미

### 자기사용 패턴에 대한 문서
- self-use pattern
- 클래스를 **상속용**으로 설계 할때
  - self-use pattern에 대해서도 문서에 남겨,
  - 다른 프로그래머에게 그 **메서드**를 올바르게 **재정의**하도록 알려해야 함
- 자기사용패턴은
  - `java8`에 추가된 `@implSpec` 태그로 문서화
- 일반적인 **문서화 주석**은
  - 해당 **메서드**와 C사이의 계약을 설명함
- 하지만 `@implSpec`은
  - 해당 **메서드**와 **하위 클래스** 사이의 **계약**을 설명
  - **하위 클래스**들이
    - 그 메서드를 **상속**하거나
    - `super` 키워드를 이용해 호출할 때
      - 그 메서드가 어떻게 동작하는지 명확히 인지하고 사용하도록 해야 함

### `implSpec`의 예시
- 코드
  ```java
  /**
    * @implSpec
    * 이 구현은 {@code this.size == 0}의 결과를 반환
    * 
    * @return 이 컬렉션이 비었다면 true, 그렇지 않으면 false
    * */
  public boolean isEmpty() { ... }
  ```
- `java 11`까지도, javadoc 명령줄에서
  - `-tag "implSpec:a:Implementation Requirements:"` 스위치를 켜지 않으면
  - `@implSpec` 태그를 무시함

### API 설명에서의 HTML 메타문자 사용
- 설명에 `<`, `>`, `&`등의 **HTML 메타문자**를 포함시키려면
  - 특별한 처리를 해주어야 함
- 가장 좋은 방법은 `{@literal}` 태그로 감싸는 것
  - **HTML 마크업**이나, **자바독 태그**를 무시하게 함
- `{@code}` 태그와 비슷하지만, 코드 폰트로 렌더링 하지 않음
- 예시
  ```java
  /**
    * {@literal |r| < 1} 이면 기하 수열이 수렴함
    * */
  ```
  - 위 예시에서 `<` 기호만 `{@literal}`로 감싸도 결과는 같으나,
    - 그럴경우, 코드에서의 문서화주석을 읽기 어려움
- **문서화 주석**은
  - **코드**에서건, **API문서**에서건, 읽기 쉬워야 한다
  - 양쪽을 만족시키지 못할 경우 **API 문서**에서의 가독성 우선

### 문서화 주석의 첫번째 문장 - 요약 설명
- 각 문서화 주석의 **첫번째 문장**은
  - 해당 요소의 **요약 설명(summary description)** 으로 간주
- 요약 설명은
  - 대상의 **기능**을 고유하게 기술
  - 헷갈리지 않으려면
    - **한 클래스|인터페이스**안에서
      - **요약 설명**이 똑같은 멤버|생성자가 **둘 이상 X**
- **다중정의**가 되어있다면, 더 조심
- **다중정의 메서드**들의 설명은
  - 같은 문장으로 시작하는게 자연스러우나,
  - **문서화 주석**에서 허용되지 않음

### 요약설명에서의 마침표 주의
- 요약 설명에서 `.`에 주의해야 함
- 요약 설명이 끝나는 패턴
  - `{<마침표><공백><다음문자시작>}` 패턴의 `<마침표>`까지
    - `Mrs. Peacock` 등을 조심해야 함
  - `<공백>`은 `space`, `tab`, `new line`을 의미
  - `<다음문장시작>`은 **소문자가 아닌**문자
- 이 역시 해결하려면 `{@literal}`을 사용해야 함
- 예시
  ```java
  /**
    * 머스타드 대령이나, {@literal Mrs. 피콕} 같은 용의자.
    * */
  public class Suspect { ... }
  ```

### java 10에서의 처리 - `{@summary}`
- `{@summary}`라는 요약 설명 전용 태그가 추가 됨
- 예시
  ```java
  /**
    * {@summary A suspect, such as Colonel Mustard or Mrs. Peacock.}
    * */
  public enum Suspect { ... }
  ```

### 요약 설명에 대해
- `"요약 설명은 문서화 주석의 첫 문장이다"`라고 말하면
  - 오해의 소지가 있음
- **주석 작성 규약**에 따르면
  - 요약 설명은 **완전한 문장**이 되는 경우가 드물다
- **메서드**와 **생성자**의 요약 설명은
  - 해당 메서드와 생성자의 **동작을 설명하는**
  - 주어가 없는 **동사구**이어야 함
  - 한글에서는 영향을 받지 않음
- 예시
  ```java
  ArrayList(int initialCapacity) : Constructs an empty list with the specified initial capacity
  Collection.size() : Returns the number of elements in this collection.
  ```
- **2인칭 문장(`return the number`)** 이 아닌, **3인칭 문장(`returns the number`)** 을 사용해야 함

### 클래스, 인터페이스, 필드의 요약 설명
- 대상을 설명하는 **명사절**
- 클래스와 인터페이스의 대상은 **인스턴스**
- 필드의 대상은 **필드** 자신
- 예시
  ```java
  Instant: An instantaneous point on the time-line.
  Math.PI: The double value that is closer than any other to pi, the ratio of the circumference of a circle to its diameter.
  ```

### HTML 문서 색인 기능
- `java 9`부터는 
  - javadoc이 생성한 `HTML` 문서에
  - 검색/색인 기능이 추가 됨
- `API`문서 페이지 우측 상단 검색창에
  - 키워드 입력시, 관련 페이지가 dropdown으로 나타남
- **클래스**, **메서드**, **필드** 같은 API 요소의 색인은
  - 자동으로 만들어짐
- 원할경우 `{@index}`로 색인화 가능
- 예시
  ```java
  /**
    * This method compiles with the {@index IEEE 754} standard.`
    * */
  ```

### 제네릭, 열거 타입, 애너테이션
- 문서화 주석에서 유의 해야 함
- **제네릭 타입**이나 **제네릭 메서드**를 문서화 할 때
  - **모든 타입 매개변수**에 주석을 달아야 함
- 예시
  ```java
  /**
    * @param <K> the type of keys maintained by this map
    * @param <V> the type of mapped values
    * */
  public interface Map<K, V> { ... }
  ```
- **열거 타입**을 문서화 할 때, **상수**들에도 주석을 달아야 함
- **열거 타입 자체**와 **그 열거타입의 public 메서드** 포함
- 예시
  ```java
  /**
    * An instrument section of a symphony orchestra.
    * */
  public enum OrchestraSection {
    /** Woodwinds, such as flute, clarinet, and oboe. */
    WOOWIND,

    /** Brass instruments, such as french horn and trumpet. */
    BRASS,
    ...;
  }
  ```

### 애너테이션 타입 문서화
- 애너테이션 타입을 문서화 할때는
  - 해당 **멤버**들에게도 모두 주석을 달아야 함
- 필드 설명은 **명사구**로 작성
- 애너테이션 타입의 **요약 설명**은
  - 프로그램 요소에 애너테이션을 단다는 것이
  - **어떤 의미**인지를 설명하는 **동사구**
    - 한글로 쓴다면, 동사로 끝나는 평범한 문장
- 예시
  ```java
  /**
    * Indicates that the annotated method is a test method that
    * must throw the designated exception to pass.
    * */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ExceptionTest {
    /**
      * The exception that the annotated test method must throw
      * in order to pass. (The test is permitted to throw any
      * subtype of the type described by this class object.)
      * */
    Class<? extends Throwable> value();
  }
  ```

### 패키지/모듈을 설명하는 문서화 주석
- 패키지를 설명하는 문서화 주석은
  - `package-info.java` 파일에 작성
- 이 파일은 **패키지 선언**을 반드시 포함해야 함
- **패키지 선언 관련 애너테이션**을 추가로 포함할 수 있음
- `java 9`부터 지원하는 **모둘 시스템(ITEM.15)**도 이와 비슷
- **모듈 시스템**을 사용한다면
  - 모듈 관련 설명은 `module-info.java` 파일에 작성

### API문서화에서 자주 누락되는 설명
- **스레드 안전성**과 **직렬화 가능성**
- **클래스** 혹은 **정적 메서드**가
  - **스레드** 안전하든, 아니든간에
  - **스레드 안전 기준**을 반드시 `API 설명`에 포함해야 함(ITEM.82)
- **직렬화**할 수 있는 클래스라면
  - **직렬화 형태**도 `API 설명`에 기술해야 함(ITEM.87)

### 메서드 주석의 상속
- javadoc은 **메서드 주석**을 **상속**시킬 수 있음
- **문서화 주석**이 없는 API 요소를 발견하면
  - javadoc이 가장 가까운 문서화 주석을 찾아줌
- **상위 클래스**보다 그 클래스가 구현한 **인터페이스**를 먼저 찾음
  - 자세한 검색 알고리즘은 `The Javadoc Reference Guide` 참고
- `{@inheritDoc}` 태그를 사용해
  - 상위 타입의 문서화 주석 일부를 **상속** 할 수 있음
- 클래스는 자신이 구현한 **인터페이스**의 문서화 주석을
  - 복사/붙여넣기 없이, 재사용 가능하다는 의미
- 위 기능을 활용하면
  - 거의 똑같은 문서화 주석 여러개를 유지보수하는 부담이 감소하나
  - 사용하지 까다롭고 제약이 존재
    - 오라클 공식문서 참조

### 문서화 주석에 대한 주의사항
- 공개된 모든 API요소에 **문서화 주석**을 달았더라도,
  - 이것만으로 충분하지 않을 수 있음
- 여러 클래스가 **상호작용**하는 복잡한 API라면
  - 문서화 주석 외에도 **아키텍처**를 설명하는 별도의 설명이 필요
- 이런 설명 문서가 있다면
  - 관련 클래스나 패키지 문서화 주석에서
  - 그 문서의 **링크**를 제공하면 좋음

### javadoc
- javadoc은 프로그래머가, javadoc 문서를 올바르게 작성했는지 **확인하는 기능 제공**
- 이번 ITEM에서 소개한 권장사항중, 대부분을 검사해줌
- `java7`에서는 명령줄에서 `-Xdoclint` 스위치를 키면 이 기능이 활성화 됨
  - `java8`에서는 기본으로 작송
- `checkstyle`과 같은 IDE 플러그인을 사용하면 더 완벽하게 검사 됨
- javadoc이 생성한 `HTML` 파일을 
  - `HTML` 유효성 검사기로 돌리면, 문서화 주석의 오류 제거 가능
    - 잘못된 HTML 태그 검사

### 정리
- **문서화 주석**은
  - API를 문서화 하는 가장 좋은 방법
- **공개 API**의 경우, 빠짐없이 설명을 달아야 함
- 표준 규약을 잘 지켜야 함
- **문서화 주석**에 임의의 `HTML 태그`를 사용할 수 있음
  - 단, `HTML 메타 문자`는 특별하게 취그배야 함
- 정말 잘 쓰인 문서인지를 확인하려면
  - **자바독 유틸리티가 생성한 웹페이지를 읽어보기**

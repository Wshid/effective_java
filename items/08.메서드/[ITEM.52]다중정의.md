## [ITEM.52] 다중정의
### 컬렉션 분류기의 오류
- 예시 코드
  ```java
  public class CollectionClassifier {
    public static String classify(Set<?> s) {
      return "Set";
    }

    public static String classify(List<?> lst) {
      return "list";
    }

    public static String classify(Collection<?> c) {
      return "other";
    }

    public static void main(String[] args) {
      Collection<?>[] collections = {
        new HashSet<String>(),
        new ArrayList<BigInteger>(),
        new HashMap<String, String>().values()
      };

      for(Collection<?> c : collections)
        System.out.println(c.classify(c));
    }
  }
  ```
- 수행 시,
  - `Set`, `list`, `other` 순서 출력이 아닌
  - `other`만 3번 연속 출력
- **overloading(다중정의)**된 세 `classify` 중
  - 어느 메서드가 호출할지는 **Compile Time**에 정해짐
- **compile time**에는 `for`문 안의 `c`는 항상 `Collection<?>` 타입
  - **run time**에는 매번 타입이 달라지나,
  - 호출할 메서드를 선택하는데 영향을 주지 못함
- 따라서 **compile time**의 **매개변수 타입**을 기준으로
  - 항상 세번째 메서드인 `classify(Collection<?>)`만 호출

### 재정의한 메서드, 다중정의한 메서드
- 생각과 다르게 동작했던 이뉴는
  - **재정의**한 메서드는 **동적**으로 선택
  - **다중정의**한 메서드는 **정적**으로 선택
- 메서드를 **재정의**했을 경우,
  - 해당 객체의 **runtime type**이 어떤 메서드를 호출할지에 대한 기준이 된다.
- 메서드 재정의란
  - **상위 클래스**가 정의한 것과 동일한 **시그니처** 메서드를 **하위 클래스**에서 정의
- **하위 클래스의 인스턴스**에서 그 메서드를 호출시, 재정의한 메서드 호출
- **compile time**에 그 인스턴스의 타입이 무엇인지는 상관 없음
- 예시
  ```java
  class Wine {
    String name() { return "포도주"; }
  }

  class SparklingWine extends Wine {
    @Override String name() {
      return "발포성 포도주";
    }
  }

  class Champagne extends SparklingWine {
    @Override String name() {
      return "샴페인";
    }
  }

  public Class Overriding {
    public static void main(String[] args){
      List<Wine> wineList = List.of(
        new Wine(), new SparklingWine(), new Champagne()
      );

      for (Wine wine : wineList)
        System.out.println(wine.name());
    }
  }
  ```
- 결과는 당연하게도, `포도주`, `발포성 포도주`, `샴페인`을 출력
- `for`에서의 **compile time type**이 모두 `Wine`인 것에 무관하게
  - 가장 **하위에서 정의**한 **재정의 메서드**가 실행

### 다중정의된 메서드와 런타임 타입
- **다중정의된 메서드** 사이에서는 객체의 **runtime type**은 중요하지 않음
- 선택은 **매개변수**의 **compile time type**에 의해 이루어진다.


### `CollectionClassifier` 해결 예시
- 매개변수의 **runtime type**에 기초하여
  - 적절한 **다중 정의 메서드**로 다중 분배
- 이 문제는
  - **정적 메서드**를 사용하여
  - `CollectionClassifier`의 모든 `classify` 메서드를 하나로 합친 후,
  - `instanceof`로 명시적 검사 사용
- 해결 코드
  ```java
  public static String classify(Collection<?> c) {
    return c instanceof Set ? "Set" :
           c instanceof List ? "list" : "other";
  }
  ```
- 개발자 관점에서, **다중정의**된 메서드는 오해를 부를 수 있음
- **다중정의**가 혼동을 일으키는 상황은 피해야 함

### 다중정의의 혼란
- **안전**하고 **보수적**으로 가려면
  - **매개변수**의 **수**가 동일한 다중정의는 만들지 말 것
- `varargs`를 사용하는 메서드라면, **다중정의 금지**(ITEM.53의 예외)
- **다중정의**하는 대신
  - 메서드의 이름을 다르게 지어주어도 된다.

### `ObjectOutputStream` 클래스 예시
- `write` 메서드
  - 모든 기본 타입 및 일부 참조용 타입용 변형 보유
  - 다중정의가 아닌, 모든 메서드에 다른 이름 지정
    - `writeBoolean(boolean)`, `writeInt(int)`, ...
  - 이 방식일 경우, `read` prefix를 가지는 메서드들과 짝을 맞추기가 쉽다
- 실제 `ObjectOutputStream`의 `read` 클래스는 위와 같이 구현됨

### 생성자와 다중정의
- **생성자**는 이름을 다르게 지을 수 없기 때문에
  - 두번째 생성자부터는 **무조건 다중정의**
- 단, **정적 팩터리**라는 대안을 사용할 수 있음(ITEM.01)
- 생성자는 **재정의**할 수 없으니,
  - **다중정의**와 재정의가 혼용될 수 없음
- 그래도 여러 생성자가 **같은 수**의 매개변수를 받아야 하는 경우 존재,
  - 안전 대책을 세워야 함

### 안전대책 - radically different
- **매개변수의 수**가 같은 **다중정의 메서드**가 많더라도
  - 주어진 **매개변수의 집합**을 처리할지가 구분 된다면, 혼란 회피 가능
- **매개변수**중 하나 이상이
  - **근본적으로 다름(radically different)**일 경우, 헷갈릴 일이 없음
- **radically different**는
  - 두 타입의 `null`이 아닌 값을 **상호간 형변환 불가**
  - 이 조건만 충족하면, 어떤 **다중 정의 메서드**를 호출할지가
    - 매개변수의 **runtime type**으로 결정
- 따라서 **compile time type**에는 영향을 받지 않으며
  - 혼란을 주는 원인이 사라짐
- 예시
  - `ArrayList`에는 `int`를 받는 생성자와 `Collection`을 받는 생성자 존재,
    - 어떤 상황에서든 두 생성자의 호출 여부가 헷갈리지는 않음

### 다중정의의 혼란 - generic과 AutoBoxing
- `java :4`에는 모든 **기본 타입**이 모든 **참조 타입**과는 달랐으나,
  - `java 5:`부터 **autoboxing**이 도입되면서, 혼란이 추가됨
- 예시 코드
  ```java
  public class SetList {
    public static void main(String[] args) {
      Set<Integer> set = new TreeSet();
      List<Integer> list = new ArrayList();

      for (int i = -3; i < 3; i++) {
        set.add(i);
        list.add(i);
      }

      for (int i = 0; i < 3; i++) {
        set.remove(i);
        list.remove(i);
      }
      System.out.println(String.format("%s %s", set, list));
    }
  }
  ```
- 예상에는 `[-3, -2 -1] [-3, -2, -1]`이 출력될 것 같지만,
  - 실제로는 집합에서 **음이 아닌 값**을 제거하고, 리스트는 **홀수를 제거**
  - `[-3, -2, -1] [-2, 0, 2]`가 출력
- 문제가 발생한 이유
  - `set.remove(i)`의 시그니처는 `remove(Object)`
    - 다중정의된 메서드가 없으니, 집합에서 `0`이상의 수를 제거한다.
  - `list.remove(i)`는
    - **다중정의**된 `remove(int index)`를 선택
    - 그런데 `remove`는 **지정한 인덱스**의 원소를 제거
      - 이때, 차례대로 1,2,3번째 원소를 제거하니 `[-2 0 2]`의 결과 리턴
- 문제의 해결 방법
  - `list.remove`의 인수를 `Integer`로 형변환 하여,
    - 올바른 **다중정의 메서드**를 선택하도록 하면 됨
  - 또는 `Integer.valueOf`를 이용하여
    - `i`를 `Integer`로 변환한 후, `list.remove`에 전달해도 됨
  - 위 두가지 방식을 사용하면
    - `[-3, -2 -1] [-3, -2, -1]`가 출력 됨
- 해결 코드
  ```java
  for(int i = 0; i < 3; i++) {
    set.remove(i);
    list.remove((Integer) i); // 또는 (Integer.valueOf(i))
  }
  ```
- 예시가 혼란스러웠던 이유는
  - `List<E>` 인터페이스가 `remove(Object)`와 `remove(int)`가 다중정의 되었기 때문
- `java :4`에서의 `List`는 `Object`와 `int`가 근본적으로 달랐음
  - 그런데 `generic`과 `autoboxing`이 등장하면서 두 매개변수 타입이 더는 근본적으로 다르지 않음
- `geneic`과 `autoboxing`을 더한 결과
  - `List` 인터페이스가 취약해짐
  - 피해를 입은 API는 거의 없으나, **다중정의**를 유의하여 사용해야 함

### 다중정의의 혼란 - 람다와 메서드 참조
- 코드
  ```java
  // Thread의 생성자 호출
  new Thread(System.out::println).start();

  // ExecutorService의 submit 메서드 호출
  ExecutorService exec = Executors.newCachedThreadPool();
  exec.submit(System.out::println);
  ```
- 두 예시가 비슷하나, `ExecutorService`에서는 컴파일 오류 발생
  - 넘겨진 인수는 `System.out::println`으로 동일
  - 양쪽 모두 `Runnable`을 받는 형제 메서드
- 실패 원인
  - `submit` 다중 정의 메서드 중
    - `Callable<T>`를 받는 메서드가 있기 때문
  - 모든 `println`은 `void`를 반환하기 때문에
    - 반환값이 있는 `Callable`과 헷갈릴 리는 없다고 생각할 수 있음
- 합리적인 추론으로,
  - **다중정의 해소**(resolution; 적절한 다중정의 메서드를 찾는 알고리즘)는 동작하지 않음
- `println`이 다중정의 없이, 하나만 존재했을 경우
  - `submit` 역시 정상 **compile**`
- 현재 참조 된 메서드(`println`)과 호출한 메서드(`submit`)이 양쪽 다중정의 되어
  - **다중정의 해소 알고리즘**이 정상 동작하지 않음

### System.out.println의 모호함
- `System.out::println`은
  - **부정확한 메서드 참조(inexact method reference)**
- 또한 **암시적 타입 람다식(implicitly typed lambda expression)**이나
  - **부정확한 메서드 참조**와 같은 **인수 표현식**은
  - **목표 타입**이 선택되기 전까지, 그 의미가 정해지지 않음
    - **적용성 테스트(applicability test)**때 **무시** 된다
- **다중정의 된 메서드**또는 생성자들이
  - **함수형 인터페이스**를 인수로 받을 때
  - 비록 서로 **다른 함수형 인터페이스**라도
    - **인수 위치**가 같으면 혼란이 생김

### 메서드 다중정의시 함수형 인터페이스와 인수의 위치
- **메서드 다중정의**할 때,
  - 서로 다른 **함수형 인터페이스**라도, **같은 위치의 인수**를 받으면 안됨
- **함수형 인터페이스**라도
  - 서로 **근본적으로 다르지 않음**
- 컴파일 할 때, 명령줄에
  - `-Xlint:overloads`를 지정하면, 이런 종류의 다중정의 경고 표시 가능

### 서로 다른 클래스와 근본적인 다름
- `Object`외에 **클래스 타입**과 **배열 타입**은 근본적으로 다름
- `Serializable`과 `Cloneable`외에
  - **인터페이스 타입**과 **배열 타입**도 근본적으로 다름
- `String`과 `Throwable`처럼
  - **상위/하위 관계**가 아닌 두 클래스는 **unrelated(관련 없음)** 이라고 함
- 어떤 객체도 관련 없는 **두 클래스**의 **공통 인스턴스**가 될 수 없으므로
  - 관련 없는 클래스들 끼리도, 근본적으로 다름

### 다중정의와 레거시
- 이미 만들어진 클래스가 관여될 경우
- `String`은
  - `java :4`, `contentEquals(StringBuffer)`메서드 보유
  - `java 5:`, `StringBuffer`, `StringBuilder`, `String`, `CharBuffer` 등의 비슷한 부류의 타입을 위한
    - 공통 인터페이스로 `CharSequence`가 등장
    - 이에 따라, `String`에도 `CharSequence`를 받은 `contentEquals`가 다중정의 됨
    - 그 결과, 이번 아이템을 대놓고 어기게 됨
      - 다행히, 두 메서드는 같은 객체를 입력하면, 동일한 역할을 하기 때문에 **해로울 것은 없음**
- 이처럼
  - **다중정의 메서드**가 불리는지 모르더라도, **기능이 동일**하다면 신경쓸 필요가 없음
- 일반적인 방법
  - 상대적으로 더 특수한 **다중정의 메서드**에서
    - **덜 특수한(일반적인) 다중정의 메서드로** 일을 **forward** 하는 것
- 자바 라이브러리 에서는 이 아이템을 따르려 하지만,
  - 실패한 클래스 역시 존재
    - `String` 클래스의 `valueOf(char[])`과 `valueOf(Object)`는 같은 객체를 건네도, 전혀 다른일 수행
    - 이렇게 할 이유가 없음에도 불구, 혼란을 야기하는 잘못된 사례

### 결론
- **다중정의**가 기능적으로 구현되어 있으나, 꼭 사용할 필요는 없음
- 일반적으로 매개변수 **수**가 동일할 경우, 다중정의를 회피할 것
- 상황에 따라, 특히 **생성자**라면, 이 조언을 따르기 어려울 수 있음
  - 그럴 때는, **매개변수**는 **형변환**하여 정확한 **다중정의 메서드**를 선택하도록 유도
  - 불가능할 경우, (**기존 클래스**를 수정하여, **새로운 인터페이스**를 구현할때와 같은 상황일 때)
    - **같은 객체**를 입력받은 **다중 정의 메서드**가
      - 모두 **동일한 동작**을 가지도록 함
    - 그러지 못할 경우, **다중정의 된 메서드**나 **생성자**의 효과적 활용이 불가능 하며
      - 의도대로 동작 안하는 이유도 확인 불가
## [ITEM.40] Override 애너테이션의 일관된 사용

### Override의 사용
- 메서드 선언에만 달 수 있음
- 상위 타입의 메서드를 재정의했다는 의미
- 일관되게 사용하면 **악명 높은 버그**를 예방해준다.


### 예제 - Bigram
- 영어 알파벳 2개로 구성된 문자열 포함
  ```java
  public class Bigram {
    private final char first;
    private final char second;
    public Bigram(char first, char second) {
      this.first = first;
      this.second = second;
    }
    public boolean equals(Bigram b) {
      return b.first == first && b.second == second;
    }
    public int hashCode() {
      return 31 * first + second;
    }
    public static void main(String[] args) {
      Set<Bigram> s = new HashSet<>();
      for(int i = 0; i<10; i++)
        for (char ch='a'; ch<='z'; ch++)
          s.add(new Bigram(ch, ch));
      System.out.println(s.size());
    }
  }
  ```
  - main 메서드
    - 소문자 2개로 구성된 Bigram 26개를 10번 반복, 집합에 추가
    - 해당 집합의 크기 출력
  - `Set`은 중복을 허용하지 않기 때문에
    - `26`이 출력될 것 같으나,
    - 실제로는 `260`이 출력됨

### Bigram 코드의 문제
- 의도 : `equals`를 재정의, `hashCode`도 함께 재정의(ITEM.10, ITEM.11)
- `equals`를 재정의(overriding) 한 게 아니라,
  - **다중정의**(overloading, ITEM.52)
- `Object::equals`를 재정의 하려면
  - 매개변수 타입을 `Object`로 해야 함
  - 여기서는 `Bigram`을 사용하여, 의도대로 되지 않음
  - 별개의 `equals`함수를 또 제작
- `Object::equals`
  - `==`연산자와 같이, 객체 식별성(identity)만 확인함
  - 같은 소문자를 소유한 `Bigram` 10개가 각각이 서로 다른 객체로 인식, 260으로 출력
- 해당 오류는 **컴파일러가 찾아낼 수 있음**

### 컴파일러가 찾아내려면
- `Object.equals`를 재정의한다는 의도를 나타내야 함
- 코드
  ```java
  @Override public boolean equls(Bigram b) {
    return b.first == && b.second == second;
  }
  ```
- `@Override` 애너테이션을 달고 컴파일 시, 경고가 출력 됨
- 경고를 확인하고 수정한 코드
  ```java
  @Override public boolean equals(Object o) {
    if(!(o instanceof Bigram))
      return false;
    Bigram b = (Bigram) o;
    return b.first == first && b.second == second;
  }
  ```

### Override의 사용 유의점
- **상위 클래스의 메서드**를 **재정의**하려는 모든 메서드에 `@Override` 애너테이션을 달아야 함
- 예외
  - **구체 클래스**에서 상위 클래스의 **추상 메서드** 재정의 시에
    - `@Override`를 달지 않아도 됨
    - 구체 클래스인데, **아직 구현하지 않은 추상 메서드**가 남아 있다면
      - **컴파일러**가 알려주기 때문
  - 물론 재정의 메서드에 `@Override`를 일괄로 붙여줘도 상관 없음
  - 대부분의 **IDE**에서 재정의할 메서드를 선택하면
    - `@Override`를 자동으로 붙여줌
- `@Override`는 **클래스** 뿐만 아니라 **인터페이스 메서드 재정의**시에서 사용 가능
  - **디폴트 메서드**를 지원하기 시작하면서
    - **인터페이스 메서드**를 구현한 메서드에도 `@Override`를 다는 습관을 들이면 좋음
  - 구현하려는 메서드에 **디폴트 메서드**가 없음을 안다면
    - 이를 **구현한 메서드**에서는 `@Override`를 생략해 코드를 깔끔히 유지해도 됨
- **추상 클래스**나 **인터페이스**에서는
  - **상위 클래스**나 **상위 인터페이스**의 메서드를 재정의하는 **모든 메서드**에 `@Override`를 다는 것이 좋음
- `Set` 인터페이스는 `Collection` 인터페이스를 확장했지만, 새로 **추가한** 메서드는 없음
  - 따라서 모든 메서드 선언에 `@Override`를 달아, **실수로 추가한 메서드가 없다**는 것을 보장

### 결론
- **재정의한 모든 메서드에 `@Override`를 달아** 실수를 방지할 수 있음
  - **컴파일러**가 알려줌
- 예외는 한가지
  - **구체 클래스**에서 **상위 클래스**의 **추상 메서드**를 재정의한 경우엔
    - 이 애너테이션을 달지 않아도 됨
    - 달아도 상관은 없음
## [ITEM.65] 리플렉션보다 인터페이스 사용
### reflection
- `java.lang.reflect`를 이용하면
  - 프로그램에서 임의의 **클래스**에 접근 가능
- `Class` 객체가 주어지면
  - 그 클래스의 **생성자**, **메서드**, **필드**에 해당하는
  - `Consturctor`, `Method`, `Field` **인스턴스**를 가져올 수 있음
- 위 **인스턴스**로는
  - 그 클래스의 **멤버 이름**, **필드 타입**, **메서드 시그니처**를 가져올 수 있음

### Constructor, Method, Field 인스턴스의 사용
- 인스턴스를 사용해, 각각에 연결된
  - 실제 **생성자**, **메서드**, **필드** 조작 가능
    - 클래스의 인스턴스를 생성하거나, 메서드를 호출하거나, 필드에 접근 가능하다는 의미
- `Method.invoke`는
  - 어떤 클래스의 **어떤 객체**가 가진 어떤 메서드라도 **호출 가능**하게 해줌
  - 물론, 일반적인 **보안 제약사항**은 준수해야 함

### reflection의 클래스 접근
- `reflection`을 이용하면
  - **컴파일 당시**에는 존재하지 않던 클래스도 이용 가능하나, 단점 존재
- 단점
  - **컴파일 타입 검사가 주는 이점이 없음**
    - 예외 검사도 마찬가지
    - `reflection` 기능을 사용해서, 존재하지 않는 혹은 접근 불가능한 메서드를 호출하려 한다면
    - **Runtime Error**가 발생한다
  - **리플렉션을 이용하면 코드가 지저분하고 장황함**
  - **성능 하락**
    - **리플렉션을 통한 호출**은 일반 메서드 호출보다 많이 느림
    - 입력 매개변수가 없고 `int`형 반환 메서드 실험시, 11배 느림

### 리플렉션 사용의 감소 추세
- 코드 분석 도구, 의존관계 주입 프레임워크 등
- 하지만 위의 경우에도, `relection` 사용이 줄고 있음
- 앱이 리플렉션이 필요한지 확신할 수 없다면
  - 필요 없을 가능성이 큼

### 리플렉션의 제한적 사용 권고
- 제한된 형태로만 사용해야, 단점을 피할 수 있음
- **컴파일 타임**에 이용할 수 없는 클래스를 사용하는 프로그램은
  - 비록 **컴파일 타임**이라도
  - 적절한 **인터페이스**나 **상위 클래스**를 사용할 수 있음(ITEM.64)
- 위와 같은 경우라면
  - 리플렉션은 **인스턴스 생성**에만 쓰고,
  - 이렇게 만든 **인스턴스**는 **인터페이스**나 **상위 클래스**로 참조하여 사용

### 사용 예시
- 예시 설명
  - `Set<String>` 인터페이스의 인스턴스를 생성
  - 정확한 클래스는 명령줄의 첫번째 인수로 확정
  - 생성된 `Set`에 두번째 이후의 인수들을 추가하여 출력
  - 첫번째 인수와 상관 없이,
    - 이후의 인수들에서는 중복 제거하여 출력
  - 출력되는 순서는
    - 첫번째 인수의 클래스가 무엇이냐에 따라 달라짐
    - `java.util.HashSet` : 무작위 순서
    - `java.util.TreeSet` : 알파벳 순
- 예시 코드
  ```java
  public static void main(String[] args) {
    // 클래스 이름을 Class 객체로 변환
    Class<? extends Set<String>> cl = null;
    try {
      cl = (Class<? extends Set<String>>) Class.forName(args[0]); // 비검사 형변환
    } catch (ClassNotFoundException e) {
      fatalError("Not found class");
    }
    // 생성자 얻기
    Constructor<? extends Set<String>> cons = null;
    try {
      cons = cl.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      fatalError("매개변수 없는 생성자를 찾을 수 없음");
    }

    // 집합의 인스턴스 생성
    Set<String> s = null;
    try {
      s = cons.newInstance();
    } catch (IllegalAccessException e) {
      fatalError("생성자 접근 불가");
    } catch (InstantiationException e) {
      fatalError("클래스 인스턴스화 불가");
    } catch(InvocationTargetException e) {
      fatalError("생성자 예외 반환" + e.getCause());
    } catch(ClassCastException e) {
      fatalError("Set을 구현하지 않은 클래스");
    }
    s.addAll(Arrays.asList(args).subList(1, args.length));
    System.out.println(s);
  }

  private static void fatalError(String msg) {
    System.err.println(msg);
    System.exit(1);
  }
  ```
- 예시 활용 방법
  - 위 예시로 쉽게 **제네릭 집합 테스터**로 사용 가능
  - 명시한 `Set` 구현체를 조작하면서
    - `Set` 규약을 잘 지키는지 검사 가능
  - **제네릭 집합 성능 분석 도구**로 활용 가능
  - 이 기법은 **서비스 제공자 프레임워크**(ITEM.1) 구현할 수 있을 만큼 강력함
  - 대부분의 경우, 리플렉션 기능은 이 정도만 사용해도 충분
- 위 예시에 드러난 리플렉션의 **단점**(2)
  - `Runtime`에 총 6가지나 되는 **예외**발생 가능
    - 모두가 **인스턴스**를 **리플렉션**없이 생성했다면,
    - **Compile Time**에 발견할 수 있는 내용들
      - 명령줄 인스턴스를 일부러 잘못 입력하면, 충분히 모든 예외 발생 가능
  - **클래스 이름**만으로 인스턴스를 생성하기 위해
    - **무려 25줄의 코**드가 사용됨
    - 리플렉션이 아니었을 경우, 1줄이면 끝날 코드
- 리플렉션 예외를 각각 잡는 대신,
  - 모든 리플렉션 상위 클래스인
  - `ReflectiveOperationException`을 잡아 사용해도 됨
    - `java 7:`
- 위 두 단점 모두 클래스를 **생성**하는 부분에 국한됨
- 객체가 일단 만들어지면, 그 후의 코드는
  - 타 `Set` 인스턴스를 사용하는 부분과 동일
- 실제 프로그램에서는, 이 제약에 영향받는 부분은 적음

### 비검사 형변환 경고
- 위 예시를 사용하였을 때 발생
- `Class<? extends Set<String>>`으로의 형변환은
  - 명시한 클래스가 `Set`을 구현하지 않았더라도, 성공함
  - 실제 문제로 일어나지는 않음
- 단, **클래스의 인스턴스를 생성**하려 할때 `ClassCastException` 발생
  - 이 경고를 숨기려면 ITEM.27 참고

### 리플렉션의 사용처 - 구버전 컴파일
- `Runtime`에 존재하지 않을수도 있는 
  - 타 클래스, 메서드, 필드와의 **의존성** 관리할 때 적합
- 이 기법은, 여러개 존재하는 **외부 패키지**를 다룰 때 유용
- 가동할 수 있는
  - 최소한의 환경, 주로 **오래된 버전을 지원**하도록 컴파일 한 후,
  - **이후 버전의 클래스와 메서드**는 `Reflection`으로 접근
- 위와 같이 하려면,
  - 접근하려는 새로운 클래스나 메서드가
  - `Runtime` 때 존재하지 않을 수 있다는 사실을 생각해야 함
- 같은 목적을 이룰 수 있는 **대체 수단**을 이용하거나
  - 기능을 줄여 동작하는 적절한 조치 필요

### 정리
- 리플렉션은 **복잡한 특수 시스템**을 개발할 때 사용하나, **단점** 존재
- `Compile Time`에는 알 수 없는 클래스를 사용하는 프로그램 작성시 사용
- 되도록 **객체 생성**에만 사용하고,
  - 생성한 객체 사용시,
  - 적절한 **인터페이스**나 `Compile Time`에만 알 수 있는 **상위 클래스**로 변환하여 사용
## [ITEM.89] 인스턴수 수 통제시 readResolve보다 열거타입

### 싱글턴 패턴의 보장
- 싱클턴 패턴
- 아래 예시의 클래스는, 밖에서 **생성자를 호출하지 못함**
  - 인스턴스가 오직 하나만 생성됨
- 코드
  ```java
  public class Elvis {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() {...}

    public void leaveTheBuilding() {...}
  }
  ```
- **ITEM.3**에서 언급했듯 위 클래스는
  - 선언에 `implements Serializable`을 추가하는 순간, **싱글턴**이 아니게 된다.
- **기본 직렬화**를 쓰지 않더라도(ITEM.87)
  - 그리고 명시적인 `readObject`를 제공하더라도(ITEM.88) 소용 없음
- `readObject` 사용하든 이 **클래스가 초기화**될 때 만들어진
  - 인스턴스와는 별개의 **인스턴스**를 반환하게 한다.

### `readResolve` 기능
- `readResolve`를 이용하게 되면
  - `readObject`가 만들어낸 **인스턴스**를 다른 것으로 대체할 수 있다.
- **역직렬화**한 객체의 클래스가 `readResolve` 메서드를 적절히 정의해뒀다면,
  - **역직렬화** 후 새로 생성된 **객체**를 인수로 이 **메서드**가 호출되고,
    - 이 **메서드**가 반환한 객체 참조가 새로 생성된 객체를 대신해 반환됨

### Serializable 구현 예시
- 다음의 `readResolve` 메서드를 추가해 **싱글턴** 속성을 유지할 수 있다.
  ```java
  // 인스턴스 통제를 위한 readResolve - 개선의 여지가 있다.
  private Object readResolve() {
    // 전체 Elvis를 반환하고, 가짜 Elvis는 가비지 컬렉터에 맡김
    return INSTNACE;
  }
  ```
- 위 메서드는 **역직렬화**하는 객체는 무시하고,
  - 클래스 초기화 때 만들어진 `Elvis` 인스턴스를 반환한다.
- 따라서 `Elvis`인스턴스의 **직렬화 형태**는
  - 아무런 **실 데이터**를 가질 이유가 없으니, 모든 인스턴스 필드를 `trasient`로 선언해야 함
- 사실, `readResolve`를 **인스턴스 통제 목적**으로 사용한다면,
  - **객체 참조 타입 인스턴스 필드** 모두 `transient`로 선언해야 함
- 그렇지 않을 경우 **ITEM.88**에서 살펴본 `MutablePeriod` 공격과 비슷한 방식으로
  - `readResolve` 메서드가 수행되기 전에 **역직렬화된 객체의 참조**를 공격할 여지가 남음

### 공격 아이디어
- 싱글턴이 `transient`가 아닌(non-transient) **참조 필드**를 가지고 있다면,
  - 그 필드의 내용은 `readResolve` 메서드가 실행되기 전 **역직렬화**
- 잘 조작된 `stream`을 짜서
  - 해당 참조 필드의 내용이 **역직렬화**되는 시점에
  - 그 **역직렬화된 인스턴스**의 참조를 훔쳐올 수 있음
- `readResolve` 메서드와 **인스턴스 필드**하나를 포함한 `strealer` 클래스를 작성
  - 이 **인스턴스 필드**는 `strealer`가 숨길 **직렬화된 싱글턴**을 참조하는 역할
- **직렬화된 스트림**에서
  - 싱글턴의 **비휘발성 필드**를 `strealer`의 인스턴스로 교체
  - 싱글턴은 `strealer`를 참조하고,
    - `strealer`는 싱글턴을 참조하는 순환고리 생성
- 싱글턴이 `strealer`를 포함하므로,
  - 싱글턴이 **역직렬화**될 때, `strealer`의 `readResolve` 메서드가 먼저 호출
  - 그 결과, `strealer`의 `readResolve` 메서드가 수행될 때
    - `strealer`의 인스턴스 필드에는 
      - **역직렬화 도중**이며,
      - `readResolve`가 수행되기 전인, 싱글턴의 참조가 담김
- `strealer`의 `readResolve` 메서드는
  - 이 인스턴스 필드가 참조한 값을 **정적 필드**로 복사하여
  - `readResolve`가 끝난 후에도 참조할 수 있도록 함
- 이후, 이 메서드는
  - `strealer`가 숨긴 `transient`가 아닌 필드의 **원래 타입에 맞는 값**을 반환
  - 위 과정을 생략할 경우, **직렬화 시스템**이 `strealer`의 참조를
    - 이 필드에 저장하려 할때, VM에서 `ClassCastException`이 발생

### 잘못된 싱글턴 예시
- 코드 : `transient`가 아닌 **참조 필드**를 가짐
  ```java
  public class Elvis implements Serializable {
    public static final Elvis INSTNACE = new Elvis();
    private Elvis() { }

    private String[] favoriteSongs = {"Hound Dog", "HeartBreak Hotel" };
    public void printFavorites() {
      System.out.println(Arrays.toString(favoriteSongs));
    }

    private Object readResolve();
  }
  ```
- `strealer` 클래스
  ```java
  public class ElvisStrealer implements Serializable {
    static Elvis impersonator;
    private Elvis payload;

    private Object readResolve() {
      // resolbe되기 전에  Elvis 인스턴스의 참조 지정
      impersonator = payload;

      // favoriteSongs 필드에 맞는 타입의 객체 반환
      return new String[] { "A Fool Such as I" };
    }
  }
  ```
- 수작업으로 만든 stream을 이용해, 2개의 싱글턴 인스턴스 반환
  ```java
  public class ElvisImpersonator {
    // 진짜 Elvis 인스턴스로는 만들어질 수 없는 스트림
    private static final byte[] serializedForm = { (byte)0xac, (byte)0xed, ... };

    public static void main(STring[] args) {
      // ElvisStrealer.impersonator를 초기화 한 다음, 
      // 진짜 Elvis(Elvis.INSTANCE)를 반환
      Elvis elvis = (Elvis) deserialize(serializedForm);
      Elvis impersonator = ElvisStrealer.impersonator;

      elvis.printFavorites();
      impersonator.printFavorites();
    }
  }
  ```
- 위 프로그램을 실행하게 되면, 다음 결과를 출력
  - 서로 다른 두개의 `Elvis` 인스턴스를 생성할 수 있음
  ```log
  [Hound Dog, Heartbreak Hotel]
  [A Fool Such as I]
  ```

### 문제의 해결 방법
- `favoriteSongs` 필드를 `transient`로 선언하여 문제를 고칠 수 있으나,
  - `Elvis`를 원소 하나짜리 **열거 타입**으로 바꾸는 편이 더 좋음(**ITEM.3**)
- `ElvisStrealer` 공격으로 보여졌듯이
  - `readResolve` 메서드를 사용해,
  - 순간적으로 만들어진 **역직렬화된 인스턴스**에 접근하지 못하게 하는 방법은
    - 깨지기 쉬우며, 신경을 많이 써야 하는 작업

### 열거타입을 사용할 때의 장접
- 직렬화 가능한 **인스턴스 통제 클래스**를, **열거 타입**을 이용해 구현할경우
  - 선언한 **상수**외의 다른 객체는 존재하지 않음을 **자바**에서 보장함
- 물론 공격자가, `AccessibleOBject.setAssesible`과 같은 `privileged` 메서드를 악용할 경우
  - 임의의 네이티브 코드 수정 권한이 있으므로, 모든 방어가 무력화 됨

### 해결 코드 - 열거 타입 싱글턴
- 코드
  ```java
  public enum Elvis {
    INSTANCE;
    private String[] favoriteSongs = { "Hound Dog", "Heartbreak Hotel" };
    public ovid printFavorites() {
      System.out.println(Arrays.toString(favoriteSongs));
    }
  }
  ```

### `readResolve`의 사용의 의미
- 인스턴스 통제를 위해 `readResolve`를 사용하는 방식이
  - 완전히 쓸모 없는 것은 아님
- **직렬화 가능 통제 클래스**를 작성할 때
  - `compileTime`에는 어떤 인스턴스가 있는지 알 수 없는 상황이라면,
  - 열거 타입으로 표현하는 것이 **불가능**하기 때문

### `readResolve` 메서드의 접근성
- `readResolve` 메서드의 접근성은 매우 중요
- `final` 클래스에서라면 `readResolve`메서드는 `private`이어야 함
- `final`이 아닌 클래스에서는 몇가지를 주의해야 함
  - `priavte`으로 선언하면, 하위 클래스에서 사용할 수 없음
  - `package-private`으로 선언하면, **같은 패키지**에 속한 하위 클래스에서 사용 가능
  - `protected`나 `public`으로 선언하면
    - 이를 **재정의**하지 않은 모든 하위 클래스에서 사용 가능
  - `protected`나 `public`이면서, 하위 클래스에서 재정의 하지 않았다면
    - 하위 클래스의 인스턴스를 **역직렬화**하면
    - 상위 클래스의 **인스턴스**를 생성하여 `ClassCastException`이 발생할 수 있음

### 정리
- **불변식**을 지키기 위해, **인스턴스 통제**가 필요할 경우
  - 가능한 **열거 타입**을 사용하기
- 여의치 않은 상황에서 **직렬화**와 **인스턴스 통제**가 모두 필요하다면
  - `readResolve`를 작성해 넣어야 하고
  - 그 클래스에서 **모든 참조 타입 인스턴스 필드**를 `transient`로 선언해야 함
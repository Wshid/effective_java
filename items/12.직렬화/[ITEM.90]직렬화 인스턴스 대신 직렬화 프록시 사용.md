## [ITEM.90] 직렬화 인스턴스 대신 직렬화 프록시 사용

### 직렬화 프록시 패턴
- serialization proxy pattern
- `Serializable`을 구현하기로 결정한 순간,
  - 언어의 정상 매커니즘인 **생성자**이외의 방법으로 **인스턴스 생성**이 가능
    - 버그와 보안 문제가 일어날 가능성이 커짐
- 이 위험을 줄이는 기법

### 직렬화 프록시 패턴의 개요
- **바깥 클래스**의 논리적 상태를 정밀하게 표현하는 **중첩 클래스** 설계
  - 이후 `private static`으로 선언
- 이 **중첩 클래스**가 바깥 클래스의 **직렬화 프록시**
- 중첩 클래스의 생성자는 **단 하나**이어야 하며,
  - **바깥 클래스**를 매개변수로 받아야 한다.
- 이 생성자는 단순히 **인수**로 넘어온 **인스턴스**의 데이터 복사
  - **일관성 검사**나 **방어적 복사**도 필요 없음
- 설계상, **직렬화 프록시**의 기본 직렬화 형태는
  - **바깥 클래스**의 직렬화 형태로 쓰기에 이상적
- 바깥 클래스와 직렬화 프록시 모두 `Serializable`을 구현한다고 선언해야 함

### `Period` 클래스 예시
- 위 클래스의 직렬화 프록시
  - `Period` 클래스 자체가 간단하기 때문에,
  - 직렬화 프록시도 바깥 클래스와 완전히 같은 **필드**로 구성
- `Period` 클래스용 직렬화 프록시
  ```java
  private static class SerializationProxy implements Serializable {
    private final Date start;
    private final Date end;

    SerializationProxy(Period p) {
      this.start = p.start;
      this.end = p.end;
    }
    
    private static final long serialVersionUID = ...L; // 아무 값이나 상관 없음
  }
  ```
- 다음으로, 바깥 클래스에 다음의 `writeReplace` 메서드를 추가
- 이 메서드는 **범용적**이기 때문에
  - **직렬화 프록시**를 사용하는 모든 클래스에 그대로 **복사**해서 사용하면 됨
- `writeReplace` 코드
  ```java
  // 직렬화 프록시 패턴용 writeReplace 메서드
  private Object writeReplace() {
    return new SerializationProxy(this);
  }
  ```
- 이 메서드는 **자바의 직렬화 시스템**이 **바깥 클래스**의 인스턴스 대신
  - `SerializationProxy`의 인스턴스를 반환하게 하는 역할
- **직렬화**가 이뤄지기 전에
  - **바깥 클래스**의 인스턴스를, **직렬화 프록시**로 변환

### writeReplace 공격과 readObject 방어
- `writeReplace` 덕분에
  - **직렬화 시스템**은, **바깥 클래스**의 직렬화된 인스턴스를 생성할 수 없음
- 하지만, 공격자는 불변식을 훼손하고자 공격 시도 가능
  - `readObject` 메서드를 바깥 클래스에 추가하면 이 공격을 가볍게 막아낼 수 있다.
    ```java
    // 직렬화 프록시 패턴용 readObject 메서드
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("need proxy");
    }
    ```
- 마지막으로, 바깥 클래스와 논리적으로 **동일한 인스턴스**를 반환하는
  - `readResolve` 메서드를 `SerializationProxy` 클래스에 추가
- 이 메서드는 **역직렬화**시에
  - **직렬화 시스템**이 **직렬화 프록시**를 다시 바깥 클래스의 **인스턴스**로 변환

### `readResolve`
- `readResolve` 메서드는 공개된 **API**만을 사용해,
  - 바깥 클래스의 인스턴스를 생성
- 직렬화는 **생성자**를 이용하지 않고도, **인스턴스 생성** 기능을 제공하는데,
  - 이 패턴은 **직렬화**의 이런 언어도단적 특성을 **상당부분 제거**
- 즉, **일반 인스턴스**를 만들 때와, 똑같은 **생성자**, **정적 팩터리** 혹은 다른 **메서드**를 사용하여
  - **역직렬화 된 인스턴스**를 생성한다
- 따라서 **역직렬화된 인스턴스**가 해당 클래스의 **불변식**을 만족하는지 검사할
  - 또 다른 수단을 고려하지 않아도 됨
- 그 클래스의 **정적 팩터리**나 **생성자**가 불변식을 확인해주고,
  - 인스턴스 **메서드**들이 **불변식**을 잘 지킨다면, 따로 공수가 들지 않음
- `Period.SerializationProxy::readResolve`
  ```java
  // Period.SerializationPRoxy용 readResolve 메서드
  private Object readResolve() {
    return new Period(start, end); // public 생성자를 사용
  }
  ```
- **방어적 복사**(ITEM.88)처럼, 직렬화 프록시 패턴은
  - **가짜 바이트 스트림 공격**과 **내부 필드 탈취 공격**을 **프록시 수준**에서 차단
- 앞의 두 접근법과 달리,
  - 직렬화 프록시는 `Period`필드를 `final`로 선언해도 되므로
    - `Period` 클래스를 진정한 **불변**(ITEM.17)으로 만들 수 있음
- 어떤 필드가 기만적인 **직렬화 공격**의 목표가 될지 고민하지 않아도 ㅁ되며,
  - 역직렬화할 때 **유효성 검사**를 수행하지 않아도 됨

### 직렬화 프록시 패턴이 readObject의 방어적 복사보다 나은 이점
- 직렬화 프록시 패턴은 **역직렬화한 인스턴스**와 **원래의 직렬화된 인스턴스** 클래스가
  - 달라도 정상 동작
- `EnumSet`의 사례(ITEM.36)
  - 위 클래스는 `public` 생성자 없이, **정적 팩터리**만 제공
  - `C`의 입장에서는 이 팩터리들이 `EnumSet` 인스턴스를 반환하는 것으로 보이지만,
  - 현재의 `OpenJDK`를 보면 **열거 타입**의 크기에 따라
    - 두 하위 클래스 중 하나의 **인스턴스**를 반환
  - 열거 타입의 원소가
    - 64개 이하라면, `RegularEnumSet`을 사용
    - 그보다 크면, `JumboEnumSet`을 사용
  - 원소 64개짜리 열거 타입을 가진 `EnumSet`을 **직렬화**한 다음
    - 원소 5개를 추가하고, **역직렬화**하게 되면,
      - 처음 직렬화 된 것은 `RegularEnumSet` 인스턴스 이며,
      - 역직렬화는 `JumboEnumSet` 인스턴스로 하면 좋음
  - 그리고 `EnumSet`은 **직렬화 프록시 패턴**을 사용하여, 실제로 이와 같이 동작

### `EnumSet`의 직렬화 프록시
- 코드
  ```java
  private static class SerializationPRoxy <E extends Enum<E>> implements Serializable {
    // 이 EnumSet의 원소 타입
    private final Class<E> elementType;

    // 이 EnumSet 안의 원소들
    private final Enum<?>[] elements;

    SerializationProxy(EnumSet<E> set) {
      elementType = set.elementType;
      elements = set.toArray(new Enum<?>[0]);
    }

    private Object readResolve() {
      EnumSet<E> result = EnumSet.noneOf(elementType);
      for (Enum<?>e : elements)
        result.add((E)e);
      return result;
    }
    
    private static final long serialVersionUID = ...L;
  }
  ```

### 직렬화 프록시 패턴의 2가지 한계
- 한계점
  - `C`가 멋대로 확장할 수 있는 클래스(**ITEM.19**)에는 적용 불가
  - 객체 그래프에 **순환**이 있는 클래스에도 적용 불가
- 이런 객체의 메서드를 **직렬화 프록시**의 `readResolve` 안에서 호출하려 하면,
  - `ClassCastException`이 발생함
- 직렬화 프록시만 가졌을 뿐, **실제 객체**에서는 아직 만들어지지 않았기 때문
- 직렬화 프록시 패턴이 주는 **강력함**과 **안전성**에는 비용이 든다
  - 방어적 복사보다 14% 정도가 느렸다고 함

### 정리
- 제3자가 확장할 수 없는 클래스라면, 가능한 한 **직렬화 프록시 패턴**을 사용
- 이 패턴은 중요한 **불변식**을 안정적으로 **직렬화**해주는 가장 쉬운 방법
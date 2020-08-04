## [ITEM.88] readObject 메서드는 방어적으로 작성

### 방어적 복사를 사용하는 불변 클래스
- `ITEM.50`, 불변인 **날짜 범위 클래스**를 만드는데에
  - 가변인 `Date`필드를 활용함
- 불변식을 지키고, 유지하기 위해
  - **생성자**와 **접근자**에서 `Date` 객체를 **방어적으로 복사**, 코드가 길어짐
- 코드
  ```java
  public final class Period {
    private final Date start;
    private final Date end;

    /**
      * @param  start 시작 시각
      * @param  end   종료 시각; 시작 시각보다 뒤어야 한다
      * @throws IllegalArgumentException  시작 시각이 종료 시각보다 늦을때 발생
      * @throws NullPointerException  start나 end가 null이변 발생
      */
    public Period(Date start, Date end) {
      this.start = new Date(start.getTime());
      this.end = new Date(end.getTime());
      if (this.start.compareTo(this.end) > 0)
        throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
    }

    public Date start() { return new Date(start.getTime()); }
    public Date end() { return new Date(end.getTime()); }
    public String toString() { return start + " - " + end; }

    ... // 나머지 코드 생략
  }
  ```
  - 위 클래스 **직렬화**시,
    - `Period`객체의 **물리적 표현**이 **논리적 표현**에 부합하므로
    - **기본 직렬화 형태**(ITEM.87)을 사용해도 무방함
  - 하지만 클래스 선언에 `implements Serializable`을 추가하는 것으로는
    - **불변식**을 보장할 수 없음

### readObject 문제
- 불변식을 보장할 수 없는 이유
  - `readObject` 메서드가 실질적으로, 또 다른 `public` 생성자 이기 때문
- 따라서 다른 생성자와 똑같은 수준으로, 주의하여 작성해야 함
- 보통의 **생성자**처럼 `readObject` 메서드에도
  - 인수가 **유효**한지 검사해야 하고,(ITEM.49)
  - 필요하다면, 매개변수를 **방어적으로 복사**해야 함(ITEM.50)
    - `readObject`가 이 작업을 제대로 수행하지 못할 경우,
      - 공격자는 아주 쉽게 해당 클래스의 **불변식** 파괴 가능

### readObject와 생성자
- `readObject`는 **매개변수**로 **바이트 스트림**을 받는 **생성자**이다
- 보통의 경우 **바이트 스트림**은
  - 정상적으로 생성된 **인스턴스**를 **직렬화**하여 만들어짐
- 하지만 불변식을 깨뜨릴 의도로
  - **임의로 생성한 바이트 스트림**을 건네면, 문제가 발생함
- 정상적인 생성자로는 만들 수 없는 객체를 생성할 수 있기 때문

### Period와 비정상 바이트 스트림
- `Period` 클래스 선언에, `implements Serializable`만 추가한다면,
  - 다음 코드로, 종료 시각이 시작 시각보다 앞서는 `Period` 인스턴스를 만들 수 있음
- 코드
  ```java
  public class BogusPeriod {
    // 진짜 Period 인스턴스에서는 만들어 질 수 없는 바이트 스트림
    private static final byte[] serializeForm = {
      (byte) 0xac, (byte) 0xed, 0x00, 0x05, ... 
    };

    public static void main(String[] args) {
      Period p = (Period) deserialize(serializedForm);
      System.out.println(p);
    }

    static Object deserialize(byte[] sf) {
      try {
        return new ObjectInputStream (
          new ByteArrayInputStream(sf)).readObject();
      } catch (IOException | ClassNotFoundException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }
  ```
  - 이 코드의 `serializedForm`에서 **상위 비트**가 `1`인 바이트 값들은 `byte`로 형변환 했는데,
    - 이는 자바가 **리터럴**을 지원하지 않고, `byte` 타입은 부호 있는(signed) 타입이기 때문
- `serializedForm`을 초기화 하는데 사용한 **바이트 배열 리터럴**은
  - 정상 `Period`를 직렬화 한다음, 수정한 스트림 형태
  - 직렬화된 바이트 스트림의 포맷이 궁금할 경우, **자바 객체 직렬화 명세**(Serialization, 6)를 참고
- 위 프로그램을 실행할 경우
  - `Fri Jan 01 12:00:00 PST 1999 - Sun Jan 01 12:00:00 PST 1984`를 출력
- `Period`를 **직렬화**할 수 있도록 선언한것만으로도,
  - 클래스의 **불변식**을 깨뜨리는 객체 생성 가능

### 문제 해결 1 - readObject 내부 검사
- 위 문제를 해결하려면 `Period`의 `readObject` 메서드가
  - `defaultReadObject`를 호출한 다음, **역직렬화**된 객체가 유효한지 검사
- 이 **유효성 검사**에 실패하면
  - `InvalidObjectException`을 던지게 하여 잘못된 **역직렬화**의 발생 방지
- 코드
  ```java
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();

    // 불변식 만족하는지 검사
    if (start.compareTo(end) > 0)
      throw new InvalidObjectException(start + "가 " + end + "보다 늦음");
  }
  ```

### 문제 해결 1 - 추가적인 문제, 가변 공격
- 위 코드로
  - 공격자가 허용되지 않는 `Period` 인스턴스를 생성하는 일을 막을 수 있으나
  - 다른 문제가 존재
- 정상 `Period` 인스턴스에서 시작된 **바이트 스트림** 끝에
  - `private Date`필드로의 참조를 추가하면
  - 가변 `Period` 인스턴스를 만들 수 있음
- 공격자는 `ObjectInputStream`에서 `Period` 인스턴스를 읽은 후
  - 스트림 끝에 추가된 이 **악의적인 객체 참조**를 읽어
  - `Period` 객체의 내부 정보를 읽을 수 있음
- 이 참조로 얻은 `Date`인스턴스들을 수정할 수 있으니,
  - `Period` 인스턴스는 더는 **불변**이 아니게 됨
- 가변 공격의 예
  ```java
  public class MutablePeriod {
    // Period 인스턴스
    public final Period period;

    // 시작 시각 필드 - 외부에서 접근 할 수 없음
    public final Date start;

    // 종료 시각 필드 - 외부에서 접근할 수 없음
    public final Date end;

    public MutablePeriod() {
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);

        // 유효한 Period 인스턴스 직렬화
        out.writeObject(new Period(new Date(), new Date()));

        /*
         * 악의적인 `이전 객체 참조`, 즉 내부 Date 필드로의 참조를 추가
         * 상세 내용은 자바 객체 직렬화 명세의 6.4절 참고
         */
        byte[] ref = { 0x71, 0, 0x7e, 0, 5};
        bos.write(ref); // start
        ref[4] = 4;
        bos.write(ref); // end

        // Period를 역직렬화 이후 Date 참조를 훔친다
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        period = (Period) in.readObject();
        start = (Date) in.readObject();
        end = (Date) in.readOjbect();
      } catch (IOException | ClassNotFoundException e) {
        throw new AssertionError(e);
      }
    }
  }
  ```
  - 위 코드를 실행하면, 공격이 실행됨
  ```java
  public static void main(String[] args) {
    MutablePeriod mp = new MutablePeriod();
    Period p = mp.period;
    Date pEnd = mp.end;

    // 시간 되돌리기
    pEnd.setYear(78);
    System.out.println(p);

    // 60년대로 회귀
    pEnd.setYear(69);
    System.out.println(p);

    // 위 코드의 결과
    // Wed Nov 22 00:21:29 PST 2017 - Wed Nov 22 00:21:29 PST 1978
    // Wed Nov 22 00:21:29 PST 2017 - Sat Nov 22 00:21:29 PST 1969
  }
  ```
- `Period` 인스턴스는 **불변식**을 유지한 채 생성되었지만,
  - 의도적으로 내부의 값을 수정할 수 있음
- 변경할 수 있는 `Period` 인스턴스를 획득한 공격자는
  - 이 인스턴스가 **불변**이라고 가정하는 클래스에 넘겨
  - 보안 문제 발생 가능
- 실제로 `String`이 불변이라는 사실에 기댄 클래스등에서 문제 발생 가능

### 문제의 원인
- `Period`의 `readObject` 메서드가 **방어적 복사**를 충분히 하지 않았기 때문
- 객체를 **역직렬화**할 때는
  - C가 소유해서는 안되는 **객체 참조**를 갖는 필드를
  - 반드시 **방어적으로 복사**해야 함
- 따라서 `readObject`에서는
  - 불변 클래스 안의 모든 `private` 가변 요소를 **방어적으로 복사**해야 함

### 방어적 복사와 유효성 검사를 수행하는 readObject
- 코드
  ```java
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();

    // 가변 요소들을 방어적으로 복사
    start = new Date(start.getTime());
    end = new Date(end.getTime());

    // 불변식을 만족하는지 검사
    if (start.compareTo(end) > 0)
      throw new InvalidObjectException(start + "가 " + end + "보다 늦다");
  }
  ```
- **방어적 복사**를 **유효성 검사**보다 앞에서 수행하며,
  - `Date`의 `clone`메서드는 사용하지 않음
- 두 조치 모두 `Period`를 공격으로부터 보호하는데 필요(ITEM.50)
- 또한 `final` 필드는 **방어적 복사**가 **불가능**
  - `readObject` 메서드를 사요하려면, `start`와 `end`에서 `final` 한정자를 제거하고,
    - 새로운 `readObject`를 적용하면
  - `MutablePeriod` 클래스의 공격도 통하지 않음
- 코드 실행 결과
  ```log
  Wed Nov 22 00:23:41 PST 2017 - Wed Nov 22 00:23:41 PST 2017
  Wed Nov 22 00:23:41 PST 2017 - Wed Nov 22 00:23:41 PST 2017
  ```

### `readObject` 메서드를 사용해야할 때
- 기본 `readObject` 메서드를 써도 좋을지를 판단하는 간단한 방법
- `trasient` 필드를 제외한 **모든 필드**의 값을
  - **매개변수**로 받아 유효성 검사 없이 필드에 대입하는 `public` 생성자를 추가해도 괜찮은가?
    - 답이 **아니오**라면 커스텀 `readObject`를 메서드를 만들어
      - 생성자에서 수행했어야 할 모든 **유효성 검사**와 **방어적 복사**를 수행해야 함
    - 혹은 **직렬화 프록시 패턴**(ITEM.90)을 사용하는 방법도 있음
      - 이 패턴은 **역직렬화**를 안전하게 만드는 데, 필요한 노력을 상당히 경감하므로, 추천함

### `final`이 아닌 직렬화 가능 클래스
- `readObject`와 **생성자**의 공통점
- 마치 생성자처럼 `readObject` 메서드도 **재정의 가능 메서드**를
  - 직접적이나 간접적으로 호출해서는 안됨(ITEM.19)
- 이 규칙을 어겼는데,
  - 해당 메서드가 재정의 되면,
  - 하위 클래스의 상태가 완전히 역직렬화되기 전에 하위 클래스에서 **재정의된 메서드**가 실행
- 프로그램 오작동으로 이어질 것이다.

### 핵심 정리
- `readObject` 메서드를 작성할 때는 
  - 언제는 `public` 생성자를 작성하는 자세여야 함
- `readObject`는 어떤 **바이트 스트림**이 넘어오더라도,
  - 유효한 **인스턴스**를 만들어내야 한다
- **바이트 스터림**이 진짜 **직렬화된 인스턴스**라고 가정해서는 안 됨
- 이번 아이템에서는
  - 기본 직렬화 형태를 사용한 클래스를 예로 들었지만
  - **커스텀 직렬화**를 사용하더라도 모든 문제가 발생할 수 있음
- 이어서 안전한 `readObject` 메서드를 작성하는 지침 4가지
  - `private`이어야 하는 **객체 참조 필드**는 각 필드가 가리키는 객체를
    - 방어적으로 복사하기, 불변 클래스 내의 가변 요소가 여기 속함
  - 모든 **불변식**을 검사하여 어긋나는게 발견되면
    - `InvalidObjectException`을 던짐
    - 방어적 복사 다음에는 반드시 **불변식 검사**가 뒤따라야 함
  - **역직렬화** 후 객체 그래프 전체의 **유효성**을 검사해야 한다면
    - `ObjectInputValidation` 인터페이스를 사용(이 책에는 없는 내용)
  - **직접적**이든 **간접적**이든, **재정의**하는 메서드는 호출하지 말자
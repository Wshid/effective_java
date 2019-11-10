## [ITEM.34] int 상수 대신 열거 타입 사용
### Java의 특수 목적의 참조 타입
- Enum, Annotation
  - Enum : 클래스의 일종
  - Annotation : 인터페이스의 일종

### 열거 타입
- 일정 개수의 상수 값 정의
- 그 외의 값은 허용하지 않는다
- 열거 타입 이전 코드 - **정수 열거 패턴(int enum pattern)**
  ```java
  public static final int APPLE_FUJI = 0;
  public static final int APPLE_PIPPIN = 1;
  public static final int APPLE_GRANNY_SMITH =2;
  ```
- **타입 안전**을 보장할 방법이 없음
- `==`(equal operator)와의 비교에서도 `compiler`는 경고를 보내지 않음
- 안좋은 예시
  ```java
  int i = (APPLE_FUJI - ORANGE_TEMPLE) / APPLE_PIPPIN;
  ```
- 자바가 **정수 열거 패턴**을 위한 별도 namespace를 제공하지 않기 때문에,
  - 접두어를 사용하여 이를 해결함

### 정수 열거 패턴의 단점
- 평범한 상수를 나열하기 때문에
  - compile을 하면 단순히 클라이언트 파일에 기록 됨
- 상수의 값이 **변경**될 경우,
  - 클라이언트도 다시 컴파일을 진행해야 함
- 정수 상수는 **문자열 출력**이 어려움
  - 출력하거나, 디버거로 확인하게 될 때, **단순 숫자로만 표기**
- 같은 정수 열거 그룹이어도, **상수 순회**가 어려움
- 상수가 **몇 개 존재하는지** 확인도 어려움

### 다른 패턴
- 문자열 상수를 사용하는 변형 패턴
- 문자열 열거 패턴(string enum pattern)
- 상수의 의미를 출력할 수 있다는 점은 좋지만,
  - 문자열 상수 이름 대신,
  - 문자열 값을 그대로 **하드코딩**하게 만들기 때문
- 하드 코딩한 문자열 검증이 어려우므로,
  - `Runtime bug`가 발생한다(Compile time 검증이 불가능함)
- 문자열 비교에 따른 성능 저하 역시 발생함

### 열거 타입 예시 코드
- 코드
  ```java
  public enum Apple { FUJI, PIPPIN, GRANNY_SMITH }
  public enum Orange { NAVEL, TEMPLE, BLOOD }
  ```
- `C, C++, C#`과 같은 타언어의 열거 타입과 유사해 보이나,
  - 자바의 열거 타입은 **완전히 다른 형태의 클래스**
  - 타언어의 열거 타입보다 강력함

### 열거 타입의 아이디어
- `Enum` 자체는 클래스
- 상수 하나당 자신의 **인스턴스**를 만들어
  - `public static final` 필드로 공개함
- 밖에서 접근할 수 있는 **생성자를 제공하지 않음**
  - 실상 `final`과 동일함
- **C가 인스턴스를 직접 생성하거나, 확장할 수 없음**
- 열거 타입 선언으로 만들어진 인스턴스는, **오직 하나씩만 존재**
- `Singleton`의 경우 **원소가 하나뿐인 열거 타입**
- 열거 타입은 `Singleton`을 일반화한 형태

### 열거 타입의 장점
- `Compile Time` 타입 안정성
  - 다른 타입의 값을 넘기려 할경우 `Compile Time ERROR` 발생
  - `==`연산자로 비교하는 것과 동일함
- 각자의 이름 공간이 존재, 이름이 같은 상수도 공유 가능하다
  - `Enum` 타입에 새로운 상수 추가, 순서 변경시
    - `recompile`이 필요 없음
  - 공개 되는 것은 단순 **필드 이름**
  - 상수 값이 C로 compile되어 공유되지 않음 => **정수 열거 패턴**의 단점 개선
- 열거타입의 `toString`은, 출력하기에 적합한 문자열 리턴
- 임의의 **메서드**나 **필드** 추가 가능
- 임의의 인터페이스 구현 가능

### 열거 타입의 메서드나 필드 추가하기
- 각 상수에 연관된 데이터를 상수 자체에 내장
- 상수 모임 뿐인 열거 타입이나,
  - 실제로는 **클래스**이기 때문에 가능

### 행성 코드 예시
- 코드
  ```java
  public enum Planet {
      MERCURY(3.302e+23, 2.439e6)
      VENUS(4.869e+24, 6.052e6)
      ...

      private final double mass;
      private final double radius;
      private final double surfaceGravity;

      private static final double G = 6.67300E-11;

      Planet(double manss, double radius) {
          this.mass = mass;
          this.radius = radius;
          surfaceGravity = G * mass / (radius * radius);
      }

      public double mass() { return mass; }
      public double radius() { return radius; }
      public double surfaceGravity() { return surfaceGravity; }

      public double surfaceWeight(double mass) {
          return mass * surfaceGravity;
      }
  }
  ```
  - 열거 타입 상수 각각을 특정 데이터와 연결 지을 때,
    - **생성자**에서 데이터를 받아 **인스턴스 필드**에 저장하면 된다.
- 열거 타입은 근본적으로 **final**이어야 한다(**ITEM.17**)
- 필드를 **public**으로 선언해도 되지만,
  - `private`으로 두고 `public` 접근자 메서드를 두는게 나음(**ITEM.16**)
- `Planet`의 생성자에서 `surfaceGravity`를 미리 계산하는 이유는
  - 최적화를 위함
- 활용 코드
  - 지구의 무게를 입력 받아
    - 여덟 행성에서의 무게를 출력
  ```java
  public class WeightTable {
      public static void main(String[] args) {
          double earthWeight = Double.parseDouble(args[0])
          double mass = earchWeight / Planet.EARTH.surfaceGravity();
          for (Planet p : Planet.values())
            System.out.println("%s의 무게 %f %n", p, p.surfaceWeight(mass));
      }
  }
  ```

### 열거 타입의 특징
- 자신안에 정의된 상수들의 값을
  - **배열**에 담아 반환하는, 정적 메서드 `values`를 제공
  - 값은 선언된 순서로 저장
- 각 열거 타입 값의 `toString` 메서드는
  - **상수 이름**을 **문자열**로 반환
- `toString`을 재정의 할 수도 있음

### 열거 타입에 정의된 상수 제거
- 클라이언트의 영향성
  - 참조 하지 않는 클라이언트 -> 영향 없음
  - 참조 하는 클라이언트 
    - 재컴파일 이후, `debugging message`를 담은 `compile error` 발생
    - 컴파일 x시, `debugging message`가 `Runtime error`로 발생
      - 정수 열거 패턴에서는 존재하지 않는 특징

### 열거 타입의 접근제어자
- 열거 타입 선언 클래스, 혹은 그 패키지에서만 유용할 경우
  - `private`, `package-private` 메서드로 구현
- 널리 쓰이는 열거 타입
  - 톱레벨 클래스로 구성
- 톱레벨 클래스에서만 쓰인다면
  - 해당 클래스의 멤버 클래스(ITEM.24)로 구성
- 예시
  - `java.math.RoundingMode` : 소수 자리의 반올림 클래스
    - `BigDecimal`이 사용함
  - 반올림 개념 자체는 `BigDecimal` 말고도 활용성이 높기 때문에,
    - 톱레벨로 올라감

### 다양한 기능 제공하기
- 상수마다 동작을 달라지게 하고 싶다면
- switch 문을 사용한 예시
  ```java
  public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;

    public double apply(double x, double y) {
      switch(this) {
        case PLUS: return x + y;
        case MINUS: return x - y;
        case TIMES: return x * y;
        case DIVIDE: return x / y;
      }
      throw new AssertionError("알 수 없는 연산: " + this);
    }
  }
  ```
- 마지막 throw는 실제 도달할 수 없음
  - 하지만 기술적으로는 도달 가능하므로,
  - `throw`문을 생략할 시 `compile`도 되지 않음
- 깨지기 쉬운 코드
  - 새로운 상수 추가시에, 해당 `case`문을 추가해야 함
- 혹시라도 추가하지 않게 되면,
  - `Compile`은 되지만
  - `Runtime Error`가 발생하다(`알 수 없는 연산 ...`)
- 상수별로 동작 방식이 다른 코드의 좋은 예시
  - 열거 타입에 `apply` 추상 메서드 선언
  - 각 상수별 클래스 몸체(constant-specific class body)
    - 각 상수별 자신에 맞게 재정의 하는 방법
  - 이를 **상수별 메서드 구현(constant-specific method implementation)**이라 한다.
    ```java
    public enum Operation {
      PLUS { public double apply(double x, double y){return x + y;}},
      MINUS { public double apply(double x, double y){return x - y;}},
      TIMES { public double apply(double x, double y){return x * y;}},
      DEVIDE { public double apply(double x, double y){return x / y;}}

      public abstract double apply(double x, double y);
    }
    ```
    - `apply` 메서드가 상수 바로 옆이므로, 재정의 사실을 까먹을 수 없음
    - `apply`가 추상 메서드이므로, 재정의하지 않았다면 `compile error` 발생

### 상수별 메서드 구현 - 상수별 데이터 결합
- `Operation`의 `toString`을 재정의 하는 예시
  ```java
  public enum Operation {
    PLUS("+") {
      public double apply(double x, double y){ return x+y; }
    }
  },
    MINUS("-") {
        public double apply(double x, double y){ return x-y; }
      }
    },
    TIMES("*") {
        public double apply(double x, double y){ return x*y; }
      }
    },
    DEVIDE("/") {
        public double apply(double x, double y){ return x/y; }
      }
    };

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol;}
    public abstract double apply(double x, double y);
  ```
  ```java
  public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    for(Operation op : Operation.values())
      System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x,y));
  }
  ```

### 열거타입의 함수
- `valueof`
  - 상수 이름을 입력받아 그 이름에 해당하는 상수 반환
  - `toString` 메서드를 재정의 하려면
    - `toString`이 반환하는 문자열을, 다시 열거타입 상수로 변환하는 `fromString` 메서드도 제공해야 함
- `fromString` 예시 코드
  ```java
  private static final Map<String, Operation> stringToEnum =
    Stream.of(values()).collect(toMap(Object::toString, e -> e));
  
  public static Optional<Operation> fromString(String symbol) {
    Return Optional.ofNullable(stringToEnum.get(symbol));
  }
  ```
  - `Operation` 상수가 `stringToEnum` 맵에 추가되는 시점
       - 열거 타입 상수 생성 후 **정적 필드**가 추가 될 때
  - values 메서드가 반환하는 배열 대신, `Stream`을 사용함
    - java8 이전에는  빈 해시맵을 만든 후, `values`가 반환된 배열을 순회하며 [문자열, 열거타입 상수]를 맵에 추가
    - 열거 타입 상수의 경우, 생성자에서 자신의 인스턴스를 맵에 추가할 수 없음
      - `Compile Error` 발생
    - 만약 이 방식이 허용될 경우 `Runtime`에 `NullPointerException`이 발생할 것
  - 열거 타입의 **정적 필드** 중, 열거 타입의 생성자에서 접근 가능한 것은
    - **상수 변수**(ITEM.24)
  - 열거 타입의 생성자가 실행되는 시점에는, **정적 필드**가 초기화 되기 전,
    - 자기 자신을 추가하지 못하게 하는 제약이 필요
    - **열거 타입의 생성자**에서, 같은 **열거 타입**의 다른 상수도 접근 불가
  - `fromString`이 `Optional<Operation>`을 반환한다
    - 주어진 **문자열이 가리키는 연산**이 **존재하지 않을 수 있음**
      - 이를 C에게 알린다
    - 그 상황을 C에서 대처하도록 함

### 상수별 메서드 구현의 단점
- **열거 타입 상수**끼리 코드를 공유하기 어려움
- 급여명세서 예시
  - 사용할 요일을 표현하는 열거 타입
  - 직원의 시간당 임금과 일한 시간을 가지고 계산하는 메서드 존재
  - 오버타임 발생시, 잔업수당
  - 주말에는 무조건 잔업수당 부여
  - `switch...case`를 이용한 코드
    ```java
    enum PayrollDay {
      MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

      private static final int MINS_PER_SHIFT = 8 * 60;

      int pay(int minutesWorked, int payRate) {
        int basePay = minutesWorked * payRate;

        int overtimePay;
        switch(this) {
          case SATURDAY: case SUNDAY:
            overtimePay = basePay / 2;
            break;
          default:
            overtimePay = minutesWored <= MINS_PER_SHIFT ?
              0 : (minutesWored - MINS_PER_SHIFT) * payRate / 2;
        }

        return basePay + overtimePay;
      }
    }
    ``` 
    - 관리 관점에서는 위험한 코드
    - 새로운 값을 열거타입에 추가하는 경우
      - 그 값에 상응하는 case 문도 추가해 주어야 한다
    - 깜빡할 경우, compile 에러도 발생하지 않겠지만, 평일과 같은 임금을 받게 될 것

### 상수별 메서드 구현 예시의 해결책
- 두가지 방법
  - 잔업수당을 계산하는 코드를 모든 상수에 중복하기
  - 계산 코드를 평일용/주말용으로 나누어
    - 각각을 **도우미 메서드**로 작성
    - 각 상수가 이를 호출하도록 하면 됨
- 두 방법 모두, 가독성이 떨어지며, 오류 발생 가능성이 높음
- 세로운 상수를 추가하면서 `overtimePay` 메서드를 재정의하지 않으면
  - 평일용 메서드를 그대로 물려 받음
- 새로운 상수를 추가할때 **잔업수당** 전략을 선택하도록 하기
  - 잔업수당 계산을 `private 중첩 열거 타입(payType)`으로 옮기고
    - `PayrollDay` 열거 타입 생성자에서 적당한 내용을 선택하기
  - `PayrollDay` 열거 타입은
    - 잔업 수당 계산을 **전략 열거 타입**에 위임,
    - `switch`나 **상수별 메서드 구현**이 필요 없음
  - 예시 코드
    ```java
    enum PayrollDay {
      MONDAY(WEEKDAY), TUESDAY(WEEKDAY), WENDESDAY(WEEKDAY),
      THURSDAY(WEEKDAY), FRIDAY(WEEKDAY), SATURDAY(WEEKEND), SUNDAY(WEEKEND);

      private final PayType payType;

      PayrollDay(PayType payType) { this.payType = payType; }

      int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
      }

      enum PayType {
        WEEKDAY {
          int overtimePay(int minsWorked, int payRate) {
            return minsWorked <= MINS_PER_SHIFT ? 0 :
              (minsWorked - MINS_PER_SHIFT) * payRate / 2;
          }
        },
        WEEKEND {
          int overtimePay(int minsWorked, int payRate) {
            return minsWorked * payRate / 2;
          }
        };

        abstract int overtimePay(int mins, int payRate);
        private static final int MINS_PER_SHIFT = 8 * 60;

        int pay(int minsWorked, int payRate) {
          int basePay = minsWorked * payRate;
          return basePay + overtimePay(minsWorked, payRate);
        }
      }
    }
    ```
    
### switch문의 선택
- `switch`문은 **열거 타입의 상수별 동작**을 구현하는데 적합하지 않음
- 하지만, **기존 열거 타입**에 **상수별 동작**을 혼합할 때 좋음
- 예시 코드
  - `Operation` 열거 타입에서, 각 연산의 반대연산을 반환하는 메서드 예시 코드
    ```java
    public static Operation inverse(Operation op) {
      switch(op) {
        case PLUS: return Operation.MINUS;
        case MINUS: return Operation.PLUS;
        case TIMES: return Operation.DEVIDE;
        case DIVIDE: return Operation.TIMES;

        default: throws new AssertionError("알 수 없는 연산 : " + op);
      }
    }
    ```
    - 추가하려는 메서드가
      - 의미상 **열거 타입에 속하지 않을 경우**
    - 열거 타입 안에 포함할 만큼 유용하지 않은 경우에도 사용 가능

### 열거 타입을 써야하는 경우
- 성능은 **정수 상수**와 거의 동일
- 열거 타입의 경우,
  - 메모리에 올리는 공간 및 초기화 시간 소요
  - 체감될 정도는 아님
- **필요한 원소**를 **Compile Time**에 다 알수 있는 상수 집합일 경우
  - `태양계 행성, 한 주의 요일, 체스 말, ...`
  - `메뉴 아이템, 연산 코드, 명령줄 플래그, ...`
- **열거 타입**에 정의된 상수 **개수**가 영원히 **고정 불변**일 필요는 x
  - 열거 타입의 경우
    - 나중에 상수가 추가돼도,
    - **바이너리 수준**에서 호환되도록 설계 됨

### 결론
- **열거 타입**은 **정수 상수**보다
  - 읽기 쉽고, 안전, 강력 함
- 대다수의 열거타입이 **명시적 생성자**나 **메서드** 없이 사용되나,
  - 각 **상수**를 **특정 데이터**와 연결 짓거나,
  - 다르게 동작할 경우, 필요함
- **하나의 메서드**가 **상수별**로 다르게 동작해야하는 경우
  - `switch`문 대신, **상수별 메서드 구현**을 활용할 것
- 열거 타입 일부가 **같은 동작**을 공유할 경우
  - **전략 열거 타입 패턴**을 사용
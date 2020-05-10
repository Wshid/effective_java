## [ITEM.78] 공유 중인 가변 데이터는 동기화

### Thread
- 스레드는 여러 활동을 **동시 수행** 가능하게 함
- 하지만 **동시성 프로그래밍**은
  - **단일 스레드 프로그래밍**보다 복잡함
- 잘못될 수 있는 일이 늘어나고
  - 문제를 재현하기도 어렵기 때문
- 자바 플랫폼에 내재되어 있을 뿐 아니라,
  - 현재 어디서나 쓰이는 **멀티코어 프로세서**의 힘을 제대로 활용해야 함

### synchronized 키워드와 동기화의 중요한 기능 1번째
- 해당 메서드나 블록을
  - **한 번에 한 스레드**씩 수행하도록 보장
- 많은 프로그래머가
  - 동기화를 **베타적 실행**
    - 한 스레드가 변경하는 중이라,
      - 상태가 일관되지 않은 순간의 객체를
    - 다른 스레드가 보지 못하게 막는 용도로 생각한다.
- 한 객체가 **일관된 상태**를 가지고 생성되고(ITEM.17),
  - 이 객체에 접근하는 **메서드**는 그 객체에 `Lock`을 건다.
- `lock`을 건 메서드는
  - 객체의 상태를 **확인**하고, 필요하면 숮ㅇ한다.
- 즉, 객체를 하나의 일관된 상태에서
  - 다른 일관된 상태로 변화시킨다.
- 동기화를 제대로 사용하려면
  - 어떤 메서드도 **이 객체의 상태가 일관되지 않은 순간**을 볼 수 없을 것

### 동기화의 중요한 기능 2번째
- 동기화 없이는
  - 한 스레드가 만든 **변화**를 **다른 스레드**에서 확인하지 못할 수 있음
- 동기화는 **일관성**이 깨진 상태를 볼 수 없게 하는 것은 물론,
  - 동기화된 메서드나 블록에 들어간 스레드가 **같은 락의 보호**하에 수행된
  - **모든 이전 수행의 최종 결과**를 보게 해준다.

### atomic
- `long`과 `double`외의 변수가 읽고 쓰는 동작은 원자적(JLS, 17.4, 17.7)
- 여러 스레드가 **같은 변수**를 동기화 없이 수정하는 중이라도,
  - 항상 여러 스레드가 정상적으로 저장한 값을 읽어온다는 의미

### 위험한 발상
- 성능을 높이려면 **원자적 데이터**를 읽고 쓸때 동기화 하지 말아야 한다?
- 자바 언어 명세에는
  - 스레드가 필드를 읽을 때
    - 항상 **수정이 완전히 반영된**값을 얻는다고 보장하지만,
  - 한 스레드가 저장한 값이
    - 다른 스레드에게 **보이는가**는 보장하지 않음
- 동기화는 **베타적 실행**뿐 아니라,
  - 스레드 사이의 **안정적인 통신**에 꼭 필요한 부분
- 이는 한 스레드가 만든 변화가
  - 다른 스레드에게 **언제, 어떻게** 보이는지를 규정한
  - 자바 메모리 모델 때문(JLS, 17.4)

### 동기화 실패의 영향과 예시
- 공유 중인 **가변 데이터**를
  - 원자적으로 읽고 쓸 수 있을지라도,
- 동기화에 실패하면, 좋지 않은 결과 발생
- 예시 - 다른 스레드를 멈추는 작업
  - `Thread.stop` 메서드는 안전하지 않아
    - 이미 오래전에 `deprecated`
    - 해당 메서드를 사용하면, 데이터가 훼손될 수 있음
    - **따라서 위 메서드는 사용하지 말 것**
  - 올바른 방법
    - 첫 번째 스레드는
      - 자신의 `boolean` 필드를 **polling**하면서 그 값이 `ture`가 되면 멈춘다.
      - 이 필드를 `false`로 초기화해놓고,
        - 다른 스레드에서 이 스레드를 멈추고자 할때, `true`로 변경
      - `boolean` 필드를 읽고/쓰는 작업은 **원자적**이라,
        - 어떤 프로그래머는 이런 필드 접근시 **동기화를 제거**하기도 함

### 잘못된 코드
- 코드
  ```java
  public class StopThread {
      private static boolean stopRequested;

      public static void main(String[] args) throws InterruptedException {
          Thread backgroundThread = new Thread(() -> {
              int i = 0;
              while (!stopRequested)
                i++;
          });
          backgroundThread.start();

          TimeUnit.SECONDS.sleep(1);
          stopRequested = true;
      }
    }
  ```
- 이 프로그램은 1초 뒤 종료 될까?
  - 메인 스레드가 1초후 `stopRequested=true`로 설정하면
  - `backgroundThread`가 반복문을 빠져나올 것처럼 보이지만, 그렇지 않음
- 원인은 **동기화**에 있음
  - 동기화를 하지 않으면 **메인 스레드**가 수정한 값을
    - **백그라운드 스레드**가 언제 확인할지 **보증할 수 없음**
- 동기화가 빠지면
  - 가상머신이 다음과 같은 **최적화**를 수행할 수도 있음
  ```java
  // 원래 코드
  while(!stopRequested)
    i++;

  // 최적화된 코드
  if (!stopRequested)
    while (true)
      i++;
  ```
- 위 코드는 실제 `OpenJDK` 서버 VM이 실제로 적용하는 `hoisting`(끌어올리기) 최적화 기법
- 이 결과, 프로그램은 `liveness failure`(응답 불가) 상태가 되어, 더 이상 진전되지 않음

### 동기화를 적절히 적용한 코드
- `stopRequested` 필드를 동기화해 접근하기
- 코드
  ```java
  public class StopThread {
      private static boolean stopRequested;

      private static synchronized void requestStop() {
          stopRequested = true;
      }

      private static synchronized boolean stopRequested() {
          return stopRequested;
      }

      public static void main(String[] args) throws InterruptedException {
          Thread backgroundThread = new Thread(() -> {
              int i= 0;
              while (!stopRequested())
                i++;
          });
          backgroundThread.start();

          TimeUnit.SECONDS.sleep(1);
          requestStop();
      }
  }
  ```
- 쓰기 메서드(`requestStop`)와 읽기 메서드(`stopRequested`)를 모두 동기화
- 쓰기 메서드만 동기화하는 것은 충분하지 않음
  - **모두를 동기화 하지 않으면, 동작을 보장하지 않음**
- 어떤 기기에서는 둘 중 하나만 동기화 해도, 동작하는 듯 보이지만,
  - 겉 모습에 속지 말 것
- 사실 두 메서드는 단순하기 때문에
  - **동기화 없이도** 원자적으로 동작
- 동기화는 **베타적 수행**과 **스레드 간 통신**의 두가지 기능을 수행하는데,
  - 이 코드에서는 그중 **통신 목적**으로 사용됨

### 속도가 더 빠른 대안 - volatile
- 반복문에서 매번 **동기화**하는 비용이 크진 않지만, 속도가 더 빠른 대안
- `stopRequested` 필드를 `volatile`로 선언하면, 동기화를 생략해도 됨
- `volatile` 한정자는 베타적 수행과는 상관 없으나,
  - 항상 가장 최근에 기록된 값을 읽도록 보장
- 코드
  ```java
  public class StopThread {
      private static volatile boolean stopRequested;

      public static void main(String[] args) throws InterruptedException {
          Thread backgroundTrhead = new Thread(() -> {
              int i = 0;
              while (!stopRequested)
                i++;
          });
          backgroundTrhead.start();

          TimeUnit.SECONDS.sleep(1);
          stopRequested = true;
      }
  }
  ```

### volatile은 주의해서 사용
- 예시 - 일련번호를 생성할 의도로 작성된 메서드
  ```java
  private static volatile int nextSerialNumber = 0;

  public static int generateSerialNumber() {
      return nextSerialNumber++;
  }
  ```
- 위 메서드는 매번 고유한 값을 반환한 의도로 만들어짐
  - 값이 `2^32`를 넘어가지 않는 경우
- 이 메서드의 상태는 `nextSerialNumber`라는
  - 단 하나의 **필드**로 결정되는데,
  - **원자적**으로 접근할 수 있고, 어떤 값이든 허용함
- 굳이 **동기화**하지 않더라도 **불변식**을 보호할 수 있어 보이나,
  - 이 역시 동기화 없이는 정상 작동하지 않음

### volatile 예시의 문제 원인
- `++`(증가 연산자)
- 이 연산자는 코드상으로는 하나지만,
  - 실제로는 `nextSerialNumber` 필드에 두 번 접근
    - 먼저 값을 읽고, 그런 다음 `1`증가한 새로운 값을 저장하는 과정
- 만약 두 번째 스레드가
  - 이 두 접근 **사이를 비집고 들어와** 값을 읽어가면
  - 첫번째 스레드와 동일한 값을 돌려받게 됨
- 잘못된 결과를 계산해내는 이런 오류를
  - **safety failure**(안전 실패)라고 한다.

### 1차적인 해결 방법 - synchronized
- `generateSerialNumber` 메서드에
  - `synchronized` 한정자를 붙이면, 해당 문제는 해결 됨
- 동시에 호출해도 서로 간섭하지 않으며
  - 이전 호출이 변경한 값을 읽게 된다는 의미
- 메서드에 `synchronized`를 붙였다면,
  - `nextSerialNumber` 필드에서는 `volatile`을 **제거 해야 함**
- 이 메서드를 더 견고하게 하려면
  - `int` -> `long`
  - `nextSerialNumber`가 최댓값에 도달하면 **예외 발생** 처리

### 2차적인 해결 방법 - AtomicLong
- **ITEM.59**의 조언에 따라
  - `java.util.concurrent.atomic` 패키지의 `AtomicLong` 사용
- 이 패키지에는 `lock-free`(락 없이도)
  - **스레드 안전한 프로그래밍**을 지원하는 클래스들이 존재
- `volatile`은 동기화의 두 효과 중
  - **통신쪽만 지원**하지만,
  - 이 패키지는 **원자성**(베타적 실행)까지 지원
- 성능도 **1차적 해결방법**보다 우수
- 예시 코드
  ```java
  private static final AtomicLong nextSerialNum = new AtomicLong();

  public static long generateSerialNumber() {
      return nextSerialNum.getAndIncrement();
  }
  ```

### 동기화 문제를 회피하는 방법
- 가장 좋은 방법은
  - 애초에 **가변 데이터**를 **공유하지 않는 것**
- **불변 데이터**(ITEM.17)만 공유하거나,
  - 아무것도 공유하지 말 것
- 가변 데이터는
  - **단일 스레드**에서만 사용할 것
- 이 정책을 받아들였을 경우,
  - 그 사실을 문서에 남겨 **유지보수 과정**에서도
  - 정책이 계속 지켜지도록 하는 것이 중요
- 또한, 사용하려는 **프레임워크**와 **라이브러리**를 깊이 이해하는 것도 중요
- 이런 외부 코드가
  - 인지하지 못한 스레드를 수행하는 **복병**으로 작용하는 경우도 존재

### 수정 이후 다른 스레드 공유시
- 한 스레드가 데이터를 다 **수정한 후**,
  - 다른 스레드에 **공유**할 경우
  - 해당 객체에서 **공유하는 부분만 동기화**
- 그 객체를 다시 수정할 일이 생기기 전까지는
  - 다른 스레드들은 **동기화 없이** 자유롭게 값을 읽어갈 수 있음
- 이런 객체를 **effectivily immutable**(사실상 불변)이라 함
- 스레드에 이런 객체를 건네는 행위를 **safe publication**(안전 발행)이라 함
- 객체를 **안전하게 발행**하는 방법
  - 클래스 **초기화** 과정에서 객체를 다음과 같이 저장
    - **정적 필드**
    - **volatile 필드**
    - **final 필드**
    - **보통의 lock**을 통해 접근하는 필드
  - 동시성 컬렉션(ITEM.81)에 저장하는 방법

### 정리
- 여러 스레드가 **가변 데이터**를 **공유**한다면
  - 그 데이터를 읽고/쓰는 동작은 반드시 **동기화**해야 함
- 동기화 하지 않을 경우
  - 한 스레드가 수행한 변경을
  - 다른 스레드가 보지 못할 수 있음
- 공유되는 가변 데이터를 동기화하는데 **실패**하면
  - **응답 불가 상태**에 빠지거나,
  - **안전 실패**로 이어질 수 있음
- 위 문제는 **디버깅 난이도가 가장 높음**
  - 간헐적이거나, 특정 타이밍에만 발생할 수 있으며,
  - VM에 따라 현상이 다를 수 있기 때문
- **베타적 실행**은 필요 없고
  - **스레드 상호 통신**만 필요하다면, `volatile` 한정자만으로 동기화 가능
  - 단, 올바르게 사용하기는 까다로움

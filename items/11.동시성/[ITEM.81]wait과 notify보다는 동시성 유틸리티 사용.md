## [ITEM.81] wait과 notify보다는 동시성 유틸리티 사용

### wait과 notify의 사용
- 중요도가 예전보다 못함
- 사용해야할 이유가 많이 줄어든 상태
- `java 5`에서 도입된 고수준의 **동시성 유틸리티**가 이전이라면,
  - `wait`과 `notify`로 하드코딩해야 했던 전형적인 일들을
  - **대신 처리**해주기 때문
- `wait`과 `notify`는 올바르게 사용하기가 어려우므로
  - **고수준 동시성 유틸리티**를 사용하기

### `java.util.concurrent`의 구성
- `java.util.concurrent`의 고수준 유틸리티는 세가지로 구성됨
  - 실행자 프레임워크
    - ITEM.80
  - 동시성 컬렉션(concurrent collection)
  - 동기화 장치(synchronizer)

### 동시성 컬렉션
- `List`, `Queue`, `Map`과 같은 **표준 컬렉션 인터페이스**에
  - **동시성**을 가미해 구현한 **고성능 컬렉션**
- 높은 동시성에 도달하기 위해
  - **동기화**를 각자의 **내부**에서 수행(ITEM.79)
- 동시성 컬렉션에서 **동시성** 무력화는 물가능하며
  - 외부에서 **락**을 추가로 사용할 경우, 오히려 속도가 **느려짐**
- 동시성 컬렉션에서 동시성 무력화가 불가능하기 때문에
  - 여러 메서드를 **원자적으로 묶어 처리**하는 것도 불가능
- 기본 **여러 동작**을 하나의 **원자적 동작**으로 묶는
  - **상태 의존적 수정** 메서드들이 추가 됨
- 이 메서드들은 유용하기에
  - `java 8`에서는 **일반 컬렉션 인터페이스**에도 `defatul method`(ITEM.21)형태로 추가 됨
- 예시
  - `Map::putIfAbsent(key, value)`메서드는
    - 주어진 키에 매핑된 값이 아직 **없을 떄**만 새 값을 집어 넣음
    - 기존 값이 있었다면, 해당 값을 반환
    - 없었다면 `null`을 반환
    - 위 메서드 덕분에 **스레드 안전한 정규화 맵**(canonicalizing map)을 쉽게 구현할 수 있음
    - `String.intern`의 동작을 구현한 메서드(최적은 아닌 방법)
      ```java
      private static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

      public static String intern(String s) {
        String previousValue = map.putIfAbsent(s, s);
        return previousValue == null ? s : previousValue;
      }
      ```
    - `ConcurrentHashMap::get`과 같은 **검색 기능 최적화**
      - `get`을 먼저 호출하여, 필요할 때만 `putIfAbsent`를 호출하면 더 빠름
    - `ConcurrentMap`으로 구현한 동시성 정규화 맵 - 더 빠른 방법
      ```java
      public static String intern(String s) {
        String result = map.get(s);
        if (result == null) {
          result = map.putIfAbsent(s, s);
          if(result == null)
            result = s;
        }
        return result;
      }
      ```

### ConcurrentHashMap의 사용
- `ConcurrentHashMap`은 **동시성**이 뛰어나며, **속도**가 빠름
- `String.intern`의 경우에도, 한 컴퓨터에서 6배 정도의 성능을 보여줌
- 동시성 컬렉션은 동기화한 컬렉션보다 좋은 선택
  - `Collections.synchronizedMap`보다는 `ConcurrentHashMap`을 사용하는게 훨씬 좋음
- **동기화된 맵** -> **동시성 맵**으로 교체하는 것만으로
  - 동시성 어플리케이션의 성능이 극적 개선 됨

### 컬렉션 인터페이스의 확장
- 컬렉션 인터페이스 중 일부는
  - 작업이 **성공적으로 완료**될 때까지 기다리도록(차단되도록) 확장됨
- 예시
  - `Queue`를 확장한 `BlockingQueue`에 추가된 메서드 중 `take`
    - 큐의 첫 원소를 꺼내는 메서드
  - 큐가 비었다면 **새로운 원소**가 추가될 때까지 기다림
  - 이 특성 때문에 `BlockingQueue`는 **작업 큐**(생산자-소비자 큐)로 쓰기에 적합
  - 작업 큐는
    - 하나 이상의 **producer** 스레드가 작업(work)을 추가하고,
    - 하나 이상의 **consumer** 스레드가 큐에 있는 작업을 꺼내 처리하는 형태
  - `ThreadPoolExecutor`를 포함한 대부분의 **실행자 서비스**(ITEM.80) 구현체에서
    - `BlockingQueue`를 사용함

### 동기화 장치
- 동기화 장치는 스레드가 다른 스레드를 **기다릴 수 있게 하여**
  - 서로 작업을 **조율**할 수 있도록 해줌
- 가장 자주 쓰이는 동기화 장치는
  - `CountDownLatch`와 `Semaphore`
- `CyclicBarrier`와 `Exchanger`는 그보다 덜 사용 됨
- 가장 강력한 동기화 장치는 `Parser`이다

### CountDownLatch
- `CountDownLatch`는 **일회성 장벽**으로
  - 하나 이상의 스레드가 또 다른 하나이상의 스레드 작업이 **끝날때 까지** 기다림
- `CountDownLatch`의 유일한 **생성자**는 `int`값을 받으며,
  - 이 값이 래치의 `countDown` 메서드를
    - 몇 번 호출해야 대기 중인 스레드를 깨우는지 결정
- 유용한 기능 구현 예시
  - 어떤 동작들을 **동시 시작**하여, 모두 완료까지 시간을 재는 프레임워크 개발
  - 이 프레임워크는 **메서드 하나**로 구성되며,
    - 이 메서드는 동작을 실행할 **실행자**와
    - 동작을 몇 개나 동시에 수행할 수 있는지를 뜻하는 **동시성 수준**(concurrency)를 인자로 받음
  - **타이머 스레드**가 시계를 시작하기 전에
    - 모든 **작업자 스레드**는 동작을 수행할 **준비**를 마침
  - 마지막 작업자 스레드가 준비를 마치면
    - 타이머 스레드가 **시작 방아쇠**를 당겨, **작업자 스레드**가 일을 시작하게 함
    - 마지막 작업자 스레드가 동작을 마치자마자,
      - 타이머 스레드는 시계를 멈춤
    - 이상의 기능을 `wait`과 `notify`로 구현하려면 난해하지만,
      - `CountDownLatch`를 사용하면 직관적인 구현이 가능함
  - 코드
    ```java
    public static long time(Executor executor, int concurrency, Runnable action) throws InterruptedException {
      CountDownLatch ready = new CountDownLatch(concurrency);
      CountDownLatch start = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(concurrency);

      for (int i = 0; i < concurrency; i++) {
        executor.execute(() -> {
          // 타이머에게 준비를 마쳤음을 알림
          ready.countDown();
          try {
            // 모든 작업자 스레드가 준비될 때까지 기다림
            start.await();
            action.run();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            done.countDown();
          }
        });
      }

      ready.await(); // 모든 작업자가 준비될 때까지 기다림
      long startNanos = System.nanoTime();
      start.countDown(); // 작업자들을 깨운다
      done.await(); // 모든 작업자가 일을 마칠때까지 기다림
      return System.nanoTime() - startNanos;
    }
    ```
    - 위 코드는 `CountDownLatch`를 3개 사용
    - `ready latch`는 작업자 스레드들이 **준비 완료**되었음을 **타이머 스레드**에 통지할 때 사용
    - 통지를 끝낸 작업자 스레드들은, `start latch`가 열리기를 기다림
    - 마지막 작업자 스레드가 `ready.countDown`을 호출하면
      - 타이머 스레드가 시작 시각을 기록하고
      - `start.countDown`을 호출하여, 기다리던 작업자 스레드를 깨운다
    - 직후, 타이머 스레드는 `done`이 열리기를 기다림
    - `done latch`는 마지막 남은 작업자 스레드가 동작을 마치고,
      - `done.countDown`을 호출하면 열림
    - 타이머 스레드는 `done latch`가 열리자마자 종료시간을 기록함
  - `time` 메서드에 넘겨진 실행자(executor)는
    - `concurrency` 매개변수로 지정한 **동시성 수준**만큼의 스레드를 생성할 수 있어야 함
    - 그렇지 못할 경우, 이 메서드는 끝나지 않게된다.
    - 이런 상태를 가지고 `thread starvation deadlock`(기아 교착 상태)라고 한다.
  - `InterruptedException`을 캐치한 작업자 스레드는
    - `Thread.currentThread().interrupt()` 관용구를 사용해 `interrupt`를 되살리고,
    - 자신은 `run`메서드에서 빠져나오게 됨
    - 위와 같은 방법을 해야 실행자가 **인터럽트**를 적절하게 처리할 수 있음

### Systme.nanoTime
- 이 코드에서 `System.nanoTime`메서드를 사용하여, 시간을 잰다.
- 시간 간격을 잴 때는 항상 `System.currentTimeMillis`가 아닌
  - `System.nanoTime`을 사용해야 함
- 더 **정확**하고, **정밀**하며
  - 시스템 실시간 시계의 시간보정에 영향을 받지 않음
- 이 예제의 코드는 작업의 충분한 시간(`1초 이상`)이 걸리지 않는다면
  - 정확한 시간을 측정할 수 없음
- **정밀한 시간 측정**은 매우 어려운 작업이라
  - 꼭 해야할 경우, `jmh`와 같은 **특수 프레임워크**를 사용하여야 함

### 위 예제의 최적화
- 동시성 유틸리티의 예시만 보여준 내용
- 위의 카운트다운 래치 3개는
  - **CyclicBarrier**(혹은 **Phaser**) 인스턴스 하나로 대체 가능
  - 위와 같이 하면, 코드는 더 명료해지나, 이해하기는 더 어려울 것

### 레거시 코드
- 새로운 코드라면 `wait`과 `notify`가 아닌 **동시성 유틸리티**를 사용해야 함
- 하지만 어쩔 수 없이 레거시 코드를 다뤄야하는 상황
- `wait` 메서드는
  - **스레드**가 어떤 조건이 충족되기를 **기다리게 할 때** 사용
- `lock` 객체의 `wait` 메서드는
  - 반드시 그 객체를 잠근 **동기화 영역 안**에서 호출하여야 함

### wait 메서드를 사용하는 방법 및 유의점
- `wait` 메서드를 사용하는 표준 방식
  ```java
  synchronized (obj) {
    while (<조건이 충족되지 않음>)
      obj.wait(); // 락을 놓고, 깨어나면 다시 잡음
    
    ... // 조건이 충족되었을 때 동작 수행
  }
  ```
- `wait` 메서드를 사용할 때는
  - 반드시 **대기 반복문**(wait loop) 관용구를 사용하기
- **반복문 밖에서는 절대로 호출하면 안됨**
  - 위 반복문은, `wait` 호출 전후로 조건이 만족하는지를 검사하는 역할을 한다.
- 대기 전에 **조건을 검사**하여,
  - 조건이 이미 충족되었다면 `wait`을 건너뛰게 하는 이유는
  - **응답 불가 상태**를 예방하기 위함
- 조건이 충족되었음에도
  - 스레드가 `notify`나 `notifyAll` 메서드를 먼저 호출한 후, **대기 상태**로 빠지면
  - 그 스레드를 다시 깨울 수 있다고 보장할 수 없음

### 대기 후 조건 검사 - 안전 실패 방어
- 대기 후에 조건을 검사하여, **조건 충족이 되지 않았**을 경우,
  - 다시 대기하게 하는 이유는
  - **안전 실패**를 막기 위한 목적
- 만약, 조건이 충족되지 않았음에도
  - 스레드가 동작을 이어가면,
  - **락**이 보호하는 **불변식**을 깨뜨릴 위험이 존재

### 조건이 불만족해도, 스레드가 깨어있는 상황(4)
- 스레드가 `notify`를 호출한 다음,
  - 대기중이던 스레드가 **깨어나는 사이**에
  - 다른 스레드가 락을 얻어, 그 락이 보호하는 상태를 변경함
- 조건이 만족하지 않아도,
  - 다른 스레드가 실수로 혹은 악의적으로 `notify`를 호출
  - **공개된 객체**를 락으로 사용해 대기하는 클래스는
    - 이런 위험에 노출됨
  - **외부 노출 객체의 동기화**된 메서드 안에서 호출하는 `wait`은
    - 모두 이 문제에 영향을 받게 됨
- 깨우는 스레드는 지나치게 **관대**해서,
  - 대기 중인 스레드 중, 일부만 조건이 충족되어도
  - `notifyAll`을 호출해 **모든 스레드**를 깨울 수 있음
- 허위 각성(`spurious wakeup`) 현상
  - 대기 중인 스레드가 드물게 `notify`없이도 깨어나는 경우가 존재

### notify보다  notifyAll
- `notify`와 `notifyAll` 중 무엇을 선택하는지에 대한 문제
  - `notify` : 스레드 하나만 깨운다
  - `notifyAll`은 모든 스레드를 깨움
- 일반적으로 언제나 `notifyAll`을 사용하는게, 합리적이고 안전함
- 깨어나야 하는 모든 스레드가 깨어남을 보장하니
  - 항상 **정확한 결과**를 얻을 수 있음
- 다른 스레드까지 깨어날수도 있지만,
  - 그것이 프로그램에 정확성에는 **영향을 주지 않음**
- 깨어난 스레드들은, 기다리던 조건이 **충족**되었는지를 확인하여
  - 충족되지 않았다면 **다시 대기**하기 때문
- 모든 스레드가 **같은 조건**을 기다리고
  - 조건이 한 번 충족될 때마다, **하나의 스레드**만 혜택을받을 수 있다면
  - `notifyAll`대신 `notify`를 사용하여 최적화 가능
- 하지만 위의 전제조건들이 만족될지라도,
  - `notify` 대신 `notifyAll`을 사용해야하는 이유가 존재
  - **외부로 공개된 객체**에 대해
    - 실수 혹은 악의적으로 `notify`를 호출하는 상황에 대비하기 위해
    - `wait`을 반복문 안에서 호출했듯,
      - `notify` 대신 `notifyAll`을 사용하면, **관련 없는 스레드**가
        - 실수로 혹은 악의적으로 `wait`을 호출하는 공격으로부터 보호 가능
    - 그런 스레드가 중요한 `notify`를 삼겨버리는 경우
      - 꼭 깨어나야하는 스레드가 **영원 대기**할 수 있기 때문

### 정리
- `wait`과 `notify`를 직접 사용하는 것을
  - **동시성 어셈블리 언어**로 프로그래밍하는 것에 비유할 수 있음
- 반면, `java.util.concurrent`는 **고수준 언어**에 비유 가능
- 코드를 새로 작성한다면
  - `wait`과 `notify`를 사용할 이유가 없음
  - 이들을 사용하는 **레거시 코드**를 유지보수 해야한다면
  - `wait`은 항상 **표준 관용구**에 따;라
    - `while`문 안에서 호출해야 함
- 일반적으로 `notify`보다 `notifyAll`을 사용해야 함
- 혹시라도 `notify`를 사용한다면
  - **응답 불가 상태**에 빠지지 않도록 주의
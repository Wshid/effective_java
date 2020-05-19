## [ITEM.79] 과도한 동기화 회피

### 과도한 동기화를 하지 말아야 하는 이유
- 성능 저하
- 교착상태 유발
- 예측할 수 없는 동작 유발

### 응답 불가와 안전 실패 회피
- 응답 불가와 안전 실패를 피하려면  
  - **동기화 메서드**나 **동기화 블록**안에서는
  - 제어를 절대 C에게 **양도하면 X**
- 예를 들어
  - 동기화된 영역 안에서는 **재정의할 수 있는 메서드**는 호출해서는 안되며,
  - C가 넘겨준 함수 객체(ITEM.24)를 호출해서는 안됨
- 동기화된 영역을 포함한 **클래스** 관점에서는
  - 이런 메서드들은 모두 바깥 세상에서 정의된 것
- 그 메서드가 무슨 일을 할지 알 수 없으며,
  - **통제가 불가능**하기 때문
- **alien method**(외계인 메서드)가 하는 일에 따라
  - **동기화된 영역**은 예외를 유발하거나, 교착상태 또는 데이터 훼손이 발생

### alien method 예시
- 어떤 `Set`을 감싼 래퍼 클래스이고,
  - 그 클래스의 C는 집합에 원소가 추가되면 알림을 받을 수 있음
  - **관찰자 패턴**
- 핵심만 보이기 위해 원소가 제거될 때 알려주는 메서드는 생략
- ITEM.18에서 사용한 `ForwardingSet`을 활용
- 동기화 블록 안에서 alien method를 호출하는 코드
  ```java
  public class ObservableSet<E> extends ForwardingSet<E> {
    public ObservableSet(Set<E> set) {
      super(set);
    }

    private final List<SetObserver<E>> observers = new ArrayList<>();

    public void AddObserver(SetObserver<E> observer) {
      synchronized(observers) {
        observers.add(observer);
      }
    }

    public boolean removeObserver(SetObserver<E> observer) {
      synchronized(observers) {
        return observers.remove(observer);
      
    }

    private void notifyElementAdded(E element) { // 문제가 되는 코드
      synchronized(observers) {
        for(SetObserver<E> observer : observers)
          observer.added(this, element);
      }
    }

    @Override public boolean add(E element) {
      boolean added = super.add(element);
      if (added)
        notifyElementAdded(element);
      return added;
    }

    @Override public boolean addAll(Collection<? extends E> c) {
      boolean result = false;
      for(E element : c)
        result != add(element); // notifyElementAdded를 호출
      return result;
    }
  }
  ```
- 관찰자들은 `addObserver`나 `removeObserver` 메서드를 호출하여,
  - 구독을 신청하거나 해지
- 두 경우의 모두 다음 **콜백 인터페이스**의 인스턴스를 메서드에 건넴
  ```java
  @FunctionalInterface public interface SetObserver<E> {
    // ObservableSet에 원소가 더해지면 호출
    void added(ObservableSet<E> set, E element);
  }
  ```
- 위 인터페이스는 구조적으로
  - `BiConsumer<ObservableSet<E>, E>`와 동일
  - 그럼에도 **커스텀 함수형 인터페이스**를 지정한 이유는,
    - 이름이 더 직관적이고, **다중 콜백**을 지원하도록 확장할 수 있기 때문
  - 단, `BiConsumer`를 그대로 사용했어도, 정상적으로 동작

### alien method의 결함과 비정상동작 1
- 겉으로 보기엔 `ObservableSet`은 잘 동작할 것 처럼 보인다.
- 잘 동작하는 예시
  ```java
  public static void main(String[] args) {
    ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());

    set.addObserver((s, e) -> System.out.println(e));
    for(int i = 0; i < 100; i++)
      set.add(i);
  }
  ```
 - 잘 동작하지 않는 예시
   - 집합에 추가된 정숫값을 출력하다가, 그 값이 23일경우, 자기 자신을 제거하는 관찰자 추가
   ```java
   set.addObserver(new SetObserver<>() {
     public void added(ObservableSet<Integer> s, Integer e) {
       System.out.println(e);
       if ( e == 23)
        s.removeObserver(this);
     }
   })
   ```
   - 람다를 사용하지 않고, **익명 클래스**를 그대로 사용함
     - `s.removeObserver` 메서드에 **함수 객체 자신**을 넘겨야 하기 때문
     - 람다는 **자기 참조가 불가능**
  - 위 프로그램은 `0 ~ 23`을 출력한 후
    - 관찰자 자신을 구독해지한 다음 조용ㅇ히 종료됨
  - 하지만 실제 구동시,
    - 23까지 출력 이후 `ConcurrentModificationException`이 발생함
- `ConcurrentModificationException`의 발생 원인
  - 관찰자의 `added` 메서드 호출이 일어난 시점이
    - `notifyElementAdded`가 관찰자들의 리스트를 **순회하는 도중**이기 때문
  - `added` 메서드는 `ObservableSet::removeObserver` 메서드를 호출하고,
    - 이 메서드는 `observers.remove` 메서드를 호출함
    - 여기서 문제 발생
- 리스트에서 원소를 제거하려고 하는데,
  - 현재 이 리스트를 순회하는 도중. -> **허용되지 않은 동작**
- `notifyElementAdded` 메서드에서 수행하는 순회는
  - **동기화 블록 안**에 있기 때문에
    - **동시 수정**이 일어나지 않도록 보장하지만,
  - 정작 자신이 **콜백**을 거쳐 돌아와 **수정하는 것**까지 막지는 못함

### alien method의 결함과 비정상동작 2
- 구독해지를 하는 관찰자를 작성할 때,
  - `removeObserver`를 직접 호출하지 않고,
  - 실행자 서비스(`ExecutorService`)를 사용하여 다른 스레드에게 부탁(ITEM.80)
- 쓸데 없이 백그라운드 스레드를 사용하는 관찰자
  ```java
  set.addObserver(new SetObserver<>() {
    public void added(ObservableSet<Integer> s, Integer e) {
      System.out.println(e);
      if( e == 23) {
        ExecutorService exec = Executors.newSingleThreadExcecutor();
        try {
          exec.submit(() -> s.removeObserver(this)).get();
        } catch (ExecutionException | InterruptedException ex) {
          throw new AssertionError(ex);
        } finally {
          exec.shutdown();
        }
      }
    }
  })
  ```
  - `catch`구문 하나에서 두가지 예외를 잡는다
    - `multi-catch`는 `java 7:`에서 동작
  - 똑같이 처리해야하는 예외가 여러개 일때, 프로그램 크기를 줄이고, 가독성을 개선함
- 위 프로그램을 실행하면, **예외**는 나지 않지만, **교착상태**가 발생
- 백그라운드 스레드가 `s.removeObserver`를 호출하면
  - 관찰자를 **잠그려 시도**하지만, 락을 얻을 수 없는 상황
  - **메인 스레드**가 이미 락을 가지고 있기 때문
  - 또한, **메인 스레드**는 **백그라운드 스레드**가 관찰자를 제거하기만을 기다림
    - 교착 상태 발생
- 사실 관찰자가, 자신을 구독해지하는데, 굳이 **백그라운드 스레드**를 이용할 이유가 없지만,
  - 여기서 보인 문제 자체는 의미가 있음
- 실제 시스템(GUI 툴킷)에서도
  - **동기화된 영역**안에서 **외계인 메서드**를 호출하는 교착상태는 빈번히 일어남

### 문제의 시사점
- 위 두가지 예시(예외 및 교착상태)는 운이 좋은 상황
  - **동기화 영역**이 보호하는 자원(관찰자)은
  - 외계인 메서드(`added`)가 호출될 때, 일관된 상태였기 때문
- 똑같은 상황이지만, **불변식**이 임시로 깨진 상황이라면,
  - 자바 언어에서는 `lock` 상태에서 **재진입**(reentrant)를 허용하기 때문에
    - **교착 상태에 빠지진 않음**
  - **예외**를 발생시킨 첫 번째 예시에서라면
    - **외계인 메서드**를 호출하는 스레드는
    - 이미 **락**을 쥐고 있기 때문에, 다음번 락 획득도 성공함
      - 해당 락이 보호하는 데이터에 대해
        - 개념적으로 관련이 없는 다른 작업이 진행중이어도 가능함
    - 하지만 위 상황에서 치명적인 결함이 발생할 수 있음
      - **락**이 제 구실을 하지 못했기 때문
    - **재진입 가능 락**은 **OOP**를 쉽게 구현할 수 있도록 해주나,
      - 응답 불가(교착 상태)가 될 상황을 **안전 실패**(데이터 훼손)으로 변모시킬 수 있음

### 문제의 해결 방법
- **외계인 메서드 호출**을 **동기화 블록 밖**으로 이동시키면 됨
- `notifyElementAdded` 메서드에서라면
  - **관찰자 리스트**를 복사해서 사용하면
  - **락 없이도** 안전하게 순회 가능함
- 외계인 메서드를 동기화 블록 밖으로 옮긴 예시
  ```java
  private void notifyElementAdded(E element) {
    List<SetObserver<E>> snapshot = null;
    synchronized(observers) {
      snapshot = new ArrayList<>(observers);
    }
    for (SetObserver<E> observer : snapshot)
      observer.added(this, element);
  }
  ```
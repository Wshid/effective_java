## [ITEM.03] Singleton
- 인스턴스를 오직 하나만 생성할 수 있는 클래스

### singleton의 생성방법
- 생성자는 `private`
- 유일하게 인스턴스에 접근하는 메서드 지정
    - `public static`
#### 1. public static final 필드 방식
```
public class Elvis {
    public static final Elvis INSTANCE = new Elvis(); // public, 외부 접근 가능
    private Elvis(){...}

    public void leaveTheBuilding(){...}
}
```
- 리플렉션 API 사용
    - `AccessibleObject.setAccessible`로 private 생성자를 호출은 가능함
    - 생성자를 수정해서, 두번째 객체가 생성되려할때 예외를 던지면 됨
#### 2. 정적 팩터리 메서드 제공
```
public class Elvis {
    private static final Elvis INSTANCE = new Elvis(); // private 외부 접근 불가
    private Elvis(){...}
    public static Elvis getInstance(){
        return INSTANCE;
    }
}
```
- Elvis.getInstance는 결국 같은 객체만 참조
- 세가지 장점
    - API를 바꾸지 않고도 Singleton을 해제시킬 수 있음
        - 유일한 인스턴스 반환 메서드를 쓰레드별로 다른 인스턴스 리턴하게끔 변경 가능
    - 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수 있음
    - 정적 팩터리의 메서드 참조를 supplier로 사용할 수 있음
        - `Elvis::getInstance`를 `Supplier<Elvis>`로 사용할 수 있음
#### 두가지 방법에 대한 serialize
- 모든 인스턴스 필드를 일시적(transient)로 판단, `readResolve` 메서드 제공 해야함
- 안그러면 직렬화된 인스턴스를 역직렬화시, 새로운 인스턴스 생성
```
private Object readResolve(){
    return INSTANCE;
}
```

#### 3. 열거 타입
```
public enum Elvis{
    INSTANCE;

    public void leaveTheBuilding(){...}
}
``` 
- public과 유사하나, 간결함
- 직렬화도 편함
- 대부분의 상황에서 원소가 하나뿐인 열거타입이 **싱글턴을 만드는 가장 좋은 방법**
- 단, 싱글턴이 `Enum`외의 클래스 상속시, 사용 불가
    - 열거 타입이 다른 인터페이스를 구현하도록 설계는 가능함
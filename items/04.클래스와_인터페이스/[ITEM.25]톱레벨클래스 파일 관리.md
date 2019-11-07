## [ITEM.25] 톱레벨 클래스 파일 관리
- 소스 파일 하나에 톱레벨 여러개
    - compiler는 에러를 내진 않음
    - 하지만 위험한 행동
- 한 클래스를 여러가지로 정의 할 수 있으며,
    - 어느 소스를 컴파일하냐에 따라
    - **어느 클래스를 사용할지**가 정해지기 때문

### 예시
- Main class
    >```java
    >pubilc class Main {
    >    public static void main(String[] args){
    >        System.out.println(Utensil.NAME + Dessert.NAME);
    >    }
    >}
    >```
- `Utensil`과 `Dessert`가 `Utensil.java` 한 파일에 정의
    >```java
    >class Utensil {
    >    static final String NAME = "pan";
    >}
    >
    >class Dessert {
    >    static final String NAME = "cake";
    >}
    >```
- `Main`을 호출하면 `"pancake"`를 출력한다.
- `Dessert.java` 파일 생성,
    - `Utensil`과 `Dessert` 클래스를 담았다.
    >```java
    >class Utensil {
    >    static final String NAME = "pot";
    >}
    >
    >class Dessert {
    >    static final String NAME = "pie";
    >}
    >```
- 다른 파일에, 동일한 클래스가 동의된 상태
- `javac Main.java Dessert.java`와 같이 컴파일 한다면
    - 컴파일 오류가 날 것이며,
    - 중복 정의 내용이 리턴됨
- `Main.java`를 컴파일 한 후
    - `Utensil` 참조를 먼저 만나면,
    - `Utensil.java` 파일을 살핀 뒤
        - `Utensil`과 `Dessert`를 파악한다.
    - 두번째 명령 인자로 넘어온
        - `Dessert.java`를 처리할 때
        - 클래스가 이미 있다는 것을 파악하게 됨
- `javac Main.java`나 `javac Main.java Utensil.java` 명령을 컴파일 시
    - `pancake`를 출력함
- `javac Dessert.java Main.java`로 컴파일 시,
    - `potpie`를 출력
- **컴파일 순서에 따라 동작이 달라진다는 의미**

### 해결 방법
- 톱레벨 클래스를 다른 소스파일로 분리하기
- 톱레벨 클래스를 같은 파일에 담고 싶다면
    - `정적 멤버 클래스(ITEM.24)`를 활용
- 다른 클래스에 부차적인 클래스라면
    - `정적 멤버 클래스`로 만드는 편이 낫다
    - 가독성이 좋으며
    - `private`로 선언(ITEM.15)시,
        - 접근 범위도 최소로 관리할 수 있기 때문

### 해결한 예시
>```java
>public class Test {
>    public static void main(String[] args){
>        System.out.println(Utensil.NAME + Dessert.NAME);
>    }
>
>    private static class Utensil {
>        static final String NAME = "pan";
>    }
>
>    private static class Dessert {
>        static final String NAME = "cake";
>    }
>}
>```

### 결론
- 소스 파일 하나에는 반드시 톱레벨 클래스/인터페이스를 하나만 담아야 함
    - 컴파일러가 한 클래스에 대한 정의가 다중으로 발생하지 않음
    - 소스 코드의 컴파일 순서와 연관이 없어짐
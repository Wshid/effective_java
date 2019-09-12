## [ITEM.23] 클래스 계층 구조 활용
### 태그 달린 클래스
- 두 가지 이상의 의미를 표현,
    - 그 중 현재 표현하는 의미를 태그 값으로 알려주는 클래스

### 태그 달린 클래스 예시
>```
>class Figure {
>    enum Shape { RECTANGLE, CIRCLE };
>
>    // 태그 필드, 현재 모양을 나타냄
>    final Shape shape;
>
>    // 필드가 RECTANGLE 일때만 사용됨
>    double length;
>    double width;
>
>    // 필드가 CIRCLE 일때만 사용됨
>    double radius;
>
>    // CRICLE constructor
>    Figure(double radius){
>        shape = Shape.CIRCLE;
>        this.radius = radius;
>    }
>
>    // RECTANGLE constructor
>    Figure(double length, double widht){
>        shape = Shape.RECTANGLE;
>        this.length = length;
>        this.width = width;
>    }
>
>    double area(){
>        switch(shape){
>            case RECTANGLE:
>                return length * width;
>            case CIRCLE:
>                return Math.PI * (radius * radius);
>            default:
>                throw new AssertionError(shape);
>        }
>    }
>}
>```

### 태그 달린 클래스의 단점
- `enum 타입 선언`, `태그 필드`, `switch 구문` 등 불필요한 코드 존재
- 여러 구현이 한 클래스에 혼합, 가독성이 좋지 않음
- 메모리를 많이 먹는다.
    - 다른 의미를 위한 코드가 포함되기 때문
- 필드를 `final`로 선언,
    - 해당 의미에 쓰이지 않는 필드까지
        - **생성자에서 초기화 해야 함**
    - 쓰지 않는 필드를 초기화 하는, 불필요 코드가 늘어남
- **생성자**가 태그 필드를 설정하고,
    - 해당 의미에 쓰이는 데이터 필드 초기화
        - compile time에 확인 불가
    - runtime때 확인 가능
- 또 다른 의미 추가 시, **코드를 수정해야함**
    - 새로운 의미 추가시마다
        - 모든 `switch` 문을 찾아 새 의미를 처리하는 코드를 추가해야 함
    - 하나라도 빠지게 되면 `runtime`에 문제 발견
- **Instance Type**만으로는 현재를 나타내는 의미를 알 수 없음

### subtyping
- 태그 달린 클래스의 다른 수단
- 클래스 계층 구조를 활용한다.

### 태그 달린 클래스 -> 클래스 계층구조
- 계층 구조의 `root`가 될 **추상 클래스** 정의
- **태그 값**에 따라 동작이 달라지는 `method`들을
    - `root` 클래스의 `추상 메서드`로 선언
    - 예시의 `area()` 함수가 여기에 해당
- 태그 값에 상관없이, **동작이 일정한 메서드**들을
    - `root` 클래스의 `일반 메서드`로 추가한다
- 모든 하위 클래스에서 **공통으로** 사용되는 **데이터 필드**도
    - 모두 `root` 클래스로 옮긴다.
- `Figure` 클래스에서는,
    - 태그 값에 상관없는 메서드가 하나도 없음
    - 모든 하위 클래스에서 사용되는 **공통 데이터 필드**도 없음
    - `root`클래스에 `area()` 하나만 남게 될 것
- `root` 클래스를 확장한 **구체 클래스**를 의미별로 정의
    - `Circle` 및 `Rectangle` 클래스를 선언하면 됨
- 각 하위 클래스에는
    - 각자의 의미에 해당하는 **데이터 필드** 정의

### 클래스 계층 구조 예시
>```
>abstract class Figure {
>    abstract double area();
>}
>
>class Circle extends Figure {
>    final double radius;
>    
>    Circle(double radius) { this.radius = radius; }
>
>    @Override double area() { return Math.PI * (radius * radius); }
>}
>
>class Rectangle extends Figure {
>    final double length;
>    final double width;
>
>    Rectangle(double length, double width){ ... }
>
>    @Override double area() { return length * width; }
>}
>```
- 각 의미에 독립된 클래스에 담아,
    - 관련이 없던 데이터 필드 모두 제거
- 살아 남은 필드는 모두 `final`로 변환
- 각 클래스의 **생성자**가 **모든 필드**를 **초기화**
- 추상 메서드를 모두 구현했는지
    - **컴파일러**가 확인 가능함
- 실수로 빼먹은 `case`문에 대한 문제도 없음
- `root` 클래스의 코드를 건드리지 않고,
    - 독립적으로 계층구조 **확장**이 가능하다.
- 타입이 의미별로 존재하니,
    - 변수의 의미를 명시/제한이 가능함

### 추가 예시
>```
>class Square extends Rectangle {
>    Square(double side) { super(side, side); }
>}
>```
- `Rectangle`을 상속 받아 처리할 수 있음

### 결론
- 태그 달린 클래스를 사용할 일은 거의 없음
- 태그 필드가 등장할 경우
    - 태그 필드를 없애고
    - 계층 구조로 대체할 것
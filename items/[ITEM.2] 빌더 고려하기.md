## [ITEM.2] 빌더 고려하기
### 점층적 생성자 패턴
- 매개변수 개수가 많아지면 클라이언트 코드 작성/읽기 어려움

### 자바 빈즈 패턴
- setter를 이용하는 방식
- 객체 하나를 만들려면 메서드를 여러번 호출해야 함
- 객체가 완성되기 전까지는 일관성(consistency)가 무너진 상태
- 클래스를 불변 형태로 만들어줄 수 없음
- freeze 패턴
    - 객체가 완전히 생성이 되기 전까지는 freeze 메서드를 사용
    - 런타임 오류에 취약하기 때문에 실제 사용하지는 않음

### 빌더 패턴
- 파이썬과 스칼라의 `명명된 선택적 매개변수(named optional parameters)`를 흉내낸 것
- `점층적 생성자 패턴` + `자바 빈즈 패턴`, 둘의 장점을 취함
- 유효성 검사 코드를 포함해야 함(아래 예시엔 포함되지 않음)
    - 빌더의 생성자와 메서드에서 입력 매개변수 검사
    - build 메서드 호출 시, 생성자에서 여러 매개변수의 불변식(invariant)를 검사
    - 이후 잘못된 매개변수 확인시 해당 메세지를 포함하여 `IllegalArgumentException` 발생 시킨다.
- 불변식(invariant)
    - 프로그램이 실행되는 동안 혹은 정해진 기간동안 반드시 만족해야 하는 조건
    - 변경을 허용할 수는 있으나, 주어진 조건 내에서는 허용 해야함
        - 리스트의 크기는 반드시 0이상, 중간에 음수값이 되면 invariant 하지 않음
- 기본 예시
```
public class NutritionFacts{
    private final servingSize;
    private final int savings;

    // 빌더 클래스 사용
    public static class Builder{
        private final int servingSize; // 필수 매개변수
        private int savings = 0; // 선택 매개변수

        public Builder(int servingSize){
            this.servingSize = servingSize;
        }

        public Builder savings(int val){
            this.savings = val;
            return this;
        }

        public NutritionFacts build(){
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder){
        servingSize = builder.servingSize;
        servings = builder.servings;
    }
}
```
### Fluent API
- `method chaning`이라고도 함
    - NutritionFacts 클래스는 `immutable`함
    - 모든 매개변수의 기본값을 한자리에 모음
    - 빌더의 세터 메서드들은 빌더 자신을 반환, **연쇄적 호출** 가능
    ```
    NutritionFacts cocaCola = new NutritionFacts.Builder(200).savings(2).build();
    ```

### 추상 클래스?(Abstract Class)
- [refer](https://programmers.co.kr/learn/courses/5/lessons/188)
- `abstract` 키워드를 가짐
- 추상 메서드를 포함할 수 있음
    - 이 역시 `abstract`로 선언해주어야 함
- 추상 클래스를 상속 받으면
    - 추상 메서드를 무조건 구현해야함
    - 추상 메서드를 구현하지 않으면, 상속 받은 클래스 역시 `추상 클래스`가 된다.
- 변수 역시 선언 가능
- Example
    ```
    abstract class Shape {
        int x,y;
        public void move(int x, int y){
            this.x=x;
            this.y=y;
        }

        public abstract void draw();
    }

    class Rectangle extends Shape{
        int width, height;
        public void draw(){
            log.info('rectangle');
        }
    }
    ```

### 계층적 클래스 설계
- 빌더 패턴이 적용되면 좋은 케이스
- 각 계층 클래스별 빌더를 멤버로 정의
- 추상 클래스 -> 추상 빌더
- 구체(concrete) 클래스 -> 구체 빌더
- 소스 코드 참조

### 빌더 패턴의 단점
- 객체를 만들려면 빌더 부터 만들어야 함
- 빌더 자체가 생성 비용이 크진 않으나, 성능 상 민감하다면 문제가 발생
- 점층적 생성자 패턴보다는 코드가 많기 때문에, 매개변수가 **4개** 이상은 되어야 효과 발생

### 핵심 정리
- 생성자나 정적 팩터리나 처리해야할 매개변수가 많다면 **빌더 패턴** 고려하기
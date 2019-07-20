## [ITEM.05] 의존객체 주입
- 많은 클래스가 하나 이상의 자원에 의존
- 잘못 구현한 예
    ```
    public class SpellChecker {
        private static final Lexicon dictionary= ...;

        private SpellChecker(){}

        public static boolean isValid(String word){...}
        public static List<String> suggestions(String typo){...}
    }
    ```
    - 유연하지 않고 테스트가 어렵다?
    - 사전을 하나만 사용한다고 가정했기 때문
        - 사전이 언어별로 있거나, 특수 어휘용 사전, 테스트용 사전 등..
- SpellChecker가 여러 사전을 사용하도록 수정하기
    - final 한정자를 제거하고 다른 사전으로 교체하기? 
        - 오류 나기 쉬움
        - 멀티 쓰레드 환경을 지원하지 않음
- **사용하지 자원에 따라 동작이 달라지는 클래스**는
    - **정적 유틸리티 클래스**나 **싱글턴 방식**이 적합하지 않음

### 인스턴스 생성시, 생성자에 필요한 자원 넘기기
- 클래스가 여러 자원 인스턴스를 지원해야함
- C가 원하는 자원을 사용할 수 있도록 해야함
- 의존 객체 주입의 한 형태
    ```
    public class SpellChecker{
        private final Lexicon dictionary;

        public SpellChecker(Lexicon dictionary){
            // requireNonNull : 인자 null 체크, null이 아닐경우 그대로, 맞다면 NPE(NullPointException) 리턴
            this.dictionary = Objects.requireNonNull(dictionary);
        }
        public boolean ...
    }
    ```
    - 자원이 무엇이든 의존 관계가 어떻든 상관 없음
    - immutable 보장
        - 동일 자원을 사용하려는 여러 client가 안심하고 사용 가능함

### 의존 객체 주입의 또다른 변형
- 생성자에 자원 팩터리를 넘겨주는 방법
- 호출할 때마다 특정 타입의 인스턴스를 반복하여 만들어주는 객체
    - **팩터리 메서드 패턴(Factory MEthod pattern)**의 구현
- java 8에서의 `Supplier<T>` 인터페이스
- Supplier<T>
    - 이를 입력으로 받는 메서드는 일반적으로 **한정적 와일드 카드 타입(bounded wildcard type)**을 사용
    - 명시한 타입의 하위 타입이라면 무엇이든 생성가능한 팩터리를 넘길 수 있음
- `Mosaic create(Supplier<? extends Tile> tileFactory){...}`
    - C가 제공한 팩터리가 생성한 Tile들로 구성된 모자이크를 만드는 메서드

### 의존 객체 주입의 장/단점
- 유연성, 테스트 용이성의 개선
- 의존성이 많은 프로젝트에서는 오히려 어려움
    - 프레임 워크로 해결 가능
        - Dagger, Guice, Spring
    

### 정리
- 클래스가 내부적으로 **하나 이상의 자원에 의존하고**,
    - 그 자원이 클래스 동작에 영향을 줄때는
        - **싱글턴** 및 **정적 유틸리티 클래스** 사용 X
- 대신 필요한 자원/팩터리(자원을 만들어주는)를 생성자에 넘기기
## Java Enum 정리
- https://programmingfbf7290.tistory.com/entry/enum-%EC%9E%90%EB%A3%8C%ED%98%95
- 하나의 객체를 public static final 필드 형태로 제공
- enum은 자료형의 개체수를 엄격히 통제
- 싱글턴 패턴을 일반화 한 것
    - singleton pattern : 열거 상수가 하나뿐인 enum
    
### 함수형태의 값을 담기
```
package enumPack;

public enum Operation {
    PLUS("+") {
        double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS("-") {
        double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES("*") {
        double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE("/") {
        double apply(double x, double y) {
            return x / y;
        }
    };

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }
    @Override public String toString(){return symbol;}

    abstract double apply(double x, double y);
}
```
- abstract로 apply 함수를 적용시키면서,
- 모든 enum 값들이, 그에 해당하는 apply 함수를 내부적으로 구현해주어야 한다.


### Ordinal 대신, 객체 필드 사용하기
- `ordinal()`은 원래 enum 기반 자료구조에서 사용할 목적
```
package enumPack;

public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3);

    private final int numberOfMusicions;

    Ensemble(int value){
        this.numberOfMusicions = value;
    }
    public int getNumberOfMusicions(){
        return numberOfMusicions;
    }
}
```

### 비트 필드 대신 EnumSet 사용하기
- 비트 필드는 일반적으로 list와 set보다 빠르다고 함
```
package enumPack;

import java.util.Set;

public class TextEnum {
    public int style;
    public enum Style{
        BOLD, ITALIC, UNDERLINE, STRIKETHROUGH;
    }

    public void applyStyles(Set<Style> styles){
        System.out.println("print styles");
        for(Style style : styles){
            System.out.println(style);
        }
    }
}
```
```
TextEnum textEnum = new TextEnum();
textEnum.applyStyles(EnumSet.of(TextEnum.Style.BOLD, TextEnum.Style.ITALIC));
```
- `EnumSet.of`를 사용하여 열거형태를 받아온다.

### EnumMap
- [refer](https://seungdols.tistory.com/464)
- `Enum`을 Instance로 가지며, `Key, Value`를 저장하는 `Map`형태

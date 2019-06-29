package enumPack;

import javafx.util.Builder;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public abstract class Pizza {
    public enum Topping{
        HAM, MUSHROOM, ONION, PEPPER, SAUSAGE
    }
    final Set<Topping> toppings;

    // 재귀적 타입 한정을 사용
    abstract static class Builder<T extends Builder<T>>{
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class); // empty class를 만든다.
        public T addTopping(Topping topping){
            toppings.add(Objects.requireNonNull(topping)); // nonNull일 때 토핑을 추가한다.
            return self();
        }

        abstract Pizza build();

        // 하위 클래스는 이 메서드를 overriding, this를 반환하도록 해야함
        // 하위 클래스에서 형 변환 없이 메서드 연쇄 가능 => 셀프타입 관용구
        protected abstract T self();

    }

    Pizza(Builder<?> builder){
        // 방어적 코딩 방법
        toppings = builder.toppings.clone();
    }
}

/**
 * 공변 반환 타이핑(covariant return typing)
 *  NyPizza.Builder -> NyPizza 반환
 *  Calzone.Builder -> Calzone 반환
 *  하위 클래스의 메서드가 상위 클래스 메서드가 정의한 타입이 아닌, 그 하위 타입을 반환하는 경우
 */
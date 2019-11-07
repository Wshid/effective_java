## [ITEM.33] 타입 안전 이종 컨테이너
- 제네릭은
    - `<Set<E>`, `Map<K,V>`등의 **Collection**과
    - `ThreadLocal<T>`, `AtomicReference<T>`등의 **단일원소 컨테이너**에도 쓰임
- 이런 모든 쓰임에서
    - **매개변수화**되는 대상은 **컨테이너**이다
    - 원소가 아님
- 하나의 **컨테이너**에서 매개변수화 할 수 있는 **타입의 수**가 제한
- 컨테이너는 일반적인 용도에 맞게 설계되었으므로, 문제는 없음
- `Set`
    - 원소의 타입을 뜻하는 **하나의 매개변수**
- `Map`
    - `Key, Value`에 해당하는 2개가 필요

### 유연한 수단, 타입 안전 이종 컨테이너 패턴(Type safe heterogeneous container pattern)
- db의 **row**는 여러개의 **column**을 가질 수 있음
- 모든 **column**을 안전하게 활용할 수 있는 방법?
- 컨테이너 대신, **키**를 **매개변수화**
    - 컨테이너에서 값을 넣거나 뺄때,
    - **매개변수화**한 **키**를 같이 제공
- 제네릭 타입 시스템이, **값의 타입 == 키**를 보장함

### 예시
- 코드
    ```java
    # Favorites API
    public class Favorites {
        public<T> void putFavorite(Class<T> type, T instance);
        public<T> T getFavorite(Class<T> type);
    }
    ```
    ```java
    public static void main(String[] args) {
        Favorites f = new Favorites();

        f.putFavorite(String.class, "Java");
        f.putFavorite(Integer.class, 0xcafebabe)
        f.putFavorite(Class.class, Favorites.class);

        String favoriteString = f.getFavorite(String.class);
        int favoriteInteger = f.getFavorite(Integer.class);
        Class<?> favoriteClass = f.getFavorite(Class.class);

        // Java cafebebe Favorites
        System.out.println("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());
        // %n : 플랫폼에 맞는 줄바꿈 문자로 자동 대체(대부분 \n)
    }
    ```
- `Favorites` 클래스
- 각 타입 별로 즐겨찾는 인스턴스를 저장하고 검색할 수 있음
- 각 타입의 `Class` 객체를 **매개변수화한 키**역할로
    - `class`의 클래스가 제네릭이기 때문에 가능
    - `class`의 리터럴 타입 : `Class<T>`(`Class`가 아님)
        - `String.class`의 타입 : `Class<String>`
        - `Integer.class`의 타입 : `Class<Integer>`
- `compile`의 타입 정보와 `runtime`의 타입 정보를 얻기 위해
    - **메서드**들이 주고 받는 `class` 리터럴을 **타입 토큰**이라 한다.
- `Favorites` 인스턴스는 **타입 안전** 하다
- `String`을 요청했는데, `Integer`를 반환하지 않음
- 모든 키의 **타입**이 다르기 때문에
    - 일반적인 맵과 달리, **여러가지 타입의 원소**를 담을 수 있음
- 타입 이종(hegerogeneous) 컨테이너의 특징
- `Favorites` : 타입 안전 이종 컨테이너
    ```java
    public class Favorites {
        private Map<Class<?>, Object> favorites = new HashMap();

        public <T> void putFavorite(Class<T> type, T instance) {
            favorites.put(Object.requireNonNull(type), instance);
        }

        public <T> T getFavorite(Class<T> type) {
            return type.cast(favorites.get(type));
        }
    }
    ```
    - `Map<Class<?>, Object>`
        - 비한정적 와일드카드 타입, 아무것도 넣을 수 없다?
        - 와일드카드 타입이 **중첩(nested)**되어 있음
            - 맵이 아니라, **키가 와일드카드 타입**이다.
        - 모든 키가 서로 다른 매개변수화 타입일 수 있다는 의미
            - `Class<String>`, `Class<Integer>`와 같이 가능하다.
        - 다양한 타입을 지원할 수 있는 특징
    - `favorites`의 값 타입 : `Object`
        - 키와 값 사이의, 타입관계를 보증하지 않음
        - 모든 값이 **키**로 명시한 타입이 아니라는 의미
  - `putFavorite`
    - 주어진 `Class` 객체와 즐겨찾기 인스턴스를 `favoritess`에 추가해 관계를 짓는다.
    - 키와 값사이의 `type linkage` 정보는 버려짐
    - 해당 키 타입의 인스턴스라는 정보가 사라진다.
      - `getFavorite` 메서드에서 다시 복구 가능
  - `getFavorite`
    - 주어진 `Class` 객체에 해당하는 값을 `favorites` 맵에서 꺼낸다.
    - 이 객체가 바로 반환할 객체는 맞으나
      - 잘못된 `compileTime` type을 가지고 있음
    - 꺼낸 객체의 타입은 `Object` 타입
      - favorites 맵의 값 타입
    - 이 객체를 `T`로 바꾸어 반환해야 한다.
    - `Class`의 `cast` 메서드를 통해서
      - 객체 참조를 `Class` 객체가 가리키는 타입으로 **동적 형변환**

### cast 메서드
- **형변환 연산자**의 동적 버전
- 단순히 주어진 인수가 `Class` 객체가 알려주는 타입의 인스턴스인지 검사
  - 맞다면, 인수를 그대로 반환
  - 아니라면, `ClassCastException`
- C의 코드가 깔끔하게 컴파일 된다면,
  - `getFavorite`가 호출하는 `cast`는 `ClassCastException`이 나오지 않을 것
  - `favorites`의 맵 안의 값 = 해당 키의 타입과 일치

### cast 메서드를 사용하는 이유
- 단순히 인수를 그대로 반환하는 함수
  - 사용하는 이유?
- `cast` 메서드의 시그니쳐가
  - **Class** 클래스의 **제네릭**이기 때문
- `cast`의 반환 타입 = `Class`객체의 타입 매개변수
  ```java
  public class Class<T> {
      T cast(Object obj);
  }
  ```
  - 정확히 `getFavorites` 메서드에 필요한 기능
  - `T`로 **비검사 형변환** 손실 없이, `Favorites`를 타입 안전하게 만든다
  
### Favorite 클래스의 두가지 제약
#### 악의적인 C가, `Class` 객체를 제네릭이 아닌 `raw`타입으로 넘기면, Favorites 인스턴스의 **타입 안전성**이 깨진다.
- 그럴 경우 C에서는 비검사 경고가 뜬다.
- `HastSet`과 `HashMap`등의 **일반 컬렉션 구현체**에도 동일한 문제 존재
  - `HashSet`의 `raw`타입 사용시
    - `HashSet<Integer>`에 `String`을 넣는건 쉬움
    - Runtime Type 안전성 확보 가능
- `Favorites`가 **타입 불변식**을 어기는 일이 없도록 보장하려면,
  - `putFavorite` 메서드에서 인수로 주어진 
    - `instance`의 타입 ? `type`으로 명시한 타입과 동일여부 확인
  - 동적 형변환을 사용하면 됨
    ```java
    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), type.cast(instance));
    }
    ```
- `java.util.Collections`에는
  - `checkedSet`, `checkedList`, `checkedMap`과 같은 메서드 존재
  - 이 방식을 적용한 컬렉션 래퍼
- 이 **정적 팩터리**들은
  - 컬렉션(| Map)과 함께 1개(| 2개)의 `Class` 객체를 받는다
  - 메서드가 모두 **제네릭**이므로, `Class` 객체 == Collection의 `CompileTime` Type`이 같음
- 또한, 래퍼들은 **내부 컬렉션**들을 **실체화**한다.
- `Runtime`에 `Coin`을 `Collection<Stamp>`에 넣는 상황,
  - `ClassCastException`이 발생한다.
- 이 래퍼들은
  - **제네릭**과 **raw** 타입을 섞어 사용하는 App에서
  - C가 Collection에 잘못된 타입의 원소를 넣지 못하게 추적함
#### 실체화 불가 타입(ITEM.28)에는 사용 불가
- `String`이나 `String[]`는 저장할 수 있어도,
  - `List<String>`은 저장불가
    - 애초에 컴파일 되지 않음
  - `List<String>`의 `Class` 객체를 찾을 수 없기 때문
    - `List<String>.class`라고 쓰면 문법 오류
  - `List.class`가 없는 이유
    - `List<String>`과 `List<Integer>`의 경우, 동일한 `List.class`를 사용
      - 같은 `Class` 객체를 공유
    - `List<String>.class`와 `List<Integer>.class`를 허용한다면,
      - `Favorites` 객체가 망가진다
- 이 문제에 대한 우회는 존재하지 않음

### super type token으로 해결한 두번째 문제
- 단, `super type token`으로 해결하려는 시도가 있음
- 실제로 유용
- SpringFramework에서 `ParameterizedTypeReference` 클래스로 제공
- 슈퍼 타입 토큰 적용시, generic도 무리 없이 적용 가능
  ```java
  Favorites f = new Favorites();

  List<String> pets = Arrays.asList("개", "고양이", "앵무");

  f.putFavorite(new TypeRef<List<String>>(){}, pets);
  List<String> listofStrings = f.getFavorite(new TypeRef<List<String>>(){});
  ```
- 완벽하진 않으므로, 주의하여 사용해야 함
  - 닐 개프터(Neal Gafter)의 글을 꼭 확인할 것

### Favorites, 비한정적
- `Favorites`가 사용하는 타입 토큰 = 비한정적
- `getFavorites`와 `putFavorites`는 어떤 `Class` 객체든 받아들인다.
- 허용하는 타입 제한을 하고 싶으면, **한정적 타입 토큰**을 활용하면 됨
  - 한정적 타입 매개변수(ITEM.29)
  - 한정적 와일드카드(ITEM.31) 등을 사용하여
  - **표현 가능한 타입을 제한**하는 토큰을 의미

### Annotation API(ITEM.39)의 한정적 타입토큰 사용
- `AnnotatedElement` 인터페이스에 선언된 메서드
- 대상 요소에 달려있는 `@`를 `Runtime`에 읽어오는 기능
- 리플렉션의 대상이 되는 타입들에서 활용
  - 클래스(`java.lang.Class<T>`)
  - 메서드(`java.lang.reflect.Method`)
  - 필드(`java.lang.reflect.Field`)
    - 프로그램 요소를 표현하는 타입들에서 구현함
- `@` 코드
  ```java
  public <T extends Annotation>
    T getAnnotation(Class<T> annotationType);
  ```
- `annotationType` 인수는 `@`타입을 뜻하는 **한정적 타입 토큰**
  - `annotationType 인수` = `annotation Class`로 되어 있음
    - **어노테이션 타입에 해당하는 클래스 객체**라는 의미
- 토큰으로 명시한 타입의 `@`가
  - 대상 요소에 달려 있다면, 해당 Annotation을 반환
  - 없다면 `null` 반환
- `@`된 요소는
  - **키**가 `@`타입인, **타입 안전 이종 컨테이너**

### Class<?> 타입, 한정적 타입 토큰 메서드에 넘기려면
- 객체를 `Class<? extends Annotation>`으로 형변환 할 수도 있으나,
  - 해당 형변환은 **비검사**
  - 컴파일 경고가 뜬다(ITEM.27)
- `Class` 클래스가, 이런 형변환을
  - 안전하게, 동적으로 수행해주는 **인스턴스 메서드 제공**
    - `asSubclass` Method
- `asSubclass` MEthod
  - 호출된 인스턴스 자신의 `Class` 객체를
    - **인수가 명시한 클래스**로 **형변환**
  - 형변환이 된다는 것은
    - 이 클래스가, **인수로 명시한 클래스**의 **하위 클래스**를 의미
  - 형변환에
    - 성공, 인수로 받은 클래스 객체 반환
    - 실패, `ClassCastException` 발생

### asSubClass 컴파일 시점에 모르는 타입을, Runtime에 읽기
- 코드
  ```java
  static Annotation getAnnotation(AnnotationElement element, String annotationTypeName) {
      Class<?> annotationType = null; // 비한정적 타입 토큰
      try {
          annotationType = Class.forName(annotationTypeName);
      } catch (Exception ex) {
          throw new IllegalArgumentException(ex);
      }
      return element.getAnnotation(
          annotationType.asSubclass(Annotation.class)
      )
  }
  ```

### 결론
- `Collection API`로 대표되는
  - 일반적인 제네릭 형태에서는
  - 한 컨테이너가 다룰 수 있는 타입 매개변수가 고정되어 있음
- 컨테이너 자체가 아닌 **키**를 **타입 매개변수**로 변경하면,
  - **제약 타입이 없는, 타입 안전 이종 컨테이너 제작 가능**
- **타입 안전 이종 컨테이너**
  - 키 : `Class`
    - 이런식으로 사용되는 `Class` 객체 : **타입 토큰**
  - 직접 구현한 키 타입도 사용 가능
    - db의 경우 row(컨테이너)를 표현한
      - `DatabaseRow` 타입에는
      - 제네릭 타입인, `Column<T>`를 키로 사용 가능
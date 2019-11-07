## [ITEM.09] try-with-resources 사용
- `close`로 닫아줘야 하는 자원이 많음
    - 안전망으로 `finalizer`를 사용하지만, 믿음직하지 못함

### try-finally
- 전통적으로 사용하는 방법
- 예와 발생 및 반환 경우를 포함
- 회수 자원이 하나 일때
>```
>BufferedReader br = new BufferedReader(new FileReader(path));
>try{
>    return br.readLine();
>} finally{
>    br.close();
>}
>```
- 회수해야 할 자원이 둘 이상일 때
>```
>InputStream in = new FileInputStream(src);
>try{
>    OutputStream out = new FileOutputStream(dst);
>    try {
>        byte[] buf = new byte[BUFFER_ISZE];
>        int n;
>        while((n = in.read(buf))>=0)
>            out.write(buf, 0, n);
>    } finally {
>        out.close();
>    }
>} finally {
>    in.close();
>}
>```
- 구문이 상당히 복잡해짐

### 예시코드의 결점
- try 및 finally 블록에서 발생
- 기기의 물리적인 장애 발생
    - `readLine` 메서드가 예외 발생
        - `close()` 메서드 역시 실패
    - 두번째 예외가, 첫번째 예외를 삼켜버림
        - `stacktrace`에 첫번째 예외에 관련된 내용이 노출되지 않음
            - 디버깅이 어려워짐
- 두번째 예외에서 첫번째 예외를 출력할 수는 있으나
    - 코드가 복잡해지기 때문에 많이 사용하지 않음

### try-with-resources
- `java 7`부터 도입
- 해당 자원이 `AutoCloseable` 인터페이스를 구현하면 됨
    - void를 반환하는 `close()`메서드 하나
- 가독성이 높아지며, 문제 진단에 유리
#### 예시1
>```
>try (BufferedREader br = new BufferedReader(
>    new FileReader(path))){
>        return br.readLine();
>}
>```
- `readLine`과 `close()` 호출 양쪽에서 예외 발생 시,
    - `close()`에 대한 예외는 숨겨지고, `readLine()`에서 발생한 예외만 기록됨
- 다른 예외들이 숨겨지는 상황
    - `stacktrace`에 숨겨졌다가, `suppressed`라는 내용과 같이 출력 됨
    - `java 7`에서 `Throwable`에 추가된 `getSuppressed` 메서드를 이용하면
        - 프로그램 코드에서 가져올 수 있음
#### 예시2
>```
>try (InputStream in = new FileInputStream(src);
>    OutputStream out = new FileOutputStream(dst)) {
>        byte[] buf = new byte[BUFFER_SIZE];
>        int n;
>        while((n = in.read(buf)) >= 0)
>            out.write(buf, 0, n);
>    }
>```

### try-with-resources의 catch 사용
- try를 중첩하지 않고, 다수의 예외 처리 가능
- 예시
```
try ( BufferedReader br = new BufferedReader(
    new FileReader(path))) {
    return br.readLine();
} catch (IOException e){
    return defaultVal;
}
```

### 결론
- 꼭 회수하는 자원 다룰 때는
    - `try-finally`가 아닌, `try-with-resources` 사용할 것
--------------------
# Spring JPA 공부 노트
--------------------


# 목차


- [1. 관계형 데이터베이스와 자바](#관계형-데이터베이스와-자바)
- [2. DB관리 Dokcer 생성](#DB관리-Dokcer-생성)
- [3. DB 연결](#DB-연결)
    - [3-1. SQL 사용](#SQL-사용)

# 관계형 데이터베이스와 자바

관계형 데이터베이스는 자바와 독립적입니다. 
JDBC 를 사용하여 데이터베이스에 접속을 해서 데이터를 저장하거나 가져옵니다.
그런일을 하는 이유는 우리 에플리케이션의 데이터를 영속화(Perpetuation) 해야하는 이유가 있기 때문입니다.

# DB관리 Dokcer 생성

- docker 생성 명령어

~~~
docker run 
-p 5432:5432 
-e POSTGRES_PASSWORD=pass 
-e POSTGRES_USER=jjunpro 
-e POSTGRES_DB=springjpa
--name postgres_boot 
-d postgres
~~~

- docker 접속 명령어

~~~
docker exec -i -t postgres_boot bash

su - postgres

psql springjpa
~~~

다만 windows 일경우 다릅니다.

> postgres@766d6461a448:~$ psql --username jjunpro --dbname springjpa

- 데이터베이스 조회

> \list

- 테이블 조회

> \dt

- 쿼리 

> SELECT * FROM table;

# DB 연결

> build.gradle

~~~
compile group: 'org.postgresql', name: 'postgresql', version: '42.2.5'
~~~

postgresql 을 사용할 것이기 때문에 의존성 postgresql을 추가합니다.

> src/main/java/me.witeship/Application.java

~~~
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {

    public static void main(String[] args) throws SQLException {

        String url = "jdbc:postgresql://localhost:5432/springjpa";
        String username = "jjunpro";
        String password = "pass";

        try(Connection connection = DriverManager.getConnection(url, username, password))  {
            System.out.println("connection : " + connection);
        }

    }
}

- 실행결과
> connection : org.postgresql.jdbc.PgConnection@7a92922
~~~

접속 결과가 정상적으로 확인 되었기 때문에 위 정보들로 DB에 접근할 수 있다는것을 알 수 있었습니다.

## SQL 사용

- DDL
    - 스키마를 만드는 역할 테이블, 인덱스...등등

- DML
    - 데이터를 조작하는 역할 추가하거나 업데이트 혹은 삭제하는 기능

간단하게 데이블 스키마를 만들어 보겠습니다.

~~~
String sql = "CREATE TABLE ACCOUNT (id int, username varchar(255), password varchar(255));";
~~~

위 sql 을 가지고 PreparedStatement 객체를 생성할 것입니다.

- 여기서 PreparedStatement 란?
    - statement를 상속받는 인터페이스로 `SQL구문을 실행시키는 기능`을 갖는 객체

    - PreCompiled된 SQL문을 표현 즉, statement객체는 실행시 sql명령어를 지정하여 여러 sql구문을 하나의 statement객체로 수행이 가능하다.(재사용 가능)  하지만, preparedStatement는 객체 생성시에 지정된 sql명령어만을 실행할수 있다.  (다른 sql구문은 실행못함 ->재사용 못함)

    - `동일한 sql구문을 반복 실행한다면 preparedStatement가 성능면에서 빠름.`

    - SQL문에서 변수가 들어갈 자리는 ' ? ' 로 표시한다. , 실행시에 ?에 대응되는 값을 지정할때 setString(int parameterIndex, String X)이나 setInt(int parameterIndex, int x)와 같이  setXXX메소드를 통해 설정한다. 그리고  PreparedStatement 는 SQL문에서 Like키워드를 사용할경우 사용할수없다.

[PreparedStatement 객체 란?](http://blog.naver.com/PostView.nhn?blogId=javaking75&logNo=140162466611)

~~~
String sql = "CREATE TABLE ACCOUNT (id int, username varchar(255), password varchar(255));";

try(PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.execute();
}
~~~

프로그램을 실행 시켜 결과를 확인해 봅니다.

![유저-생성](./images/20190912_191815.png)

ACCOUNT TABLE 정상적으로 생성 되었습니다.

다음 테이블에 데이터를 넣어보겠습니다.

~~~
String sql = "INSERT INTO ACCOUNT VALUES(1, 'jjunpro', 'pwd');";

try(PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.execute();
}
~~~

![유저-생성](./images/20190912_194136.png)

데이터 또한 정상적으로 들어갔습니다.

위와 같은 코딩작업으로 일어나는 단점이 있습니다.

- ACCOUNT 라는 Domain 이라는 클레스를 맵핑을 해줘야 하는 테이블 생성하는 것이 번거롭고 또 테이블에서 가져온 데이터를 우리가 가지고 있는 Domain 객체로 맵핑하는 과정 자체도 번거롭습니다.

- Connection connection 을 만드는 비용이 많이 크고 오래걸립니다. 객체 자체가 마음대로 생성할 수 없습니다.

- SQL 표준이 다 달라서 사용한는 SQL 이 변경될 경우 쿼리 실행에 문제가 발생합니다.

# ORM Object-Relation Mapping

> JDBC 사용

~~~
try(Connection connection = DriverManager.getConnection(url, username, password))  {
    String sql = "INSERT INTO ACCOUNT VALUES(1, 'jjunpro', 'pwd');";
    try(PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.execute();
    }
}
~~~

> 도메인 모델 사용

~~~
Account account = new Account("jjunpro", "pwd");
accountRepository.save(account);
~~~

- JDBC 대신 도메인 모델을 사용하려는 이유
    - 객체 지향 프로그래밍의 장점을 활용하기 좋음
    - 각종 디자인 패턴
    - 코드 재사용
    - 비즈니스 로직 구현 및 테스트 편리

ORM은 어플리케이션의 클래스와 SQL 데이터베이스의 테이블 사이의 `맵핑 정보를 기술한 메타데이터`를 사용하여, 자바 애플리케이션의 객체를 SQL 데이터베이스의 테이블에 `자동으로 (또 깨끗하게) 영속화 해주는 기술`입니다.

# ORM : 패러다임 불일치

객체를 릴레이션에 맴핑하려니 발생하는 문제들과 해결책

밀도(Granularity) 문제

|---|---|
|객체|릴레이션|
|다양한 크기의 객체를 만들 수 있음|테이블|
|커스텀한 타입 만들기 쉬움|기본 데이터 타입 (UDT는 비추)|

# 링크

https://subicura.com/2017/01/19/docker-guide-for-beginners-1.html - [doker란?]
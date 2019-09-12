--------------------
# Spring JPA 공부 노트
--------------------


# 목차


- [1. 관계형 데이터베이스와 자바](#관계형-데이터베이스와-자바)
- [2. DB관리 Dokcer 생성](#DB관리-Dokcer-생성)
- [3. DB 연결](#DB-연결)
    - [3-1. SQL 사용](#SQL-사용)
- [4. ORM Object-Relation Mapping](#ORM-Object-Relation-Mapping)
- [5. ORM : 패러다임 불일치](#ORM-:-패러다임-불일치)
- [6. JPA 프로그래밍 프로젝트 셋팅](#JPA-프로그래밍-프로젝트-셋팅)
    - [6-1. 자동 설정 HibemateJpaAutoConfiguration](#자동-설정-HibemateJpaAutoConfiguration)
- [7. Domain 생성](#Domain-생성)
    - [7-1. JPA 데이터 영속화](#JPA-데이터-영속화)
    - [7-2. hibernate 영속화](#hibernate-영속화)


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

- docker 접속 확인

> docker ps

- docker 컨테이너 실행

> docker start

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

릴레이션 (relation) 

같은 성격의 데이터들의 집합을 의미. 흔히 테이블이라고 말하는 용어와 같은 의미로 이론적인 용어. 
릴레이션은 튜플과 에트리뷰트로 데이터를 정렬하여 관리한다. 


- 밀도(Granularity) 문제
    - 객체
        - 다양한 크기의 객체를 만들 수 있음.
        - 커스텀한 타입 만들기 쉬움.
    - 릴레이션
        - 테이블
        - 기본 데이터 타입 (UDT는 비추)

- 서브타입(Subtype) 문제
    - 객체
        - 상속 구조 만들기 쉬움.
        - 다형성
    - 릴레이션
        - 테이블 상속이라는게 없음.
        - 상속 기능을 구현했다 하더라도 표쥰 기술이 아님.
        - 다형적인 관계를 표현할 방법이 없음.

- 식별성(identity) 문제
    - 객체
        - 레퍼런스 동일성 (==)
        - 인스턴스 동일성 (equais() 메소드)
    - 릴레이션
        - 주키 (primary key)

- 관계(Association) 문제
    - 객체
        - 객체 레퍼런스로 관계 표현
        - 근본적으로 "방향"이 존재한다.
        - 다대다 관계를 가질 수 있음
    - 릴레이션
        - 외래키(foreign key)로 관계 표현
        - "방향"이라는 의미가 없음. 그냥 Join 으로 아무거나 묶을 수 있음
        -   태생적으로 다대다 관계를 못만들고, 조인 테이블 또는 링크 테이블을 사용해서 두개의 1대다 관계로 풀어야 함.

- 데이터 네비게이션(Navigation)의 문제
    - 객체
        - 레퍼런스를 이용해서 다른 객체로 이동 가능.
        - 콜렉션을 순회할 수도 있음.
    - 릴레이션
        - 하지만 그런 방식은 릴레이션에서 데이터를 조회하는데 있어서 매우 비효율적이다.
        - 데이터베이스에 요청을 적게 할 수록 성능이 좋다. 따라서 Join을 쓴다.
        - 하지만 너무 많이 한번에 가져오려고 해도 문제다.
        - 그렇다고 lazy loading을 하자니 그것도 문제

# JPA 프로그래밍 프로젝트 셋팅

- 데이터베이스 실행
    - PostgreSQL 도커 컨테이너 재사용
    - docker start postgres_boot

- 스프링 부트
    - 스프링 부트 v2
    - 스프링 프레임워크 v5

- 스프링 부트 스타터 JPA
    - JPA 프로그래밍에 필요한 의존성 추가
        - JPA v2
        - Hibemate v5
    - 자동 설정 : HibemateJpaAutoConfiguration
        - 컨테이너가 관리하는 EntityManager (프록시) 빈 설정
        - PlatformTransactionManager 빈 설정

- JDBC 설정
    - jdbc:postgresql://localhost:5432/springjpa
    - jjunpro
    - pass

https://start.spring.io/ 링크에서 spring boot jpa 의존성을 추가하여 프로젝트를 생성 후 import 합니다.

![유저-생성](./images/20190912_235447.png)

gradle에 설치된 의존성을 확인하면 jpa, hibernate 가 있는것을 확인할 수 있습니다.

엔티티매니저 가 JPA 스펙의 일부이고 엔티티매니저 내부적으로 hibernate를 사용합니다. 그러므로 둘다 사용 가능합니다. JPA 기반으로 코딩할 수 있고 hibernate 기반으로 도 코딩이 가능합니다. 하지만 둘다 사용하는 일은 거의 없습니다.

## 자동 설정 HibemateJpaAutoConfiguration

> application.properties

~~~
spring.datasource.url=jdbc:postgresql://localhost:5432/springjpa
spring.datasource.username=jjunpro
spring.datasource.password=pass

spring.jpa.hibernate.ddl-auto=create
~~~

application.properties 에 우리가 사용하는 DB에 접근할수 있는 정보를 줘야합니다.

spring.jpa.hibernate.ddl-auto 는 create 를 줘서 개발환경에 맞춰서 실행시 스키마를 새로 만들어주도록 명령합니다.

# Domain 생성

> Account.java

~~~
@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    ...getter, setter
}

~~~

어노테이션 @Entity 를 선언하여 Account 라는 Domain Class을 생성합니다.

`@Entity` 해당 클래스가 DB에 존재하는 Account 테이블에 맵핑이되는 Entity라고 알려주는 어노테이션 입니다.

`@Id` 는 DB의 주 키의 맵핑이 되는 어노테이션

`@GeneratedValue` 해당 값이 자동으로 생성되는 값이라고 알려주는 것

`@Column` 해당 테이블의 컬럼에 맵핑을 알려주는 어노테이션

## JPA 데이터 영속화

> JpaRunner

~~~
@Component
@Transactional
public class JpaRunner implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account account = new Account();
        account.setUsername("new user");
        account.setPassword("new pwd");

        entityManager.persist(account);
    }
}
~~~

EntityManager JPA 가장 핵심적인 클래스 타입의 Bean을 주입받을 수 있습니다. 그러므로 해당 클래스가 JPA 의 핵심입니다.

entityManager 클래스를 가지고 Entity 들을 `영속화` 할 수 있습니다. 즉 `데이터를 저장 한다는 의미입니다.`

위에서는 persist() 메소드를 통해서 `account Entity 를 영속화`합니다.

그리고 `EntityManager 과 관련된 모든 오퍼레이션 들은 한 Transactional 안에서 일어나야 합니다.` 그러므로 @Transactional 어노테이션을 상단에 추가합니다. 사용하는 해당 메소드 위에 바로 작성해도 됩니다.

실행 결과를 확인해보면 정상적으로 데이터 값이 저장된 것을 확인 하였습니다.

## hibernate 영속화

JPA는 hibernate 를 사용합니다. 그러므로 hibernate API도 사용할 수 있습니다.

hibernate 의 가장 핵심적은 API는 `Session` 입니다.

~~~
@Component
@Transactional
public class JpaRunner implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account account = new Account();
        account.setUsername("new user");
        account.setPassword("hibernate");

        Session session = entityManager.unwrap(Session.class);
        session.save(account);
    }
}
~~~



# 링크

https://subicura.com/2017/01/19/docker-guide-for-beginners-1.html - [doker란?]
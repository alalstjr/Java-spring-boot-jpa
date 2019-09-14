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
- [8. JPA 엔티티 맵핑](#JPA-엔티티-맵핑)
    - [8-1. 어노테이션 정보](#어노테이션-정보)
- [9. JPA Value 타입 맵핑](#JPA-Value-타입-맵핑)
- [10. JPA 1대다 맵핑](#JPA-1대다-맵핑)
    - [10-1. 단방향 @ManyToOne Study 가 주인](#단방향-@ManyToOne-Study-가-주인)
    - [10-2. 단방향 @OneToMany Account 가 주인](#단방향-@OneToMany-Account-가-주인)
    - [10-3. 양방향 관계](#양방향-관계)


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

![jpa 확인](./images/20190912_235447.png)

gradle에 설치된 의존성을 확인하면 jpa, hibernate 가 있는것을 확인할 수 있습니다.

엔티티매니저 가 JPA 스펙의 일부이고 엔티티매니저 내부적으로 hibernate를 사용합니다. 그러므로 둘다 사용 가능합니다. JPA 기반으로 코딩할 수 있고 hibernate 기반으로 도 코딩이 가능합니다. 하지만 둘다 사용하는 일은 거의 없습니다.

## 자동 설정 HibemateJpaAutoConfiguration

> application.properties

~~~
spring.datasource.url=jdbc:postgresql://localhost:5432/springjpa
spring.datasource.username=jjunpro
spring.datasource.password=pass

spring.jpa.hibernate.ddl-auto=create
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
~~~

application.properties 에 우리가 사용하는 DB에 접근할수 있는 정보를 줘야합니다.

> spring.jpa.hibernate.ddl-auto=create

create 를 줘서 개발환경에 맞춰서 실행시 스키마를 새로 만들어주도록 명령합니다.

> spring.jpa.show-sql=true

쿼리문을 표시합니다.

> spring.jpa.properties.hibernate.format_sql=true

SQL 문 쿼리가 좀더 보기 쉽게 표시되도록 하는 설정입니다.

![쿼리확인](./images/20190913_230126.png)

결과 쿼리문이 표시되는 것을 확인할 수 있습니다.

# Domain 생성

> Account.java

~~~
@Entity
@Table
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

`@Table, @Column` 해당 테이블의 테이블, 컬럼에 맵핑을 알려주는 어노테이션

@Table 과 @Column 은 생략 가능합니다.

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

# JPA 엔티티 맵핑

도메인 모델을 만들었다면 `릴레이션(테이블)에 어떻게 맵핑` 시킬지 그런 정보를 `HibemateJ(하이버네이트)` 한테 줘야합니다. 정보를 주는 방법은 크게 두가지가 있습니다.

- 1. 어노테이션을 사용하는 방법
- 2. XML을 사용하는 방법

하지만 최근에는 어노테이션을 사용하여 맵핑시켜주는 방법을 많이 사용합니다.

## 어노테이션 정보

- @Entity
    - "엔티티"는 객체 세상에서 부르는 이름.
    - 보통 클래스와 같은 이름을 사용하기 때문에 값을 변경하지 않음.
    - 엔티티의 이름은 JQL 에서 쓰임.
    - @Entity(name = "newName") 으로 선언할 경우 하이버네이트 객체 내부 안에서만 선언된 이름으로 사용됩니다.

- @Table
    - "릴레이션(테이블)" 세상에서 부르는 이름
    - @Table 이름을 선언해주지 않으면 @Entity의 이름으로 기본값을 가집니다.
    - 테이블의 이름은 SQL 에서 쓰임.

- @Id
    - 엔티티의 `주키를 맵핑`할 때 사용.
    - 자바의 모든 Primitive Type(원시 타입)과 그 Reference Type(참조 타입) 을 사용할 수 있음.
        - Date랑 BigDecimal, Biginteger도 가용 가능.
    - 복합키를 만드는 맵핑하는 방법도 있습니다.
    - 보통 Reference Type 을 사용하요 (Long Id) 식으로 만들어 구분을 명확하게 해줍니다. 테이블의 Id가 0인 레코드를 가진 Account 랑 새로만든 Account는 Reference가 Null 이라서 완전히 구분이 됩니다. Primitive 으로 (long Id)로 사용할 경우 Account는 Reference도 0 이기때문에 테이블의 Id 0 값과 겹칩니다.

- @GeneraledValue
    - `주키의 생성 방법을 맵핑`하는 에노테이션
    - 생성 전략과 생성기를 설정할 수 있다.
        - 기본 전략은 AUTO, 사용하는 DB에 따라 적절한 전략 선택
        - TABLE, SEQUENCE, IDENTITY 중 하나. DB에 따라 달라집니다.

- @Column
    - nuique
    - nullable
    - length
    - columnDefinition 등등..

- @Temporal
    - 현재 JPA 2.1까지는 Date와 Calendar만 지원.

- @Transient
    - 컬럼으로 맵핑하고 싶지 않은 멤버 변수에 사용.

~~~
@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Transient
    private String no;

    ...getter, setter
} 
~~~

![유저-생성](./images/20190913_225425.png)

# JPA Value 타입 맵핑

~~~
public class Account {
    private String username;
}
~~~

엔티티 타입은 class Account 를 가리키고 `Value 타입은 String username` 을 가리킵니다.

간단한 예를 들어 Address 클래스가 존재합니다.

~~~
public class Address { ... }
~~~

Address 가 Account 엔티티에 Value 타입으로 들어가있으면 Address 클래스는 엔티티가 아닙니다. 그 이유는 Account 가 만들어 질때 같이 만들어 져야 하고 생명주기가 Account에 있습니다. 이러한 다른 엔티티에 종속적인 타입을 Value 타입이라고 보면 됩니다.

Value 타입선언은 해당 클래스에 @Embeddable 을 선언해 주면 됩니다.

Composite 한 타입을 사용하는 방법은 @Embeddable 어노테이션을 붙여서 사용하면 됩니다.

~~~
@Embeddable
public class Address {

    @Column
    private String street;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String zipCode;
}
~~~

~~~
@Entity
class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Transient
    private String no;

    @Embedded
    private Address address;

    ...getter, setter
}
~~~

![Address](./images/20190913_234311.png)

Address 프로퍼티 들을 각각 오버라이딩 하는 방법도 있습니다.

~~~
...
@Embedded
@AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "home_street"))
})
private Address address;
~~~

![AttributeOverrides](./images/20190913_235230.png)

# JPA 1대다 맵핑

- 관계에는 항상 `두 엔티티`가 존재 합니다.
    - `둥 중 하나`는 그 관계의 `주인(owning)`이고
    - `다른 쪽은 종속된(non-owning)`쪽 입니다.
    - 해당 관계의 `반대쪽 레퍼런스를 가지고 있는 쪽이 주인`.

- `단방향`에서의 `관계의 주인은 명확`합니다.
    - `관계를 정의`한 쪽이 그 관계의 `주인`입니다.

- 단방향 @ManyToOne
    - 기본값 FK(Foreign Key) 생성

- 단방향 @OneToMany
    - 기본값은 조인 테이블 생성

- 양방향
    - FK 가지고 있는 쪽이 오너 따라서 기본값은 @ManyToOne 가지고 있는 쪽이 주인.
    - 주인 아닌쪽(@OneToMany쪽)에서 mappedBy 사용해서 관계를 맺고 있는 필드를 설정해야 합니다.

- 양방향
    - @ManyToOne (이쪽이 주인)
    - @OneToMany(mappedBy)
    - 주인한테 관계를 설정해야 DB에 반영이 됩니다.

## 단방향 @ManyToOne Study 가 주인

> Study.java

~~~
@Entity
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Account owner;

    ...getter, setter
}
~~~

Study 클래스의 주인은 누구인가 만든이는 누구인가 의 관계를 만들다고 할때 Account owner 관계를 만들 수 있습니다.

이 관계는 어떠한 Study를 만드는 클래스는 `여러개의 Study`를 만들 수 있습니다. 그러면 `Study 입장에서는 ManyToOne 입장`이 되는 것입니다.

> JpaRunner.java

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

        + Study study = new Study();
        + study.setName("Srping data jpa");
        + study.setOwner(account);

        Session session = entityManager.unwrap(Session.class);
        session.save(account);
        + session.save(study);
    }
}
~~~

study Owner 를 account 를 set 해줍니다.

이러한 과정의 결과는 Study 라는 테이블 안에 `Account 테이블의 PK(Primary Key) 를 참조`하는 `foreign key 컬럼을 생성`해서 `Study 테이블 안에` 가지고 있게 됩니다.

![JPA 1대다 맵핑](./images/20190914_002317.png)

Study 테이블 안에 owner_id 라는 컬럼이 생성됩니다.

owner_id에 대한 constraint(강제,속박)가 foreign key로 잡힙니다.

이 관계에서의 `주인은 Study 엔티티` 입니다. 그 이유는 반대쪽의 엔티티 정보를 Study에서 참조하고 있어서 입니다.

## 단방향 @OneToMany Account 가 주인

> Study.java

~~~
@Entity
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    ...getter, setter
}
~~~

Study 엔티티에서 더이상 Account owner 를 관리하지 않습니다. 
대신에 Account owner 쪽에서 관리합니다.

> Account.java

~~~
@Entity
class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Transient
    private String no;

    @Embedded
    private Address address;

    @OneToMany
    private Set<Study> studies = new HashSet<>();

    ...getter, setter
}
~~~

Account 엔티티는 자기가 만든 Study 목록을 가지고 있다고 가정한다면
한 클래스가 하나의 Study만 만든다 는 맞지 않기 때문에 한 클래스가 여러개의 Study를 만들다는 것이 옳바릅니다. 그래서 OneToMany 입니다. (하나가 여러개를 대함)

> JpaRunner.java

관계 설정은 어디서 할 수 있냐면 `주인인 쪽에서 관계 설정`을 할 수 있습니다. 이제는 Account 가 Study 관계를 가지고 있으므로 주인 입니다.

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

        Study study = new Study();
        study.setName("Srping data jpa");

        + account.getStudies().add(study);

        Session session = entityManager.unwrap(Session.class);
        session.save(account);
        session.save(study);
    }
}
~~~

결과 확인

~~~
Hibernate: 
    drop table if exists study cascade

Hibernate: 
    drop sequence if exists hibernate_sequence

Hibernate: create sequence hibernate_sequence start 1 increment 1

Hibernate:     
    create table account (
       id int8 not null,
        city varchar(255),
        state varchar(255),
        home_street varchar(255),
        zip_code varchar(255),
        created timestamp,
        password varchar(255),
        username varchar(255) not null,
        primary key (id)
    )

Hibernate: 
    create table account_studies (
       account_id int8 not null,
        studies_id int8 not null,
        primary key (account_id, studies_id)
    )

Hibernate: 
    create table study (
       id int8 not null,
        name varchar(255),
        primary key (id)
    )

Hibernate: 
    alter table if exists account 
       add constraint UK_gex1lmaqpg0ir5g1f5eftyaa1 unique (username)

Hibernate: 
    alter table if exists account_studies 
       add constraint UK_tevcop76y9etp9vx5vce7gns6 unique (studies_id)

Hibernate: 
    alter table if exists account_studies 
       add constraint FKem9ae62rreqwn7sv2efcphluk 
       foreign key (studies_id) 
       references study

Hibernate: 
    alter table if exists account_studies 
       add constraint FK4h3r1x3qcsugrps8vc6dgnn25 
       foreign key (account_id) 
       references account
~~~

(account, account_studies, study) 테이블이 3개가 생성되었습니다.

account_studies 테이블이 account id와 study id를 둘다 가지고 있습니다.

만약 데이터를 저장하게 된다면

~~~
Hibernate: 
    insert 
    into
        account
        (city, state, home_street, zip_code, created, password, username, id) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?)

Hibernate: 
    insert 
    into
        study
        (name, id) 
    values
        (?, ?)

Hibernate: 
    insert 
    into
        account_studies
        (account_id, studies_id) 
    values
        (?, ?)
~~~

![관계형결과](./images/20190914_014905.png)

account 데이블에도 study 테이블에도 관계에 대한 정보가 없습니다.

둘의 관계의 대한 정보는 account_studies 정의 되어 있는 것을 확인할 수 있습니다.

## 양방향 관계

Study 는 Account 를 Account는 Study 를 참조하려면 양방향 관계로 만들어야 합니다.

> Study.java

~~~
@Entity
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Account owner;
}
~~~

> Account.java

~~~
@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @OneToMany(mappedBy = "owner")
    private Set<Study> studies = new HashSet<>();
}
~~~

양방향 관계를 만들어 주려면 OneToMany 쪽에 mappedBy 로 이 관계가 반대쪽에 어떻게 맵핑이 되어있는지 관계를 정의한 필드를 작성하면 됩니다.

Study 클래스에서 Account 를 owner 라고 정의를 하였습니다. 이를 Account 클래스에서 owner라는것을 알려주어야 합니다. 그러면 Account 에서 Study의 필요한 관계를 또 생성하지 않고 Study에 정의되어 있는 Account owner 에 종속됩니다. 그러면 Study 가 주인이 됩니다.

그렇다면 지금은 주인은 Study 클래스 입니다.
하지만 Run 코드를 Account 에 Study를 주입하는 방식으로 실행한다면 어떻게 될까요? 그러면 account 에는 아무런 문제가 없지만 Study 테이블의 데이터 owner_id 값에는 아무런 값이 들어가지 않게 됩니다.

~~~
@Override
public void run(ApplicationArguments args) throws Exception {
    Account account = new Account();
    account.setUsername("new user");
    account.setPassword("hibernate");

    Study study = new Study();
    study.setName("Srping data jpa");

    account.getStudies().add(study);

    Session session = entityManager.unwrap(Session.class);
    session.save(account);
    session.save(study);
}
~~~

account.getStudies().add(study);

account 에 study 가 들어가는 입장

![관계형결과](./images/20190914_141130.png)

객체적으로 봤을때 둘다 양방향으로 가리키도록 해야합니다.

~~~
account.getStudies().add(study);
study.setOwner(account);
~~~

account 도 study를 study도 account 를 연관있도록 합니다.

이를 쉽게 메소드를 생성하여 사용하도록 합니다.

> Account.java

~~~
// add
public void addStudy(Study study) {
    this.getStudies().add(study);
    study.setOwner(this);
}

// remove
public void removeStudy(Study study) {
    this.getStudies().remove(study);
    study.setOwner(null);
}
~~~

# 링크

https://subicura.com/2017/01/19/docker-guide-for-beginners-1.html - [doker란?]
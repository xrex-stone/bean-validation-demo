
# Introduction
This project is to demonstrate the validation results in different way. 

My case focus on invoking API and doing some validations. For example, creating a Todo object via RESTFul API,
the code needs to validate its values (check non-null or allow the max length of string object).

In this project, I use H2 as our database. The main table schema as below (refer to /src/java/resources/h2_schema.sql).
```sql
CREATE TABLE todo
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY,
    name VARCHAR(10) NOT NULL,
    description VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);
```
This table represents Todo object. The field `name` and `description` should be null and has max length limitation.

The JPA Entity object `Todo` looks like (generated by Intellij plugin *JPA Buddy*) : 
```java
@Entity
@Data
@Table(name = "todo")
public class TodoV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "description", nullable = false, length = 20)
    private String description;
}
```

The API entrypoint is `http://127.0.0.1:8080/v1/todo`. The successful post request is :
``` shell
$ curl --location 'http://127.0.0.1:8080/v1/todo' \
--header 'Content-Type: application/json' \
--data '{
    "name" : "schedul1",
    "description" : "remind me"
}'

// response
{
    "code": "0",
    "data": {
        "id": 1,
        "name": "schedul1",
        "description": "remind me"
    }
}
```

In addition, we wrapped the response error message by `com.github.stone.exception.RestResponseEntityExceptionHandler`, 
The response can see the error was raised by which exception (see field `metadata`).
```JSON
{
    "code": "10001",
    "data": "system error",
    "metadata": "throw by class org.springframework.dao.DataIntegrityViolationException"
}
```

# Validation
There are 4 ways to validate the field of the object.

## 1. Database constraint
Immediately validate via database constraint (i.e. `NOT NULL`, `VARCHAR(10)`).

If you create an invalid request, we can find out the exception is `org.springframework.dao.DataIntegrityViolationException`, 
and the root cause is `java.sql.SQLIntegrityConstraintViolationException` showed in logging. It shows the error is raised by database layer. 

```shell
curl --location 'http://127.0.0.1:8080/v1/todo' \
--header 'Content-Type: application/json' \
--data '{
    "name" : "12345678901"
}'

// response 
{
    "code": "10001",
    "data": "system error",
    "metadata": "throw by class org.springframework.dao.DataIntegrityViolationException"
}
```
please note that we invoked `/v1/todo` here.

## 2. JPA constraint
Actually, annotation `@Column` only works for DDL schema generation. It is not responsible for validation,
but we can enable it to check fields by setting `spring.jpa.properties.hibernate.check_nullability=true`. 
Then, try to re-create an invalid request as *1. Database constraint*. you will see `org.springframework.dao.DataIntegrityViolationException`,
and the root cause changes to `org.hibernate.PropertyValueException` showed in logging. This means the code will not send insert SQL to database
, hence, it can reduce the database validation loading.

application.yml
```yaml
spring:
  jpa:
    properties:
      hibernate:
        check_nullability: true

```


## 3. Bean Validation with JPA Entity
This way make use of Bean Validation (JSR303) to validate object in application rather than database.
We can just remove the `@Column` and add Bean Validation annotations like `@NotNull` or `@Size`.

Refer to v2 Todo entity `TodoV2`.
```java
@Entity
@Data
@Table(name = "todo")
public class TodoV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 10)
    private String name;

    @NotNull
    @Size(max = 20)
    private String description;
}
```

When re-creating an invalid request. This time, we got exception `jakarta.validation.ConstraintViolationException`.
In addition, we could organize the failure messages of the fields more structure but not like previous SQL failed statement. 
```shell
curl --location 'http://127.0.0.1:8080/v2/todo' \
--header 'Content-Type: application/json' \
--data '{
    "name" : "12345678901"
}'

// response
{
    "code": "10001",
    "data": "[description] must not be null, [name] size must be between 0 and 10",
    "metadata": "throw by class jakarta.validation.ConstraintViolationException"
}
```
please note that we invoked `/v2/todo` here.

## 4. Bean validation with @Valid 
We can validate object in different layers such as Controller or Service by Bean validation annotation as well.
Below is the snippet code. It needs `@Valid` in the checkpoint of methods.

```java
@RestController
public class TodoController {

    @PostMapping("/v3/todo")
    public GenericResponse<TodoV3> createTodoV3(@Valid @RequestBody CreateTodoDto dto) {

        // expected org.springframework.web.bind.MethodArgumentNotValidException

        return new GenericResponse(SUCCESS_CODE,
                todoV3Repository.save(TodoV3.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .build()));
    }

    @Data
    public static class CreateTodoDto {

        @NotNull
        @Size(max = 10)
        private String name;

        @NotNull
        @Size(max = 20)
        private String description;
    }

}
```

Again, creating an invalid request. we will get exception `org.springframework.web.bind.MethodArgumentNotValidException`.
The effect is almost similar to *3. Bean Validation with JPA Entity*. We can also organize the failure messages as well-structure.
```shell
curl --location 'http://127.0.0.1:8080/v3/todo' \
--header 'Content-Type: application/json' \
--data '{
    "name" : "12345678901"
}'

// response
{
    "code": "10001",
    "data": "[description] must not be null, [name] size must be between 0 and 10",
    "metadata": "throw by class org.springframework.web.bind.MethodArgumentNotValidException"
}
```
please note that we invoked `/v3/todo` here.

## Conclusion
Comparison of above's different ways, I recommend using Bean Validation is a better option, because it's not related to 
any database, and could be used in different layers. It also mitigates the database constraint loading. 

# Appendix
All curl commands
```shell
// success request
curl --location 'http://127.0.0.1:8080/v1/todo' \
--data '{
    "name" : "schedul1",
    "description" : "remind me"
}'

// v1 success request
curl --location 'http://127.0.0.1:8080/v1/todo' \
--header 'Content-Type: application/json' \
--data '{
    "name" : "12345678901"
}'

// v2 success request
curl --location 'http://127.0.0.1:8080/v2/todo' \
--data '{
    "name" : "12345678901"
}'

// v3 success request
curl --location 'http://127.0.0.1:8080/v3/todo' \
--header 'Content-Type: application/json' \
--data '{
    "name" : "12345678901"
}'
```

Reference Link
- https://www.baeldung.com/hibernate-notnull-vs-nullable
- https://www.baeldung.com/javax-validation
- https://spring.io/projects/spring-data-jpa/
- https://beanvalidation.org/1.0/spec/
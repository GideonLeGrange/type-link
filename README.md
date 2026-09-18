# type-link

Type-safe SQL queries for Java, written as ordinary lambdas.

```java
var overdue = db.from(Invoice.class)
        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
        .where((i, c) -> i.dueDate().isBefore(today) && c.name().equals("Acme Corp"))
        .orderBy(Invoice::dueDate)
        .list();
```

That is not a string, and it is not a DSL that imitates SQL with method names. It is a Java
lambda, checked by the compiler. type-link reads the lambda's bytecode and generates the SQL:

```sql
SELECT Invoice.* FROM Invoice JOIN Client ON Invoice.clientId = Client.id
 WHERE Invoice.dueDate < ? AND Client.name = ? ORDER BY Invoice.dueDate
```

Rename a column and the query fails to compile. Get the types wrong and the query fails to
compile. There is no mapping file to keep in step, and no string to get wrong at run time.

## Installing

```xml
<dependency>
    <groupId>me.legrange.typelink</groupId>
    <artifactId>type-link.core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Requires **Java 25**.

## Setting up

A `Database` needs two things: where connections come from, and how your classes map to tables.

```java
Database<Bean> db = DatabaseBuilder.of(Bean.class)
        .connection(dataSource::getConnection)
        .mapper(new BeanMapper<>(Bean.class))
        .build();
```

Three mappers come with it:

| mapper | for |
|---|---|
| `RecordMapper` | Java records — the component names are the columns |
| `BeanMapper` | JavaBeans — `getFoo()` is the `foo` column |
| `TableMapper` | the interface, where neither fits |

Implementing `TableMapper` yourself is the intended path for anything with its own idea of how
classes become tables. It is how [ObjDB](https://github.com/GideonLeGrange) drives type-link
against an existing schema whose column naming it already owns.

## What you can write

Queries over one, two or three tables, joined with a lambda over both sides:

```java
db.from(Invoice.class)
  .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
  .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
  .where((_, _, p) -> p.name().equals("Bob Jones"))
  .list();
```

Aggregates, grouping and ordering:

```java
db.from(Invoice.class).where(i -> i.paid()).sum(Invoice::amount);
db.from(Invoice.class).avg(Invoice::amount);
db.from(Invoice.class).groupBy(Invoice::clientId).list(Invoice::clientId, Invoice::amount);
```

Projections, so a query returns the columns you asked for rather than whole rows:

```java
List<String> names = db.from(Client.class).list(Client::name);
List<Row2<String, Long>> pairs = db.from(Client.class).list(Client::name, Client::id);
```

Sub-queries, by putting one query inside another's lambda:

```java
db.from(Invoice.class)
  .where(i -> i.amount() > db.from(Invoice.class).avg(Invoice::amount))
  .list();
```

Also `having()`, `distinct()`, `limit()`, `orderByDescending()`, left/right/full outer joins,
`count()`, `min()`, `max()`, and arithmetic inside an aggregate.

Tested against **H2**, **MariaDB** and **PostgreSQL**.

## Catching mistakes at build time

Not everything a lambda can express can become SQL. A predicate that calls a helper method, for
instance, cannot — the decoder reads one method body and does not follow calls out of it. Left
alone, that surfaces when the query runs.

The Maven plugin finds them at build time instead:

```xml
<plugin>
    <groupId>me.legrange.typelink</groupId>
    <artifactId>type-link.plugins</artifactId>
    <version>1.1.0</version>
    <executions>
        <execution><goals><goal>validate-queries</goal></goals></execution>
    </executions>
</plugin>
```

## One thing to know about your build

**Do not strip debug information** from classes containing queries, or rather — if you do, test
that you have.

type-link reads argument types from the method descriptor, which is always present, so a
stripped build works. It has not always: an earlier decoder read them from the
`LocalVariableTable` attribute, which exists only in a debug build, and a release build
configured to strip it turned every query into a `NullPointerException` deep in the parser, with
nothing in the failure pointing at a build setting.

There is a `no-debug-info` Maven profile that runs the whole suite against stripped classes, and
a test that strips the attribute in-process during a normal build. If you are building this from
source, `mvn -P no-debug-info clean verify` is worth running from clean.

## Modules

| module | what it is |
|---|---|
| `type-link.core` | the library |
| `type-link.json` | reading and writing queries as JSON |
| `type-link.plugins` | the Maven plugin above |
| `test` | the test suite — not published |

## Licence

Apache License 2.0 — see [LICENSE](LICENSE).

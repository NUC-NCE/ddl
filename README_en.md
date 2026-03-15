# DDL Checker

[中文](./README.md) | English

A tool to compare DDL (Data Definition Language) between Oracle and GaussDB databases.

## Features

- Tablespace comparison
- Table structure (column definition) comparison
- Primary key comparison
- Index comparison
- CLI command-line interface
- SDK programming interface

## Quick Start

### CLI

```bash
# Build
mvn package

# Run comparison
java -jar target/ddl-checker-1.0.0.jar -c config.json -t tables.txt -o result.txt
```

### SDK

```xml
<!-- Parent project pom.xml -->
<dependency>
    <groupId>com.datacheck</groupId>
    <artifactId>ddl-checker</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Add custom Gauss driver -->
<dependency>
    <groupId>com.huawei.gauss</groupId>
    <artifactId>gaussjdbc</artifactId>
    <version>3.0.0</version>
</dependency>
```

```java
import com.datacheck.sdk.*;
import com.datacheck.sdk.config.*;
import com.datacheck.model.*;

List<String> tables = Arrays.asList("EMPLOYEE", "DEPARTMENT");

DdlCompareResult result = DdlComparatorBuilder.builder()
    .oracle()
        .jdbcUrl("jdbc:oracle:thin:@localhost:1521:orcl")
        .username("scott")
        .password("tiger")
    .gauss()
        .jdbcUrl("jdbc:postgresql://localhost:5432/dbname")
        .username("gauss")
        .password("gauss")
    .options()
        .compareTablespace(true)
        .compareIndex(true)
        .comparePrimaryKey(true)
        .compareColumn(true)
        .threadCount(4)
    .buildAndCompare(tables);

// Get summary
System.out.println(result.getSummary());

// Get detailed differences
for (DdlDifference diff : result.getAllDifferences()) {
    System.out.println(diff.getMessage());
}
```

## Configuration

`config.json`:

```json
{
  "oracle": {
    "jdbcUrl": "jdbc:oracle:thin:@localhost:1521:orcl",
    "username": "scott",
    "password": "tiger"
  },
  "gauss": {
    "jdbcUrl": "jdbc:postgresql://localhost:5432/dbname",
    "username": "gauss",
    "password": "gauss"
  },
  "options": {
    "compareTablespace": true,
    "compareIndex": true,
    "comparePrimaryKey": true,
    "compareColumn": true
  },
  "threadCount": 4,
  "outputDir": "./output"
}
```

## Table List

`tables.txt`:

```
EMPLOYEE
DEPARTMENT
SALARY
```

One table name per line. Lines starting with `#` are comments.

## CLI Options

| Option | Description | Default |
|--------|-------------|---------|
| -c, --config | Configuration file path | config.json |
| -t, --tables | Table list file path | tables.txt |
| -o, --output | Output file path | Console |
| -h, --help | Show help | - |
| -v, --version | Show version | - |

## Output Example

```
========================================
DDL Compare Report
========================================
Generated: 2026-03-15 12:00:00

Summary:
----------------------------------------
Total Tables: 3
Matched: 1
Tables with Differences: 2
Total Differences: 5

Detailed Differences:
----------------------------------------

Table: EMPLOYEE
  [DIFFERENT] Table: EMPLOYEE, COLUMN_TYPE: SALARY | Oracle=NUMBER(10,2) vs Gauss=NUMERIC
  [DIFFERENT] Table: EMPLOYEE, COLUMN_NULLABLE: HIRE_DATE | Oracle=NULL vs Gauss=NOT NULL

Table: DEPARTMENT
  [ORACLE_ONLY] Table: DEPARTMENT, INDEX: IDX_DEPT_NAME
```

## Exit Codes

- 0: Completed, no differences
- 1: Completed, differences found
- 2: Database connection error
- 3: IO error
- 4: Other errors

## Driver Note

JDBC drivers in this project are marked as `optional` and excluded by default during packaging. Parent projects can include custom versions:

- Oracle: `com.oracle.database.jdbc:ojdbc8`
- GaussDB: `com.huawei.gauss:gaussjdbc` or PostgreSQL compatible driver

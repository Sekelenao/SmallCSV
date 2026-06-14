<p align="center">
  <img src=".idea/icon.svg" width="250" alt="logo">
</p>

<h2 align="center">
CSV as simple as it is
</h2>

[![Java](https://img.shields.io/badge/Java_21%2B-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/21/docs/api/index.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/small-csv?label=Maven%20central&logo=apachemaven&logoColor=white&color=C71A36&labelColor=C71A36)](https://central.sonatype.com/artifact/io.github.sekelenao/small-csv)
![Tests](https://raw.githubusercontent.com/Sekelenao/SmallCSV/badges/Tests.svg)
![Coverage](https://raw.githubusercontent.com/Sekelenao/SmallCSV/badges/Coverage.svg)
![Branches](https://raw.githubusercontent.com/Sekelenao/SmallCSV/badges/Branches.svg)

## Description

SmallCSV is a library designed for easy data manipulation, simplifying the import and export processes. You don't need to
worry about special characters or other complexities during parsing. The focus has been directed towards prioritizing
speed and security. The level of abstraction allows you to forget that you're working with a CSV format.

## Changelog

### 1.0.0

Initial release (as SkCsv).

### 1.0.1

Fixed a bug when trying to iterate over an empty CSV file.

### 2.0.0

Major release with API cleanup and rename to SmallCSV:
- Renamed the library artifact from `SkCsv` to `small-csv`.
- Renamed core classes: `SkCsv` -> `Csv`, `SkCsvRow` -> `Row`, `SkCsvConfig` -> `CsvConfiguration`, and `SkCsvRecords` -> `Csvs`.
- Hid all constructors in favor of static factory methods: `Csv.empty()`, `Csv.of(...)`, `Row.empty()`, and `Row.of(...)`.
- Decoupled I/O operations from model classes by transferring all `from(...)` and `export(...)` methods into the `Csvs` utility class.
- Added support for exporting directly to an `OutputStream` for both `Csv` objects and record collections.
- Standardized file paths using Java NIO.

## How to install

### Using Maven

You can use the following Maven dependency:

```xml
<dependency>
    <groupId>io.github.sekelenao</groupId>
    <artifactId>small-csv</artifactId>
    <version>{version}</version>
</dependency>
```

### Using Gradle

You can add the following Gradle dependency:

```groovy
implementation 'io.github.sekelenao:small-csv:{version}'
```

### Using JAR files:

You can download the JAR files (including Javadoc and classes) [here](https://github.com/Sekelenao/SmallCSV/tree/main/jars).

The next step is to consult the documentation of the build tool you are using to learn how to import dependencies correctly.

## SmallCSV Library Architecture

### 1. Core CSV Data Structures

- **Row**: Represents a row in a CSV file. Provides methods for manipulating and accessing row data.
- **Csv**: Represents a CSV file as a whole. Allows manipulation of file rows and configuration properties.

### 2. I/O Operations & Record Serialization

- **Csvs**: Consolidates all static factories to parse CSV files/streams (`Csvs.from(...)`) and write CSV data or serialize Java records to paths and output streams (`Csvs.export(...)`).
- **CsvColumn**: Annotation used to mark components of a record that should be exported to a CSV file.

### 3. Configuration

- **CsvConfiguration**: Represents the delimiter and quote characters used for formatting and parsing CSV data.

## Documentation

Please note that this documentation provides an overview of the core functionalities and usage of the SmallCSV library. 
While it covers the fundamental aspects to get you started, it does not encompass every feature available. For a complete
understanding of all the functionalities and detailed class descriptions, we encourage you to refer to the `Javadoc`.

The documentation here aims to introduce the main concepts and classes to help you navigate and use the library effectively.
By familiarizing yourself with the basics, you'll be better equipped to explore and utilize the full range of capabilities
provided in the `Javadoc`.

### Create a Csv object with its content from Java

You can create a `Csv` object with its content from Java using static factories:

```java
var csv = Csv.empty(); // Empty CSV
var csv2 = Csv.of(     // Csv Containing 5 rows
        Row.of("Project", "Language", "Made with heart ?"),
        Row.of("SmallCSV", "Java", "YES !"),
        Row.empty(),
        Row.of("Don\"t worry;", "It's working anyway\n :)"),
        Row.of("END", "")
);
```

Printing the second object will result in the following text:

```
Project;Language;Made with heart ?
SmallCSV;Java;ALWAYS !

"Don""t worry;";"It's working anyway
 :)"
END;

```

### Import an existing CSV file and map it to a Java object

Now we have a `CSV` file with some languages and their descriptions, separated by a tab and quoted by double quotes.

```
Language	Creation date	File extension
Java	1995	.java
Python	1991	.py
Powershell	2006	.ps1
Ocaml	1996	.ml
Fortran	1957	.f90
C	1972	.c
Typescript	2012	.ts
```

To import this file, we can use the `Csvs` utility class:

```java
Csvs.from(CSV_PATH, new CsvConfiguration('\t', '"'));
Csvs.from(CSV_PATH, new CsvConfiguration('\t', '"'), StandardCharsets.UTF_8);
```

If the CSV uses the library's default format, which is a semicolon as a separator and double quotes for quoting, you can use:

```java
Csvs.from(CSV_PATH);
Csvs.from(CSV_PATH, StandardCharsets.UTF_8);
Csvs.from(CSV_PATH, CsvConfiguration.SEMICOLON);
```

Printing any of these objects will result in:

```
Language;Creation date;File extension
Java;1995;.java
Python;1991;.py
Powershell;2006;.ps1
Ocaml;1996;.ml
Fortran;1957;.f90
C;1972;.c
Typescript;2012;.ts
```

### Export a Csv object to a file or stream

To export a `Csv` object to a file using the default format, you can use the following code:
```java
var csv = Csvs.from(CSV_PATH, new CsvConfiguration('\t', '"'));
var exportPath = Paths.get("new_csv.csv");
Csvs.export(exportPath, csv);
```

If you need to apply a custom format, you can configure the `Csv` object before exporting:

```java
var csv = Csvs.from(CSV_PATH, new CsvConfiguration('\t', '"'));
var exportPath = Paths.get("new_csv.csv");
Csvs.export(exportPath, csv.configure(CsvConfiguration.COMMA));
```

### Export records without modifying the previous code

Imagine we have a massive and critical banking application. We would like to export our data to a CSV, but because we're
wary of tampering with these precious records, we don't want to add any code inside or export sensitive data.

Therefore, we will add an annotation to designate the data that we want to export.

```java
public record BankAccount(@CsvColumn String bankName, @CsvColumn UUID uuid, @CsvColumn BigDecimal balance, int secretCode) {}
```

Let's imagine that within the application, we have a way to obtain these records as an Iterable, as shown below.

```java
import java.util.Iterator;

private static final Iterable<BankAccount> BANK_ACCOUNT_ITERABLE =
        new Iterable<BankAccount>() {...};
```

In terms of lines of code, we'll only need a single line! The parser will take care of everything as usual;
it uses the `String.valueOf` method for annotated types. Unlike the rest of the library, the absence of null values is
not guaranteed!

```java
public final class Main {

    public static void main(String[] args) throws IOException {
        Csvs.export(Paths.get("out.csv"), BANK_ACCOUNT_ITERABLE);
    }

}
```

To obtain a huge CSV file of 1_000_000 records in ~2 seconds:

```
OnlyBank;a78e71e4-4c6f-4fde-990e-21274c2bd809;57.44878667185084
OnlyBank;2ebf4749-77e3-4158-85c6-5423c5f0b791;80.68069692551795
OnlyBank;89bd2b50-1aa5-4bb0-bf19-15defb47a0ed;51.924661497651535
```

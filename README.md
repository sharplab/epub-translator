# epub-translator 

[![Actions Status](https://github.com/sharplab/epub-translator/workflows/CI/badge.svg)](https://github.com/sharplab/epub-translator/actions)

epub-translator is an utility to translate EPub books.

- Utilize DeepL API (You need to register DeepL API plan)
- Leave the original text for reference, and insert the translated text below per paragraph

This project uses Quarkus, the Supersonic Subatomic Java Framework.
If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .


## Build

The application can be packaged using following command:

```
./gradlew quarkusBuild --uber-jar
```

It produces the `epub-translator-0.5.0-SNAPSHOT-runner.jar` file in the `build` directory.

## Creating a native executable

You can create a native executable in this way:
```
 ./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using following command:
```
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with 

```
./build/epub-translator-1.0.0-SNAPSHOT-runner
```

## Execution

```
java -jar epub-translator.jar --src <path to source epub file> [--dst <path to destination epub file>] \
[--srcLang <source language>] [--dstLang <destination language>]
```

## Configuration

place application.yml to `<epub-translator working directory>/config/application.yml`

#### application.yml

```
ePubTranslator:
  deepL:
    apiKey: <put your api key here>
  language:
    source: en        # default source language
    destination: ja   # default destination language
```


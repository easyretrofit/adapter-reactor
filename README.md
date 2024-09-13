[![Version](https://img.shields.io/maven-central/v/io.github.easyretrofit/adapter-reactor?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.github.easyretrofit/adapter-reactor)
[![Build](https://github.com/easyretrofit/adapter-reactor/actions/workflows/build.yml/badge.svg)](https://github.com/easyretrofit/adapter-reactor/actions/workflows/build.yml/badge.svg)
[![License](https://img.shields.io/github/license/easyretrofit/adapter-reactor.svg)](http://www.apache.org/licenses/LICENSE-2.0)


# call-adapter-reactor
Request Call Adapter for Retrofit Implementation Based on Java Reactor Framework

## Usage
Maven:
```xml
<dependency>
  <groupId>io.github.easyretrofit</groupId>
  <artifactId>adapter-reactor</artifactId>
  <version>${latest.version}</version>
</dependency>
```
Gradle:
```groovy
implementation 'io.github.easyretrofit:adapter-reactor:${latest.version}'
```

### used with easy-retrofit

#### create ReactorCallAdapterFactoryBuilder class
```java
public class ReactorCallAdapterFactoryBuilder extends BaseCallAdapterFactoryBuilder {
    @Override
    public CallAdapter.Factory buildCallAdapterFactory() {
        return ReactorCallAdapterFactory.create();
    }
}

```
#### add ReactorCallAdapterFactoryBuilder to your RetrofitBuilder
```java
@RetrofitBuilder(baseUrl = "${app.backend.url}",
        addCallAdapterFactory = {ReactorCallAdapterFactoryBuilder.class})
public interface MyService {
    
}
```

### used with retrofit2
```java
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addCallAdapterFactory(ReactorCallAdapterFactory.create())
        .build();
```




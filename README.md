# lecousin.net - Java framework - Injection

This library provides dependency injection functionalities.

It depends on the [net.lecousin.core]("https://github.com/lecousin/java-framework-core" "java-framework-core") library
mainly for parsing configuration files.

An object to inject can be a singleton, or using a factory. When using a factory, a new object is created by the factory
each time it needs to be injected.

When an object needs to be injected, it can be injected
 * by ID, by specifying the ID of the object or the ID of the factory
 * by type, in this case the object injected will be the available object matching this type
 
In addition, conditions can be specified on when an injectable object (or factory) is eligible, based on properties.
For example, a production implementation can be injected when a property is set to 'PROD', else a mock implementation
can be injected.

An injectable object can also declare an initialization method to be called once instantiated, and its dependencies
have been injected if any.

Injection is done in an _Injection Context_, a context being composed of:
 * Properties, that can be used in a configuration file or in the @InjectableWhen annotation
 * Singletons, which are instances of objects ready to be injected
 * Factories, which can be used to create an instance each time an object needs to be injected
 * An optional parent context. If it has a parent, a context inherits from its content.
 
## Build status

### Current version - branch master

[![Maven Central](https://img.shields.io/maven-central/v/net.lecousin.framework/injection.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.lecousin.framework%22%20AND%20a%3A%22injection%22)
[![Javadoc](https://img.shields.io/badge/javadoc-0.1.4-brightgreen.svg)](https://www.javadoc.io/doc/net.lecousin.framework/injection/0.1.4)

![build status](https://travis-ci.org/lecousin/java-framework-injection.svg?branch=master "Build Status")
![build status](https://ci.appveyor.com/api/projects/status/github/lecousin/java-framework-injection?branch=master&svg=true "Build Status")
[![Codecov](https://codecov.io/gh/lecousin/java-framework-injection/graph/badge.svg)](https://codecov.io/gh/lecousin/java-framework-injection/branch/master)

### Next minor release - branch 0.1

![build status](https://travis-ci.org/lecousin/java-framework-injection.svg?branch=0.1 "Build Status")
![build status](https://ci.appveyor.com/api/projects/status/github/lecousin/java-framework-injection?branch=0.1&svg=true "Build Status")
[![Codecov](https://codecov.io/gh/lecousin/java-framework-injection/branch/0.1/graph/badge.svg)](https://codecov.io/gh/lecousin/java-framework-injection/branch/0.1)
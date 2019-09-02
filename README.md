# File lister

[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](/LICENSE.txt) [![Download](https://api.bintray.com/packages/gmullerb/all.shared.gradle/file-lister/images/download.svg)](https://bintray.com/gmullerb/all.shared.gradle/file-lister/_latestVersion) [![coverage report](https://gitlab.com/gmullerb/file-lister/badges/master/coverage.svg)](https://gitlab.com/gmullerb/project-style-checker/commits/master)

**File lister offers a small set of utilities for listing files to Gradle projects.**

This project is licensed under the terms of the [MIT license](/LICENSE.txt).
__________________

## Quick Start

1 . Apply the plugin:

`build.gradle`:

```gradle
 plugins {
   id 'all.shared.gradle.file-lister' version '1.0.2'
 }
```

2 . Use `fileLister` methods, `obtainPartialFileTree` and/or `obtainFullFileTree`:

`build.gradle`:

```gradle
  final someFilesInTree = fileLister.obtainPartialFileTree()
  final allFilesInTree = fileLister.obtainFullFileTree()
```

3 . Jump to [Using/Configuration](#Using/Configuration), for customization or digging on How it works.
__________________

## Goal

Get a file tree from a project's specified folder, excluding by default Gradle's files and folders: `**/gradlew.*`, `**/gradle`, `**/.gradle` and `**/build`, excluding by default files and/or folders listed in the `.gitignore`'s files found in the folder tree, excluding by default Nodes' folder `**/node_modules`, and optionally excluding and/or including a set of custom ANT patterns.

## Features

Basically offers a small set of functions:

* `ConfigurableFileTree obtainFullFileTree(folder, [excludes:[..], includes:[..]])`: A function for recursively listing all files and/or folders from a project's specified folder, excluding and/or including a set of custom ANT patterns.
* `ConfigurableFileTree obtainPartialFileTree(folder, [excludes:[..], includes:[..]])`: A function for recursively listing all files and/or folders from a project's specified folder, excluding and/or including a set of custom ANT patterns and excluding files listed in the `.gitignore`'s files found in the folder tree.

When using the core `fileTree` method, it excludes some file/folders by default[1], mainly commanded by ANT, e.g.: `**/.cvsignore`, `**/.git`, `**/.gitignore`, `**/.svn`, etc; that is good, but this plugin provides:

* Additional exclusions based on Gradle: `**/.gradle` and `**/gradle-wrapper.jar`.
* Also one of the methods, `obtainPartialFileTree`, additionally excludes by default files/folders taking in account also the `.gitignore` files information (which is what is not sent from the project to the repository, what usually means that is not relevant to the project).
  * Git patterns containing `!`,`[` & `]` are not considered by the plugin.

E.g.[2]:

Given the following folder structure:

```
  .gitignore
  /.git
    file1.ext1
  /folderA
    file1.ext1
    file1.ext3
    /gradle
      file1.ext1
  /folderB
    .gitignore
     file1.ext1
     file1.ext2
     file1.ext3
    /node_modules
      file1.ext1
```

* With `/.gitignore` having: `*.ext2`
* With `/folderB/.gitignore` having: `*.ext3`

The result will be:

* When using `fileTree()`: `/folderA/file1.ext1`, `/folderA/file1.ext3`, `/folderA/gradle/file1.ext1`, `/folderB/file1.ext1`, `/folderB/file1.ext2`, `/folderB/file1.ext3` and `/folderB/node_modules/file1.ext1`
* When using `obtainFullFileTree)`: `/folderA/file1.ext1`, `/folderA/file1.ext3`, `/folderB/file1.ext1`, `/folderB/file1.ext2` and `/folderB/file1.ext3`
* When using `obtainPartialFileTree()`: `/folderA/file1.ext1`, `/folderA/file1.ext3` and `/folderB/file1.ext1`

> [1] [All ANT default excludes](https://ant.apache.org/manual/dirtasks.html#defaultexcludes).  
> [2] For an actual use example, see [basecode project](https://github.com/gmullerb/basecode).
__________________

## Using/Configuration

### Prerequisites

* None

### Gradle configuration

1. Apply the plugin:

```gradle
 plugins {
   id 'all.shared.gradle.file-lister' version '1.0.2'
 }
```

2. Use the plugin, it will add a property named `fileLister` to the `Project`:

* Without folder or filters: `final filesInTree = fileLister.obtainPartialFileTree()`, this will walk through project folder.
* Without filters: `final filesInTree = fileLister.obtainFullFileTree('someFolder')`, this will walk through `someFolder` inside the project folder.
* Without including: `final filesInTree = fileLister.obtainPartialFileTree('someFolder', [excludes: 'someANTpattern'])`, this will walk through `someFolder` inside the project folder, excluding `someANTpattern` pattern.
* Without excluding: `final filesInTree = fileLister.obtainFullFileTree('someFolder', [includes: 'someANTpattern'])`, this will walk through `someFolder` inside the project folder, including `someANTpattern` pattern.
* Without folder: `final filesInTree = fileLister.obtainPartialFileTree('.', [excludes: 'someANTpattern1', includes: 'someANTpattern2'])`, this will walk through project folder, excluding `someANTpattern1` pattern and including `someANTpattern2` pattern [1].
* With all: `final filesInTree = fileLister.obtainFullFileTree('someFolder', [excludes: 'someANTpattern1', includes: 'someANTpattern2'])`, this will walk through `someFolder`, excluding `someANTpattern1` pattern and including `someANTpattern2` pattern [1].

> [1] `excludes` pattern has precedence over `includes` patterns.

### Using inside a plugin

1 . Add dependency:

```gradle
  repositories {
    jcenter()
    maven {
      url 'https://plugins.gradle.org/m2/'
    }
    maven {
      url 'https://dl.bintray.com/gmullerb/all.shared.gradle'
    }
  }

  dependencies {
    compile gradleApi()
    compile 'gradle.plugin.all.shared.gradle.file-lister:file-lister:+'
  }
```

2 . Add plugin programmatically:

```gradle
  import all.shared.gradle.file.FileListerExtension
  import all.shared.gradle.file.FileListerPlugin

  ..

    if (project.extensions.findByName(FileListerPlugin.EXTENSION_NAME) == null) {
      plugin.apply(new FileListerPlugin())
    }
```

3 . Access `filelister`:

```gradle
  ..
  final FileTree result = ((FileListerExtension) project.extensions
      .findByName(FileListerPlugin.EXTENSION_NAME))
      .obtainPartialFileTree()
  result.visit {
    ..
  }
  ..
```
__________________

## Extending/Developing

### Prerequisites

* [Java](http://www.oracle.com/technetwork/java/javase/downloads).
* [Git](https://git-scm.com/downloads) (only if you are going to clone the project).

### Getting it

Clone or download the project[1], in the desired folder execute:

```sh
git clone https://github.com/gmullerb/file-lister
```

> [1] [Cloning a repository](https://help.github.com/articles/cloning-a-repository/)

### Set up

* **No need**, only download and run (It's Gradle! Yes!).

### Building it

* To build it:
  * `gradlew`: this will run default tasks, or
  * `gradlew build`.

* To assess files:
  * `gradlew assessCommon`: will check common style of files.
  * `gradlew assessGradle`: will check code style of Gradle's.
  * `gradlew assess`: will check code style of Groovy's.
    * `gradlew codenarcMain`: will check code style of Groovy's source files.
    * `gradlew codenarcTest`: will check code style of Groovy's test files.

* To test code: `gradlew test`
  * This task is finalized with a Jacoco Report.

* To get all the tasks for the project: `gradlew tasks --all`

### Folders structure

```
  /src
    /main
      /groovy
    /test
      /groovy
```

- `src/main/groovy`: Source code files.
  - [`FileListerExtension`](src/main/groovy/all/shared/gradle/file/FileListerExtension.groovy) is where all the magic happens.
- `src/test/groovy`: Test code files[1].

> [1] Tests are done with [JUnit](http://junit.org) and [Mockito](http://javadoc.io/page/org.mockito/mockito-core/latest/org/mockito/Mockito.html).

### Convention over Configuration

All `all.shared.gradle` plugins define:

* _PluginName_**Plugin**: which contains the class implements `Plugin` interface.
* _PluginName_**Extension**: which represent the extension of the plugin.
* If Tasks are define, then their names will be _TaskName_**Task**.
* If Actions are define, then their names will be _ActionName_**Action**.

All `all.shared.gradle` plugins have two **`static`** members:

* `String EXTENSION_NAME`: This will have the name of the extension that the plugin add.
  * if the plugin does not add an extension the this field will not exist.

* `String TASK_NAME`: This will have the name of the **unique** task that the plugin add.
  * if the plugin does not add a task or add more than one task, then this field will not exist.

* `boolean complement(final ..)`: will apply the plugin and return true if successful, false otherwise.
  * this methods is **exactly equivalent to the instance `apply` method**, but without instantiate the class if not required.

Both may be useful when applying the plugin when creating custom plugins.

All `all.shared.gradle` plugins "silently" fail when the extension can not be added.

## Documentation

* [`CHANGELOG.md`](CHANGELOG.md): add information of notable changes for each version here, chronologically ordered [1].

> [1] [Keep a Changelog](http://keepachangelog.com)

## License

[MIT License](/LICENSE.txt)
__________________

## Remember

* Use code style verification tools => Encourages Best Practices, Efficiency, Readability and Learnability.
* Start testing early => Encourages Reliability and Maintainability.
* Code Review everything => Encourages Functional suitability, Performance Efficiency and Teamwork.

## Additional words

Don't forget:

* **Love what you do**.
* **Learn everyday**.
* **Learn yourself**.
* **Share your knowledge**.
* **Learn from the past, dream on the future, live and enjoy the present to the max!**.

At life:

* Let's act, not complain.
* Be flexible.

At work:

* Let's give solutions, not questions.
* Aim to simplicity not intellectualism.

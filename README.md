# SudoScan
![master status](https://github.com/pintowar/sudoscan/actions/workflows/gradle_master.yml/badge.svg?branch=master)
[![Sonar Coverage](https://sonarcloud.io/api/project_badges/measure?project=pintowar_sudoscan&metric=coverage)](https://sonarcloud.io/dashboard?id=pintowar_sudoscan)

![develop status](https://github.com/pintowar/sudoscan/actions/workflows/gradle_develop.yml/badge.svg?branch=develop)

![GitHub tag (latest)](https://img.shields.io/github/v/tag/pintowar/sudoscan)
![GitHub license](https://img.shields.io/github/license/pintowar/sudoscan)

Scan and Solve Sudoku Puzzles

## Project Info

This is a toy project for educational purpose.
I usually use this project to explore some JVM/Kotlin libs, new Gradle features/plugins,
AI libs and CI pipes (using github actions).

[![Sudoscan Project](http://img.youtube.com/vi/8D4gMhDRu-U/0.jpg)](https://youtu.be/8D4gMhDRu-U "Sudoscan Project")

For a more detailed explanation of how the project works, check out the [Project Blog](https://pintowar.github.io/sudoscan/).

### Project Concepts

The main objective of this project is to use an image of a sudoku puzzle as input, identify the puzzle, 
recognize the numbers, solve the problem and plot the solution back to the original image.

That been said, the project can be divided in concepts:

* Extractor/Plotter: computer vision components responsible the read/write information from/to an image;
* Recognizer: responsible to recognize the number images and translate it into an integer;
* Solver: responsible to effectively solve the sudoku puzzle;
* Engine: a pipe that glues all above components together to achieve the main objective of the project.

### Project Modules

The project was broken into the following modules (using java SPI):

* sudoscan-api: module containing domain classes, spi for solvers and recognizers and main engine;
* sudoscan-cv-opencv: module containing a spi implementation using [javacv](https://github.com/bytedeco/javacv)
(computer vision wrapper around opencv) to work on the image processing pipe;
* sudoscan-solver-choco: module containing a spi implementation using 
[choco solver](https://github.com/chocoteam/choco-solver) (a CSP solver) to solve to find the sudoku solution;
* sudoscan-solver-ojalgo: module containing a spi implementation using 
[ojAlgo](https://github.com/optimatika/ojAlgo) (a MIP solver) to solve to find the sudoku solution;
* sudoscan-recognizer-djl: module containing a spi implementation using [djl](https://github.com/deepjavalibrary/djl) 
(a deep learning framework) to recognize numeric images;  
* sudoscan-recognizer-dl4j: module containing a spi implementation using 
[dl4j](https://github.com/eclipse/deeplearning4j) (a deep learning framework) to recognize numeric images (this is the 
default recognizer);
* sudoscan-cli: cli application using other modules to solve sudoku problems using a webcam as user interface.

Both solvers (choco-solver and ojalgo) are implementations that use different discrete optimizations approaches. To
learn more about MIP and CSP approaches on Sudoku problems, take a look at the following 
[Kaggle Notebook](https://www.kaggle.com/pintowar/modeling-a-sudoku-solver-with-or-tools).

Both recognizers (dlf4 and djl) are implementations that use a pre-trained model. The model creation and training can be
found on the following [Kaggle Notebook](https://www.kaggle.com/pintowar/sudoscan-number-recognizer).

### Project Modules Usage

Add api, cv, solver and recognizer the dependencies

```kotlin
implementation("com.github.pintowar:sudoscan-api:x.y.z")
implementation("com.github.pintowar:sudoscan-cv-opencv:x.y.z")
implementation("com.github.pintowar:sudoscan-solver-choco:x.y.z") // or sudoscan-solver-ojalgo
implementation("com.github.pintowar:sudoscan-recognizer-dl4j:x.y.z") // or sudoscan-recognizer-djl
```

## Building Project

To build the fat jar client version of the app, run the following command:

`gradle -PjavacppPlatform=linux-x86_64,macosx-x86_64,windows-x86_64 clean assembleCliApp`

The command above will build a fat jar containing the native dependencies of all informed platforms. 
To build a more optimized jar, just inform the desired platform, for instance: 

`gradle -PjavacppPlatform=linux-x86_64 clean assembleCliApp`

This second command will build a smaller jar, but it will run only on linux-x86_64 platforms.

It is also possible to chose in compile time, which solver/recognizer module to use. The commands above (by default) 
will generate a jar using **sudoscan-solver-choco** and **sudoscan-recognizer-dl4j** as the main solver and recognizer. 
To use **sudoscan-solver-ojalgo** and **sudoscan-recognizer-djl** as solver and recognizer components, 
run the following command:

`gradle -Pojalgo -Pdjl -PjavacppPlatform=macosx-x86_64 clean assembleCliApp`

### Native Image Build

It is also possible to generate a native image of the sudoscan-cli, using a special JVM called the 
[GraalVM](https://www.graalvm.org/).

GraalVM provides this mechanism that helps to create a native image of the application which can be executed as a 
standalone executable. The build process builds the executable with all the required dependencies such that 
there is no need the JVM to run the application.

The latest GraalVM version tested was GraalVM CE 21.2.0 (graalvm-21.2.0+java11) which is for Java 11. 
To build the native image, run the following command on `sudoscan-cli` module:

`gradle -PjavacppPlatform=linux-x86_64 clean nativeCompile`
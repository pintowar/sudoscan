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

### Project Concepts

The main objective of this project is to use an image of a sudoku puzzle as input, identify the puzzle, 
recognize the numbers, solve the problem and plot the solution back to the original image.

That been said, the project can be divided in concepts:

* Parser/Plotter: computer vision components responsible the read/write information from/to an image;
* Recognizer: responsible to recognize the number images and translate it into an integer;
* Solver: responsible to effectively solve the sudoku puzzle;
* Engine: a pipe that glues all above components together to achieve the main objective of the project.

### Project Modules

The project was broken into the following modules (using java SPI):

* sudoscan-api: module containing domain classes, spi for solvers and recognizers and main engine.
* sudoscan-solver-choco: module containing a spi implementation using 
[choco solver](https://github.com/chocoteam/choco-solver) (a CSP solver) to solve to find the sudoku solution;
* sudoscan-recognizer-djl: module containing a spi implementation using [djl](https://github.com/deepjavalibrary/djl) 
(a deep learning framework) to recognize numeric images;  
* sudoscan-recognizer-dl4j: module containing a spi implementation using 
[dl4j](https://github.com/eclipse/deeplearning4j) (a deep learning framework) to recognize numeric images (this is the 
default recognizer);
* sudoscan-deskapp: desktop application using all modules to solve sudoku problems using a webcam as user interface.

## Building Project

To build the fat jar desktop version of the app, run the following command:

`gradle -PjavacppPlatform=linux-x86_64,macosx-x86_64,windows-x86_64 clean assembleDesktopApp`

The command above will build a fat jar containing the native dependencies of all informed platforms. 
To build a more optimized jar, just inform the desired platform, for instance: 

`gradle -PjavacppPlatform=linux-x86_64 clean assembleDesktopApp`

This second command will build a smaller jar, but it will run only on linux-x86_64 platforms.

It is also possible to chose in compile time, which recognizer module to use. The commands above (by default) will 
generate a jar using **sudoscan-recognizer-dl4j** as the main recognizer. To use **sudoscan-recognizer-djl** as 
recognizer component, run the following command:

`gradle -Pdjl -PjavacppPlatform=macosx-x86_64 clean assembleDesktopApp`
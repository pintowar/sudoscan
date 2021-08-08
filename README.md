# SudoScan
![master status](https://github.com/pintowar/sudoscan/actions/workflows/gradle_master.yml/badge.svg?branch=master)

![develop status](https://github.com/pintowar/sudoscan/actions/workflows/gradle_develop.yml/badge.svg?branch=develop)
[![Sonar Coverage](https://sonarcloud.io/api/project_badges/measure?project=pintowar_sudoscan&metric=coverage)](https://sonarcloud.io/dashboard?id=pintowar_sudoscan)

![GitHub tag (latest)](https://img.shields.io/github/v/tag/pintowar/sudoscan)
![GitHub license](https://img.shields.io/github/license/pintowar/sudoscan)

Scan and Solve Sudoku Puzzles

## Building Project

To build the fat jar desktop version of the app, run the following command:

`gradle -PjavacppPlatform=linux-x86_64,macosx-x86_64,windows-x86_64 clean assembleDesktopApp`

The command above will build a fat jar containing the native dependencies of all informed platforms. 
To build a more optimized jar, just inform the desired platform, for instance: 

`gradle -PjavacppPlatform=linux-x86_64 clean assembleDesktopApp`

This second command will build a smaller jar, but it will run only on linux-x86_64 platforms. 
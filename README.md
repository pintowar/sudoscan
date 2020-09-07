# SudoScan
Scan and Solve Sudoku

## Building Project

To build the fat jar desktop version of the app, run the following command:

`gradle -PjavacppPlatform=linux-x86_64,macosx-x86_64,windows-x86_64 clean assembleDesktopApp`

The command above will build a fat jar containing the native dependencies of all informed platforms. 
To build a more optimized jar, just inform the desired platform, for instance: 

`gradle -PjavacppPlatform=linux-x86_64 clean assembleDesktopApp`

This second command will build a smaller jar, but it will run only on linux-x86_64 platforms. 
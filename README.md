# MiniJava_Compiler
 
 Implemented a compiler for MiniJava, a subset of Java.

## Dependencies

In order to execute the produced LLVM intermediate code you should install the Clang compiler. For the testing purposes of this project clang-4.0 was used. You can install Clang by using the following command in any Linux distro:

```
sudo apt-get install clang-4.0
```

## Compile and Run

In order to compile the project you simply need to run make in the scr file
```
cd src
make
```

From the main directory, execute
```
java Main <inputFile>
```

In order to run the compiled LLVM code you'll need to create an executable file
```
clang-4.0 <fileName> -o out**
```
and then simply run it.
```
./out
```

# Example
Let's say we want to compile and run the following file: `input/Factorial.java`

Firstly, we need to compile our project:
```
make
```
Secondly, we want to compile input/Factorial.java using our MiniJava compiler:
```
java Main input/Factorial.java
```
This will produce the file Factorial.ll in the /output directory. Then we want to compile this intermediate LLVM code using Clang:
```
clang-4.0 output/Factorial.ll -o out
```
Finally, all that is left to do is to execute the executable:
```
./out
```

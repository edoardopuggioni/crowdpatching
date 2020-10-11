After having cloned this repository, follow these instructions to
get everything ready for running Jsnark and Libsnark executables.

# Jsnark instructions

`cd zksnarks/JsnarkCircuitBuilder`

## Install Java JDK

`sudo apt install openjdk-8-jdk`

## Install Junit4

`sudo apt-get install junit4`

## Download BouncyCastle

`wget https://www.bouncycastle.org/download/bcprov-jdk15on-159.jar`

## Compile everything

`javac -d bin -cp /usr/share/java/junit4.jar:bcprov-jdk15on-159.jar  $(find ./src/* | grep ".java$")`


# Libsnark instructions

`cd zksnarks/libsnark`

## Create a build directory

`mkdir build`

`cd build`

## Use cmake before compiling

`cmake ..`

## Compile everything

`make`

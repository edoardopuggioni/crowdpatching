The zk-SNARKs implementation for _CrowdPatching_ employs the [_jsnark_](https://github.com/akosba/jsnark) library, which in turn uses [_libsnark_](https://github.com/scipr-lab/libsnark) as sub-module, among many other dependencies.

# Prerequisites

## Prerequisites for _libsnark_

- Tested on Ubuntu 20.04
    ```
    $ sudo apt-get install build-essential cmake git libgmp3-dev libprocps-dev python-markdown libboost-all-dev libssl-dev
    ```
- For other distributions check the [_jsnark_](https://github.com/akosba/jsnark#prerequisites) and [_libsnark_](https://github.com/scipr-lab/libsnark#dependencies) instructions

## Prerequisites for _jsnark_

- Install JDK8
    ```
    $ sudo apt install openjdk-8-jdk
    ```
- Install Junit4
    ```
    $ sudo apt-get install junit4
    ```
- BouncyCastle
    - File `bcprov-jdk15on-159.jar` already included in this repository
    - It is assumed to be placed in the `JsnarkCircuitBuilder` directory
    - However you can download it with the following command:
        ``` 
        $ wget https://www.bouncycastle.org/download/bcprov-jdk15on-159.jar
        ``` 
    - Make it executable with `chmod +x bcprov-jdk15on-159.jar`

# Installation Instructions

- Clone this repository
    ```
    $ git clone https://github.com/edoardopuggioni/crowdpatching.git
    ```
- Compile _libsnark_
    - Enter the `libsnark` folder
        ```
        $ cd crowdpatching/zksnarks/libsnark
        ```
    - Create a _build_ directory and enter it
        ```
        $ mkdir build
        $ cd build
        ```
    - Execute _CMake_ before compiling
        ```
        $ cmake .. -DWITH_PROCPS=OFF
        ```
    - Finally compile all _libsnark_ source code
        ```
        $ make
        ```
- Compile _jsnark_
    - Enter the _jsnark_ circuits folder
        ```
        $ cd crowdpatching/zksnarks/JsnarkCircuitBuilder
        ```
    - Compile all _jsnark_ source code
        ```
        $ javac -d bin -cp /usr/share/java/junit4.jar:bcprov-jdk15on-159.jar  $(find ./src/* | grep ".java$")
        ```
# Execute the zk-SNARKs algorithms for the _CrowdPatching_ protocol


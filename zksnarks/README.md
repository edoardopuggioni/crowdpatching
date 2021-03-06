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
    - Create a new `bin` folder
        ```
        $ mkdir bin
        ```
    - Compile all _jsnark_ source code
        ```
        $ javac -d bin -cp /usr/share/java/junit4.jar:bcprov-jdk15on-159.jar  $(find ./src/* | grep ".java$")
        ```
# Execute zk-SNARKs algorithms for _CrowdPatching_

Instructions to run the three zk-SNARKs algorithms:
- Setup: generator G
- Prove: prover P
- Verify: verifier V

This guide assumes that both the _jsnark_ and _libsnark_ source files were compiled though the instructions above.

## Setup: generate proving and verifying key

Run the jsnark-side program for the generator G:
(Note that you need to indicate the option `g` as line argument)

```
$ cd crowdpatching/zksnark/JsnarkCircuitBuilder
$ java -cp bin examples.generators.CrowdPatchingCircuitGenerator g
```

This will produce (i) the keys PK and VK (exported into files with hard-coded names) and (ii) the .arith file, which represents the arithmetic circuit. The latter will be used by the libsnark-side program for the generator G.

Indeed, a new file called `crowdpatching_generator.arith` has been created in the JsnarkCircuitBuilder directory. Now we can run the libsnark-side program for the generator G, providing the .arith file as a command line argument:
(Note that you have to adapt this command depending on the location of the repository)

```
$ ~/<REPOSITORY PATH>/crowdpatching/zksnarks/libsnark/build/libsnark/jsnark_interface/generator crowdpatching_generator.arith
```

This will generate the proving key PK and the verifying key VK keys. They are exported into files with hard-coded names, `PK_export` and `VK_export`, in the current directory.

## Prove: generate proof using the proving key

Run the jsnark-side program for the prover P:
(Note that you need to indicate the option `p` as line argument)

```
$ java -cp bin examples.generators.CrowdPatchingCircuitGenerator p
```

This will create two new files
- Another .arith file called `crowdpatching_prover.arith` which happens to be identical to the one created by the generator but with a different name. We could avoid creating this file again and reuse the one generated by G, but we do it to represent the fact that P is independent of G, as it happens in a real-world scenario.
- A .in input file called `crowdpatching_prover.in`

Now we can run the libsnark-side program for the prover P. This will take both the .arith and .in files just created as command line arguments. Another input is the PK key file, but its name is hard-coded. Most importantly, the secret values and the public values (i.e. the non-secret values, the primary input for the NP statement) are hard-coded in the program.

```
$ ~/<REPOSITORY PATH>/crowdpatching/zksnarks/libsnark/build/libsnark/jsnark_interface/prover crowdpatching_prover.arith crowdpatching_prover.in
```

In the last step we generated the actual zk-SNARKs proof! This is stored in a new file called `proof_export`.

## Verify: verify proof using the verifying key

Finally, we can execute the verifier V. In this case, there is no jsnark-side program, but only the libsnark-side. This program takes no command line arguments, as the file names for the key VK and the proof are hard-coded. Most importantly, the public values (i.e. the non-secret values, the primary input for the NP statement) are hard-coded in this program.

```
$ ~/<REPOSITORY PATH>/crowdpatching/zksnarks/libsnark/build/libsnark/jsnark_interface/verifier
```
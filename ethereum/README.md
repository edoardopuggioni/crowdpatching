# Installation Instructions

- Install Node.js
    ```
    curl -sL https://deb.nodesource.com/setup_12.x | sudo -E bash -
    sudo apt-get install -y nodejs
    ```

- Install [Truffle](https://www.trufflesuite.com/truffle)
    ```
    npm install -g truffle
    ```
    This repository was tested with the following Truffle version:
    ```
    $ truffle version
    Truffle v5.1.48 (core: 5.1.48)
    Solidity v0.5.16 (solc-js)
    Node v12.19.0
    Web3.js v1.2.1
    ```
    To install that specific version:
    ```
    npm install -g truffle@v5.1.48
    ```

- Download Ganache

    Instead of downloading the program from the [main Ganache website](https://www.trufflesuite.com/ganache) go to the [official GitHub repository](https://github.com/trufflesuite/ganache/releases) to download a specific version of the program from the assets: this implementation was **tested with version 2.4.0**


# Build and deploy the smart contracts

## Run Ganache (Ethereum local blockchain)

- Open new terminal in directory where the Ganache AppImage was downloaded earlier

- Execute `chmod +x` command on the .AppImage file

- Execute the .AppImage file itself

- Select the `QUICKSTART` option

## Configure Truffle and Ganache

- This repository already includes a Truffle configuration file in `ethereum/truffle-config.js`, as well as a migration configuration files 

- Solidity smart contracts in this repository use compiler version 0.5.16

    - As a consequence, we select the same version in the Truffle configuration file:
        ```
        // Configure your compilers
        compilers: {
            solc: {
            version: "0.5.16", // Fetch exact version from solc-bin (default: truffle's version)
            }
        } 
        ```
    - To browse/modify the Solidity source files with Visual Studio Code:
    
        - Install the _solidity_ extension by Juan Blanco

        - Add the following setting to the `settings.json` file:
            ```
            "solidity.compileUsingRemoteVersion": "v0.5.16+commit.9c3226ce"
            ```
        
        - Otherwise VSCode will use the latest compiler and show errors

- Change port used by Ganache (from the GUI) to match the one used in `ethereum/truffle-config.js` for the `development` network, which is the default network used by Truffle

## Migrate (deploy on the blockchain) the SSC smart contract

- Open a new terminal in the `crowdpatching/ethereum` directory

- Execute the following command, which compiles the smart contracts before deploying them on the local blockchain provided by Ganache:
    ```
    $ truffle migrate
    ```

## Interact with the SCC (deploy DSCs and ESCs and more)

- Execute the provided Truffle script:

    ```
    truffle execute truffle-executable-script.js
    ```

    This will perform several tests

- Alternatively you can modify that script or create a new one
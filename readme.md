# ScroogeCoin Transaction Handler

## Project Overview

This project implements the `TxHandler` class that validates transactions and handles the processing of a series of transactions in the context of the **ScroogeCoin** cryptocurrency. The project consists of several tests to ensure the correctness of the transaction handling system, implemented using Java.

## Files

- **TxHandler.java**: The main class that implements the core logic for handling and validating transactions.
- **TestTxHandler.java**: The test suite that runs various test cases to validate the functionality of the `TxHandler` class.
- **Transaction.java**: A class that represents transactions, including inputs, outputs, and signatures.
- **UTXO.java** & **UTXOPool.java**: Classes that represent unspent transaction outputs and the pool of such outputs.
- **scroogeCoinGrader.jar**, **rsa.jar**, **algs4.jar**: JAR files containing the necessary libraries and grading framework.

## Prerequisites

To run this project, you'll need:

- **Java Development Kit (JDK)** installed on your system.
- Dependencies:
  - `scroogeCoinGrader.jar`
  - `rsa.jar`
  - `algs4.jar`

Ensure the above JAR files are available in your project folder or provide their full paths when compiling/running the code.

### Verify Java Installation

Ensure that Java is installed and configured correctly by running the following commands:

```bash
java -version
javac -version
```

## Compilation & Execution Guide

## Linux/Mac

### Step 1: Compile the Test File

Use the following command to compile `TestTxHandler.java`. Replace the semicolons (`;`) with colons (`:`) when specifying the classpath:

```bash
javac -cp scroogeCoinGrader.jar:rsa.jar:algs4.jar:. TestTxHandler.java

```

Step 2: Run the Test File

- After successful compilation, run the test suite using the following command:

```bash
java -cp scroogeCoinGrader.jar:rsa.jar:algs4.jar:. TestTxHandler
```

## Step 3: Expected Output

- If the TxHandler is correctly implemented, the output will show the results of the tests, similar to the following:

```bash
Running 7 total tests.
Test 1: test isValidTx() with valid transactions
==> passed
...
Total: 7/7 tests passed!

Running 8 total tests.
...
Total: 8/8 tests passed!
```

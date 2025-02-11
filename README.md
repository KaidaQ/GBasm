# GBasm
z80 cpu written via javascript.

A simple and modular z80 emulator written in java. The emulator aims to simulate the CPU behaviour of Zilog z80-like devices, by interpreting opcodes and executing them step by step.

# Features
* CPU Inits and states
* Memory handling
* Opcode handling
* Fetch-decode-executes
* Mem boundaries
* Debugging

Todo -
* fully emulate the Game Boy CPU

Planned Features -
* Full opcode set support
* Flag management
* Interupt Handling
* I/O handling
* Enhanced debugging feats.

# Getting Started
Firstly clone the repo.
git clone https://github.com/KaidaQ/GBasm

compile
javac -d bin src/*.java

then run
java main.Emulator

# License
This project is licensed under the MIT License - see the LICENSE file for details.



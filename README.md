# Java 6502 Emulator
 
This is a project I started because I wanted a place to write and test code for my Ben Eater 6502 kit. After seeing some other emulators written in C++ ~~(by sane people)~~, I tried downloading them but had trouble building them. So, I figured I would just write my own. It was a fun process, and was greatly helped by [OneLoneCoder's NES Emulator Tutorial](https://github.com/OneLoneCoder/olcNES). The LCD simulator was 100% me, and I'm proud of it.

UNIMPLEMENTED FEATURES:
 - DECIMAL MODE
 - LCD Memory (sorry, it's a character-level simulation)
 - LCD Reads (always returns not busy)
 - Every feature on the 65C22 that isn't writing to the LCD (will always read 0).
 - Interrupts (the opcodes and functions are all there, but irq() and nmi() are never called)

Some might ask, why write an emulator in Java? And I would respond: "Because no one else would." Sure, Java is terribly slow (more than 1000x slower than the original!), and the fact that Java's ```byte```s and ```short```s are a pain to work with because they're signed, but it's the language I'm best in so I don't care ;)

Feel free to fork it, improve it, whatever, just link back to here. Enjoy!

**The font isn't mine!**

##Tutorial

Controls:
   C - Toggle Clock
   Space - Pulse Clock
   H/J - Decrement/Increment RAM Page
   K/L - Decrement/Increment ROM Page
   R - Reset
   
You can load ```.bin``` files into RAM or ROM using the File Pickers in the top right.

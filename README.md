# Java System (6502) Emulator

** Dylans's Original Summary **
This is a project I started because I wanted a place to write and test code for my Ben Eater 6502 kit. After seeing some other emulators written in C++ ~~(by sane people)~~, I tried downloading them but had trouble building them. So, I figured I would just write my own. It was a fun process, and was greatly helped by [OneLoneCoder's NES Emulator Tutorial](https://github.com/OneLoneCoder/olcNES). The LCD simulator was 100% me, and I'm proud of it.

** Mike's Extensions **
This was a great project to find and I really appreciate what Dylan has done.  My need was to have flexible device configuration and easily mapped IO ports to model real reto hardware. The existing project construction provided an easy way to get what I wanted and allowed me to get there, quickly,  in workable stages.

ADDED FEATURES:
 - Flexible Bus Architecture for ease of modeling systems
 - Mapped IO
 - System can be defined in configuration
 - Many bus devices out of the box with changeable addresses
     - 40x25 Text Display with variable fonts
     - 640x480 256 Color Paged Graphics Mode
     - Banked RAM module, up to 256 banks with 64k each
     - Timer Device with IRQ
 	 

UNIMPLEMENTED FEATURES:
 - All 65c02-specific features (it is functionally a regular 6502 target when compiling code).
 - DECIMAL MODE
 - LCD Memory (sorry, it's a character-level simulation)
 - LCD Reads (always returns not busy)
 - Most features on the 65C22 (will always read 0).

Some might ask, why write an emulator in Java? And I would respond: "Because no one else would." Sure, Java is terribly slow (more than 1000x slower than the original!), and the fact that Java's ```byte```s and ```short```s are a pain to work with because they're signed, but it's the language I'm best in so I don't care ;)

Feel free to fork it, improve it, whatever, just link back to here. Enjoy!

**The font isn't mine!**

## Controls
- C - Toggle Clock
- Space - Pulse Clock
- H/J - Decrement/Increment RAM Page
- K/L - Decrement/Increment ROM Page
- R - Reset
- S - Toggle Slow Clock
- I - Trigger Interrupt (the CA1 pin on the VIA)
   
You can load ```.bin``` files into RAM or ROM using the File Pickers in the top right. It should be fully compatible with any binary compiled for the 6502 kit, except if it uses any unimplemented features. I might get to these sometime in the future. If I do, the repo will be updated.

## Environmental Variables
- SEMU_BIN_DIR
- SEMU_RAM_DIR
- SEMU_ROM_DIR
- SEMU_FONT_DIR
- SEMU_CFG_DIR

#

## Screenshots
![Screenshot 0](screenshots/screenshot0.png?raw=true)
![Screenshot 1](screenshots/screenshot1.png?raw=true)
![Screenshot 2](screenshots/screenshot2.png?raw=true)

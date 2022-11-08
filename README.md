# Java 6502 Emulator

## Update: v2.5: Keyboards are _SO_ hot right now.
Look at that another update so quickly! This update has a bunch of bug fixes like actually being able to save preference files and more accurate clock speed calculations. It also comes with a very simple keyboard interface, detailed below:

- The "Keyboard mode" button, activated with the mouse, toggles the keyboard between controlling the emulator and sending keystrokes to the CPU.
- When in typing mode, pressing a key on your computer's keyboard will write the <ins>**ASCII CODE**</ins> of that key to the memory location specified in the options menu (default is $3fff).
- Each key will also trigger <ins>**ONE (1)**</ins> interrupt to CA1.

I have chosen to implement the keyboard in this way for simplicity. Maybe one day I will implement a more realistic emulation of a PS/2 interface like the one from Ben's videos but this works pretty well for now. I was able to make a very simple shell/text editor using it.

Enjoy, and see you soon!
-Dylan

Note: when upgrading to a new version, your saved preferences from a previous version will not work anymore. Delete them and start anew.

## Update: v2.1 is here!
Whoa! Almost a year after the last update it's back and better than ever! The emulator now includes a GPU (with Ben's bitmap and a custom character mode) and SO MUCH customizability!
You can now hide and show the LCD and GPU windows with the buttons at the top right!
You can now customize the address ranges of the VIA and GPU!
The GPU's resolution and array size is _whatever you want it to be_!
The fonts actually work now!
The file choosers aren't the old Java ones!
And **COLORS!!**

Store the configs in our fancy new .pref files!
These configuration settings are stored in your AppData or Application Support folder on Windows/Mac respectively. It generates a defaults.pref file on startup every time if you want to go back to the default settings. (options.pref is loaded on startup.)

Enjoy the new features!
-Dylan

## Overview
This is a project I started because I wanted a place to write and test code for my Ben Eater 6502 kit. After seeing some other emulators written in C++ ~~(by sane people)~~, I tried downloading them but had trouble building them. So, I figured I would just write my own. It was a fun process, and was greatly helped by [OneLoneCoder's NES Emulator Tutorial](https://github.com/OneLoneCoder/olcNES). The LCD simulator was 100% me, and I'm proud of it.

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

## Screenshots
![Screenshot 0](screenshots/screenshot0.png?raw=true)
![Screenshot 1](screenshots/screenshot1.png?raw=true)
![Screenshot 2](screenshots/screenshot2.png?raw=true)
![Screenshot 3](screenshots/screenshot3.png?raw=true)

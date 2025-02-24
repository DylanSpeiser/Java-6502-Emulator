# Java 6502 Emulator

## Update: v2.11: Wozmon Works + Disassembler!
@lythd has once again added some great new features! For changes, see: https://github.com/DylanSpeiser/Java-6502-Emulator/pull/19#issue-2850599707

## Update: v2.10: Realistic Keyboard, Serial Emulation, and more command line args!
What an update! Thanks to lythd, the emulator now supports a realistic PS/2-like keyboard mode that can be enabled in the options pane. They've also added a serial emulator that can be turned on in a separate window. It's super cool!

Note that Wozmon is not running correctly through this serial interface yet, but we are working on the issue and will update when it's fixed.

We've also added some new command line arguments:
- `-windowWidth <int>` and `windowHeight <int>` do what they say on the tin, and should be a welcome change for those of you with differently sized monitors.
- `-f <path to ROM file>` will pre-load the ROM with the file specified. I finally got around to adding this one, and I hope you find it useful.

I'd also like to take this opportunity to note that this repo is showing its age. The code is an absolute mess since I wrote it back in high school, and I'd like to take some time to completely rewrite it from the ground up using actual OOP instead of the mess of magic numbers and duplicated function calls that builds the Swing UI.<br>
It will be a big undertaking, but seeing how popular this repo is getting I'm sure everyone would appreciate it.

(still) Coming soon!:
- More bug fixes, Wozmon
- Complete code refactor
- Other 65C02 instructions

## Update: v2.9: ACIA and Command line options!
Thanks to Steve Rubin, the emulator now supports ACIA communication through the command line! Like the VIA, the ACIA's address can be adjusted in the options menu.

Speaking of the command line, the first command line option has been added: `-verbose`. Use it to enable debug messages in the command line, which are now disabled by default.

That's it for this update. Remember to save new config files!

Coming soon:
- ROM preloading via a command line argument
- Implement all 65C02 instructions
- More bug fixes
- Code cleanup

## Update: v2.8: Bigger is better!
Added support for:
- 20x4 LCDs
- Reading LCD DDRAM Address (cursor position)
- Writing LCD DDRAM Address

Remember to save new config files!

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
 - DECIMAL MODE
 - LCD Character Memory (sorry, it's a character-level simulation)
 - Many features on the 65C22

Some might ask, why write an emulator in Java? And I would respond: "Because no one else would." Sure, Java is terribly slow (more than 1000x slower than the original!), and the fact that Java's ```byte```s and ```short```s are a pain to work with because they're signed, but it's the language I'm best in so I don't care ;)

Feel free to fork it, improve it, whatever, just link back to here. Enjoy!

**The font isn't mine!**

## How to Run:
If you download the JAR file from the releases page, you should be able to double-click it and run it. If, for some reason, that doesn't work, you can run it from the command line with:
```
java -jar <path to JAR file>
```

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

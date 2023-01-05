# Java System 6502/Z80 Emulator

** Dylans's Original Summary **
This is a project I started because I wanted a place to write and test code for my Ben Eater 6502 kit. After seeing some other emulators written in C++ ~~(by sane people)~~, I tried downloading them but had trouble building them. So, I figured I would just write my own. It was a fun process, and was greatly helped by [OneLoneCoder's NES Emulator Tutorial](https://github.com/OneLoneCoder/olcNES). The LCD simulator was 100% me, and I'm proud of it.

** Mike's Extensions **
This was a great project to find and I really appreciate what Dylan has done.  My need was to have flexible device configuration and easily mapped IO ports to model real reto hardware. The existing project construction provided an easy way to get what I wanted and allowed me to get there, quickly, in workable stages. This is still a work in progress and is buggy.  I would like to use this to do development debugging and gives flexibility to that end.

ADDED FEATURES:
 - CPU through-put increase (~3x)
 - Flexible Bus Architecture for ease of modeling systems
 - Mapped IO
 - System can be defined in configuration
 - Many bus devices out of the box with changeable addresses
     - 40x25 Text Display with variable fonts
	 - 80x25 Text Display with variable fonts
     - 640x480 256 Color Paged Graphics Mode
     - Banked RAM module, up to 256 banks with 64k each
     - Timer Device with IRQ
 - Included ROM and RAM demo binaries
 - New Font Resource Manager
 - New ROM Resource Manager
 - New System Configuration Loader
 - Extensible CPU model, currently 6502 and Z-80, with 68000 in works
 - Address Mapper
 - C64 Testing ability.  Loads the Kernal and BASIC, but not 100% functional
 - CC65/PASM integeration projects
 - System Configuration files 

Some might ask, why write an emulator in Java? And I would respond: "Because no one else would." Sure, Java is terribly slow (more than 1000x slower than the original!), and the fact that Java's ```byte```s and ```short```s are a pain to work with because they're signed, but it's the language I'm best in so I don't care ;)

Feel free to fork it, improve it, whatever, just link back to here. Enjoy!

## Commandline 
- --config configuration.system.file
    - Configuration file that describes the CPU and devices that make up the emulation 
- --project project.directory
    - Project directory that contains the source and development configuration files

Example:
  java -cp ./dist/JavaSystemEmulator.jar com.hadden.emulator.ui.MainSystemEmulator --project ./demo/projectA --config ./demo/Z80.system


## Controls
- C - Toggle Clock
- Space - Pulse Clock
- H/J - Decrement/Increment RAM Page
- K/L - Decrement/Increment ROM Page
- R - Reset
- P - Reset CPU
- S - Toggle Slow Clock
- I - Toggle Interrupt Disable
- Arrows Navigate
- CTL-E Enter/Exit Edit Mode
- CTL-D Enter/Exit Debug Mode
- Enter 
     - Edit Mode, selects and accepts value 
     - Debug Mode, selects break address
- Cursors(Wheel) Instruction Scroll History
- < & > Default Reset Address 
   
You can load ```.bin``` files into RAM or ROM using the File Pickers in the top right. It should be fully compatible with any binary compiled for the 6502 kit, except if it uses any unimplemented features. I might get to these sometime in the future. If I do, the repo will be updated.

## Environmental Variables
- SEMU_BIN_DIR
- SEMU_RAM_DIR
- SEMU_ROM_DIR
- SEMU_FONT_DIR
- SEMU_CFG_DIR

#

## Screenshots
![M6502 Emulating](screenshots/M6502.png?raw=true)

![Z80 Emulating](screenshots/z80ui.png?raw=true)

![Screenshot 0](screenshots/screenshot0.png?raw=true)
![Screenshot 1](screenshots/screenshot1.png?raw=true)
![Screenshot 2](screenshots/screenshot2.png?raw=true)
![Text Display](screenshots/display.png?raw=true)
![Text New Font Display](screenshots/font.png?raw=true)
![Graphics Display](screenshots/gfx.png?raw=true)
![Configuration](screenshots/cfg.png?raw=true)
![C64 test](screenshots/c64.png?raw=true)
![Z80 Emulating](screenshots/z80ui.png?raw=true)
![Edit Memory](screenshots/edit.png?raw=true)
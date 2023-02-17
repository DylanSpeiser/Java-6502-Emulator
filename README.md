# JUSE - Java Universal System Emulator

** We changed to JUSE! **

This was a great project to find and I really appreciate what Ben and Dylan has done.  
My need was to have flexible device configuration and easily mapped IO ports to model real reto hardware. 
The existing project construction provided an easy way to get what I wanted and allowed me to get there, quickly, in workable stages.
I have started by decoupling things so now you can expand and use it by making your own CPUs, devices and UIs.   
As before, this is still a work in progress and may discover some bugs.  
I would like to use this to do development debugging and gives flexibility to that end.

** NEW SOURCE IS UNDER /java **

FEATURES:
 - Architecture is modularized
 - Flexible Bus Architecture for ease of modeling systems
 - Better cross-platform UI:  Windows, MacOS and Linux should all behave.
 - CPU Assortment, 6502, Z80 and more to come
 - BYOD - Bring your own devices
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
- --demo:
	- Extracts the demonstration projects, resources, extensions and ROM images to the 'demo' directory.

- --ext:
	- Specifies a directory to use for additional extensions to the emulator.
	- The directory, and any sub-directories, are scanned for all jars to be added.

- --ui:
	- Specifies an optional UI class to override the default GUI

- --help:
	- Provides this informational message.

- --project:
	- Specifies a project associated with the emulator to build and run code.

- --config:
	- Specifies a configuration for the emulator to use.



Example:
  java -jar JUSE.jar --demo --ext demo/juse/ext --config demo/Z80A.system
  
  - This extracts the demo artifacts, set the extension directory into it and starts up a Z80 system with the awful green UI (green is for illustrative purposes).


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
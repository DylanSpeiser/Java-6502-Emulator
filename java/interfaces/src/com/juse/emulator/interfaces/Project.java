package com.juse.emulator.interfaces;

import java.io.File;

public interface Project
{
	File[] getFiles();
	int compile();
	int link();
}

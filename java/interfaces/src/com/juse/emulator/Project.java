package com.juse.emulator;

import java.io.File;

public interface Project
{
	File[] getFiles();
	int compile();
	int link();
}

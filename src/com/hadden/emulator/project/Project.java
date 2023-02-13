package com.juse.emulator.project;

import java.io.File;

public interface Project
{
	File[] getFiles();
	int compile();
	int link();
}

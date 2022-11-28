package com.hadden.emulator.project;

public interface Project
{
	String[] getFiles();
	int compile();
	int link();
}

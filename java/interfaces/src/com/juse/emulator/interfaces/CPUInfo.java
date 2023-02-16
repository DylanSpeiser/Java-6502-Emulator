package com.juse.emulator.interfaces;

public interface CPUInfo
{
	IOSize getAddressableSize();
	IOSize getDataSize();
	boolean isOddAlignmentValid(IOSize size);
}

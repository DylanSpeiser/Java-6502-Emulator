package com.juse.emulator;

public interface CPUInfo
{
	IOSize getAddressableSize();
	IOSize getDataSize();
	boolean isOddAlignmentValid(IOSize size);
}

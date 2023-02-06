package com.hadden.emu;

public interface CPUInfo
{
	IOSize getAddressableSize();
	IOSize getDataSize();
	boolean isOddAlignmentValid(IOSize size);
}

package com.hadden.emu;

public interface AddressMap
{
	public void setDefaultDevice(BusDevice bd);
	public void setIRQHandler(BusIRQ busIRQ);
	public AddressMap addBusDevice(BusDevice bd);
	public BusDevice getMemoryMappedDevice(int address);
	public void printAddressMap();
	public void setBusListener(BusListener listener);
}

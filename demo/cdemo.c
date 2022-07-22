
// cc65 --cpu 6502 cdemo.c
// ca65 cdemo.s -D BASE=0x0000
// cl65 cdemo.o --target none --start-addr 0x0000 -o cdemo.bin
void main(void)
{
	int i = 0;

	while(1)
	{
		((char*)(0xA000))[0] = 65;
		for(i=0;i<10;i++)
			((char*)(0xA000))[1] = 66;
	//	((char*)(0xA000))[1] = 66;
	}
	return;
}

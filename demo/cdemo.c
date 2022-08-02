
// cc65 --cpu 6502 cdemo.c
// ca65 cdemo.s -D BASE=0x0000
// cl65 cdemo.o --target none --start-addr 0x0000 -o cdemo.bin
void main(void)
{
	unsigned int i = 2;
	char far *p = (char far *)(0xA000);
	
	((char far*)p)[0] = 'A';
	*((char far*)0xA001) = 'B';
	*((char far*)0xA002) = 'C';
	*((char far*)0xA003) = 'D';
	*((char far*)(0xA003 + i)) = 'F';

	for(i=0;i<10;i++)
	{
		*((char far*)(0xA005 + i)) = 'X';
	}


	while(1)
	{
	};
	
	/*
	char c = 70;
	//char* p0 = ((char*)(0xA000));
	//char* p1 = ((char*)(0xA001));
	//char* p2 = ((char*)(0xA002));

	((char*)(0xA000))[0]  = 65;
	((char*)(0xA001))[0]  = 66;
	((char*)(0xA002))[0]  = 67;

	((char*)(0xA003 + i++))[0]  = 68;

	//((char*)(0xA003 + i))[0]  = 69;

	//((char*)(0xA003 + i))[0]  = 70;

	//while(i<5)
		((char*)(0xA003 + (i++)))[0] = 78;
	//((char*)((int)0xA000 + i))[0]  = 65;
	//i++;
	//((char*)((int)0xA000 + i))[0]  = 66;

	while(1);
	*/
	return;
}

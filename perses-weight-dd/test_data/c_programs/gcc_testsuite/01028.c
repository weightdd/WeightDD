


char acDummy[0xf0] __attribute__ ((__BELOW100__));
unsigned short B100 __attribute__ ((__BELOW100__)) = 0x1234;
unsigned short *p = &B100;

void
Do (void)
{
  B100 |= 0x0080;
}

int
main (void)
{
  Do ();
  return (*p == 0x12b4) ? 0 : 1;
}

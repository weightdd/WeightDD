


char acDummy[0xf0] __attribute__ ((__BELOW100__));
unsigned char B100 __attribute__ ((__BELOW100__)) = 0xcb;
unsigned char *p = &B100;

void
Do (void)
{
  B100 &= ~0x01;
}

int
main (void)
{
  Do ();
  return (*p == 0xca) ? 0 : 1;
}

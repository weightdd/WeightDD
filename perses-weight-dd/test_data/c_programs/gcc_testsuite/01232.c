






long __attribute__((target("thumb")))
foo (char *s, long size, int unsigned_p)
{
  long x;
  unsigned char *p = (unsigned char *) s;
  switch (size)
    {
    case 4:
      x = ((long) p[3] << 24) | ((long) p[2] << 16) | (p[1] << 8) | p[0];
      return x;
    }
}

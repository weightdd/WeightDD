



int g;
void bar();
void blah(int);

int foo (int n, int l, int m, int r)
{
  int v;

  if (n)
    if (l)
      v = r;

  if (m) g++;
  else bar();

  if ( n && l)
      blah(v);

  if (l)
    if (n)
      blah(v);

  return 0;
}

int foo_2 (int n, int l, int m, int r)
{
  int v;

  if (n)
    if (l)
      v = r;

  if (m) g++;
  else bar();

  if (n || l)
      blah (v);

  return 0;
}

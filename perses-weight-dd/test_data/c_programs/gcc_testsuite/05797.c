


typedef struct { int i; } FILE;
FILE *fp;
extern int __fprintf_chk (FILE *, int, const char *, ...);
volatile int vi0, vi1, vi2, vi3, vi4, vi5, vi6, vi7, vi8, vi9;

void test (void)
{
  vi0 = 0;
  __fprintf_chk (fp, 1, "hello");
  vi1 = 0;
  __fprintf_chk (fp, 1, "hello\n");
  vi2 = 0;
  __fprintf_chk (fp, 1, "a");
  vi3 = 0;
  __fprintf_chk (fp, 1, "");
  vi4 = 0;
  __fprintf_chk (fp, 1, "%s", "hello");
  vi5 = 0;
  __fprintf_chk (fp, 1, "%s", "hello\n");
  vi6 = 0;
  __fprintf_chk (fp, 1, "%s", "a");
  vi7 = 0;
  __fprintf_chk (fp, 1, "%c", 'x');
  vi8 = 0;
  __fprintf_chk (fp, 1, "%d%d", vi0, vi1);
  vi9 = 0;
}

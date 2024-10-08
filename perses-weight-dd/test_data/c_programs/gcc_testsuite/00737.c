







struct z
{
  double x[4];
};

struct z a = { 5.0, 6.0, 7.0, 8.0 };
struct z b = { 9.0, 10.0, 11.0, 12.0 };


enum aapcs64_test_kind
{
  TK_PARAM = 0,
  TK_VA_ARG,
  TK_RETURN
};

int which_kind_of_test;

extern int printf (const char*, ...);
extern void abort (void);
extern void dumpregs () __asm("myfunc");
enum structure_type
{
  flat = 0,
  i32in128,
  f32in64,
  i8in64,
  i16in64,
  i32in64,
};



typedef long unsigned int size_t;
int memcmp (void *, void *, size_t);



const int element_size[] = { 1, 4, 4, 1, 2, 4 };
const int block_reverse_size[] = { 1, 16, 8, 8, 8, 8 };

int
validate_memory (void *mem1, char *mem2, size_t size, enum structure_type type)
{
  unsigned int i;
  char *cmem1 = (char *) mem1;
  switch (type)
    {
    case f32in64:
    case i32in128:
    default:
      break;
    }
  return memcmp (mem1, mem2, size);
}
void testfunc(char* stack)
{


  { struct z __x = a; ; if (validate_memory (&__x, stack + 0, sizeof (struct z), flat) != 0) abort(); }
  { struct z __x = b; ; if (validate_memory (&__x, stack + 32, sizeof (struct z), flat) != 0) abort(); }
  { double __x = 0.5; ; if (validate_memory (&__x, stack + 336, sizeof (double), flat) != 0) abort(); }
  { int __x = 7; ; if (validate_memory (&__x, stack + 96, sizeof (int), flat) != 0) abort(); }
  { int __x = 8; ; if (validate_memory (&__x, stack + 100, sizeof (int), flat) != 0) abort(); }

  return;
}






void myfunc(


  struct z,
  struct z,
  double,
  int,
  int

) ;
int main()
{
  which_kind_of_test = TK_PARAM;
  myfunc(






  a,
  b,
  0.5,
  7,
  8

);
  return 0;
}


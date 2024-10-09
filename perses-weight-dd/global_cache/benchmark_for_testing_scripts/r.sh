#!/usr/bin/env bash

rm a.out temp.txt &> /dev/null

# Check the program does not have cerntain errors.
if ! gcc -Wall -Wextra t.c &> temp.txt ; then
  exit 1
fi

if command -v clang &> /dev/null
then
  if ! clang -Weverything t.c >> temp.txt 2>&1 ; then
    exit 1
  fi
else
#  since there is no clang in the docker.
  if ! clang-trunk -Weverything t.c >> temp.txt 2>&1 ; then
    exit 1
  fi
fi

if grep -q "Wimplicit-int" temp.txt || \
   grep -q "defaulting to type" temp.txt || \
   grep -q "too few arguments" temp.txt ; then
  exit 1
fi
# End of the check.

./a.out > temp.txt

if grep -q 'world' temp.txt ; then
  exit 0
fi

exit 1

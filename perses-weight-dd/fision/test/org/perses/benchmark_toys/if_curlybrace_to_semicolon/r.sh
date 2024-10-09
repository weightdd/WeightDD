#!/usr/bin/env bash

set -o pipefail
set -o nounset
set -o xtrace

readonly FILE="t.c"
readonly OUTPUT="temp.txt"
readonly EXE="./temp.out"

gcc -Wall -Wextra "${FILE}" -o "${EXE}" > "${OUTPUT}" 2>&1 || exit 1
"${EXE}" >> "${OUTPUT}" 2>&1
if [[ "$?" != 99 ]] ; then
  exit 1
fi

if grep -q "Wimplicit-int" "${OUTPUT}" || \
   grep -q "defaulting to type" "${OUTPUT}" || \
   grep -q "uninitialized" "${OUTPUT}" || \
   grep -q "Wmain-return-type" "${OUTPUT}" || \
   grep -q "Wimplicit-function-declaration" "${OUTPUT}" || \
   grep -q "Wincompatible-library-redeclaration" "${OUTPUT}" || \
   grep -q "too few arguments" "${OUTPUT}" ; then
  exit 1
fi

readonly OCCURRENCE=3
if [[ "$(grep -c "var" "${FILE}")" != "${OCCURRENCE}" ]] ; then
  exit 1
fi

if grep -c "if (100)" "${FILE}" ; then
  exit 1
fi

exit 0


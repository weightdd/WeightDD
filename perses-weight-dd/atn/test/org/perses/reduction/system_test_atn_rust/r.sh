#!/usr/bin/env bash

set -o nounset

readonly BIN="temp_a.out"
rm "${BIN}" temp.txt &> /dev/null
trap "rm ${BIN} temp.txt" EXIT

if ! rustc t.rs -o "${BIN}" ; then
  exit 1
fi

"./${BIN}" > temp.txt

if grep -q 'raw' temp.txt ; then
  exit 0
fi

exit 1

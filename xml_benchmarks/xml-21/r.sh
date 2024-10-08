#! /bin/bash

QUERY='declare namespace oD ="AhGBUjZOWYyiF";declare namespace qS ="HzBUDWUjpyEDSPQ";declare namespace nfw ="LJdwWr";declare namespace Hs ="AhGBUjZOWYyiF";declare namespace z ="HzBUDWUjpyEDSPQ";declare namespace CooQr ="ZkUbXTeSKZkQYoezodZ";declare namespace yY ="LJdwWr";declare namespace LHIn ="wCkrH";declare namespace gJhWW ="FKPcDafWopwZyDHHyU";//*[boolean(. ! .)]/following-sibling::*[not(((subsequence(has-children(), 3) = ()) or true()) and false()) and not(boolean(./(/R12/A14/G14/D10/T4/G14/R19/F3/K27)/parent::nfw:U13[. = <A>2</A>]))]/ancestor::*/child::z:I21[((boolean(subsequence(., 2, 42)) != false()) != true()) != false() and (not(boolean(tail(subsequence(./*, 22)))) and boolean(position() + 676432593))]//*[not(head(subsequence(boolean(./parent::*), 53))) and boolean(./ancestor-or-self::*//*)]/ancestor-or-self::*'
GOOD_VERSION="1e9bc83"
BAD_VERSION="816b386"

echo $QUERY > query.xq

# run saxon
target_saxon="saxon"
java -cp '/home/coq/cdd/benchmarks/xmlprocessorbugs/lib/saxon-he-12.4.jar:/home/coq/cdd/benchmarks/xmlprocessorbugs/lib/xmlresolver-5.2.0/lib/*' net.sf.saxon.Query -s:./input.xml -q:./query.xq > ${target_saxon}_raw_result.xml 2>&1
ret=$?

if [ $ret != 0 ]; then
  exit 1
fi

# run basex_bad
target_basex_bad="basex_bad"
java -cp "/home/coq/cdd/benchmarks/xmlprocessorbugs/lib/basex-${BAD_VERSION}.jar" org.basex.BaseX -i input.xml query.xq > ${target_basex_bad}_raw_result.xml 2>&1
ret=$?

if [ $ret != 0 ]; then
  exit 1
fi

# run basex_good
target_basex_good="basex_good"
java -cp "/home/coq/cdd/benchmarks/xmlprocessorbugs/lib/basex-${GOOD_VERSION}.jar" org.basex.BaseX -i input.xml query.xq > ${target_basex_good}_raw_result.xml 2>&1
ret=$?

if [ $ret != 0 ]; then
  exit 1
fi

# process saxon result
grep -o 'id="[^"]*"' ${target_saxon}_raw_result.xml | sed 's/id="//g' | sed 's/"//g' | grep -v '^[[:space:]]*$' > ${target_saxon}_processed_result.txt

# process basex_bad result
grep -o 'id="[^"]*"' ${target_basex_bad}_raw_result.xml | sed 's/id="//g' | sed 's/"//g' | grep -v '^[[:space:]]*$' > ${target_basex_bad}_processed_result.txt

# process basex_good result
grep -o 'id="[^"]*"' ${target_basex_good}_raw_result.xml | sed 's/id="//g' | sed 's/"//g' | grep -v '^[[:space:]]*$' > ${target_basex_good}_processed_result.txt


# diff, files should be different
if diff ${target_saxon}_processed_result.txt ${target_basex_bad}_processed_result.txt > /dev/null 2>&1; then
    exit 1
fi

# diff, files should be same
if ! diff ${target_saxon}_processed_result.txt ${target_basex_good}_processed_result.txt > /dev/null 2>&1; then
    exit 1
fi

exit 0



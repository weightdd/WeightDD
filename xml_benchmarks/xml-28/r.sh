#! /bin/bash

QUERY='declare namespace Aa ="saEqczELwiDr";declare namespace eTA ="NrJTEVj";declare namespace A ="xBgwTrJDHDdbRMXBx";declare namespace ycMC ="iFMLJyyqIYnZTtwzRjQf";declare namespace feMuH ="WyfHLLUDdrTuJjNxgxH";declare namespace VrN ="X";declare namespace CrEt ="iF";declare namespace EU ="fLxguhNPLNhmUoaCLj";declare namespace yAcX ="QcMEgKoMhEpBY";declare namespace CIIz ="NrJTEVj";declare namespace s ="xBgwTrJDHDdbRMXBx";declare namespace ibfTi ="saEqczELwiDr";//*/ancestor::*[reverse(boolean(reverse(B29))) or not(boolean(normalize-space(@e9)))]/CIIz:G5/preceding::VrN:U5[not((boolean(tail(./(/U13/S11/E16/F5/T6/H7/U5))) and false()) ! head(.))]/parent::*[(boolean(last()) or ((head(head(head(head(subsequence(., 2))/node-name()))) ! .) castable as xs:duration)) or (reverse(boolean(.)) = false()) = false()]/Aa:T31[boolean((node-name() ! count(subsequence(., 90))) + -299134339) and (boolean(last()) and not(boolean(subsequence(tail(./preceding-sibling::Aa:E3), 2))))]/parent::VrN:H7[boolean(./*//*[not(boolean(O31))])]/*'
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



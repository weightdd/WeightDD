#! /bin/bash

QUERY='declare namespace XcVhp ="MDZqNWkphbnK";declare namespace xL ="eNjcLqxrGSZ";declare namespace Rjd ="MDZqNWkphbnK";declare namespace Bf ="ICGXMn";declare namespace XRXV ="xDepjA";declare namespace JbfIA ="wYJeJxRGCwLQnbKjReR";declare namespace ISVt ="YxDv";declare namespace I ="dHkFqPPU";//*[(reverse(boolean(reverse((. = <A>2</A>) != true())) ! ((. or false()) or false(),(. ! (. castable as xs:integer)) castable as xs:string)) = () and boolean(count(subsequence(reverse(./descendant-or-self::*/(/X8/C17/N11/O5/H20/H9/S12/B24)) = (), 5)))) or boolean(last() mod -9335984661) or true()]/preceding-sibling::JbfIA:W13/ancestor::W2[(./descendant::xL:Z16[not(boolean(count(subsequence(., 4, 52))))] = () or boolean(. ! .)) and (head(.) = <A>2</A> or not(head(head(node-name() ! .)) castable as xs:duration))]/JbfIA:W13[boolean(./descendant::*//L7) or not(count(boolean(.)) <= 1411478101)]//*'
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



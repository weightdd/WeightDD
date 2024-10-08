#! /bin/bash

QUERY='declare namespace ciEJU ="EsZLbBUbSMkZrB";declare namespace lDXQt ="EsZLbBUbSMkZrB";declare namespace F ="rbOVGyba";declare namespace b ="bTmsLz";declare namespace TfCCZ ="rzCUqyw";declare namespace z ="BRt";declare namespace OqDbe ="rB";//*[not(boolean(abs(min(sum(boolean(.) ! (subsequence(., 66) ! count(.)))))))]//*[(boolean(tail(subsequence(./(/E21/L18/L9/C10/V26), 40, 33))) or (count(name() <= "VOhlq8") < 974223853 or boolean(head(subsequence(position(), 2)) < 1936496908))) and not((./ancestor::*/descendant::W3 = ()) or false())]/preceding::OqDbe:X11/following::*[boolean(name()) or ((boolean(subsequence(reverse(.//F:K7), 44)) and boolean(math:cos(max(count(.))))) and boolean(last()))]/descendant-or-self::b:Y17/following::*[boolean(.//*/*)]//*[((boolean(./(K7)/OqDbe:I8[. ! (local-name() != "fQ)iwH,%L um")]) and boolean(subsequence(./*[not(starts-with(local-name(), "*k:zY! "))], 24))) and subsequence(., 44) = ()) or not(head(subsequence(./*/preceding::*[boolean(name())] = (), 6)))]'
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



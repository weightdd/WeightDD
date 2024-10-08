#! /bin/bash

QUERY='declare namespace IGfWU ="UARJTpzmNcrQdLrr";declare namespace F ="QulWIkvQY";declare namespace DwBA ="QulWIkvQY";declare namespace l ="QulWIkvQY";declare namespace o ="UARJTpzmNcrQdLrr";declare namespace XIPOL ="m";//*[boolean(./*/(W1))]/*//*[not(. = <A>2</A>) or ((boolean(avg(count(tail(./(/Z10/X2/D18/Y8/S7/T3/C23/H2)[boolean(reverse(.))]/(/Z10/F13/T11/T13/H7/T10/O5/C8,/Z10/U16/L1/U9/R4/Z5/N13/M5/S11/S2/Y16/D34))))) and boolean(count(tail(./(/Z10/X2/D18/Y8/S7/T3/C23/H2)[boolean(reverse(head(.)))]/(U5,/Z10/B20/S6/P8/B19/Z3/E8/B13/C10)[name() = "e"]) = ()))) or boolean(count(tail(subsequence(. ! (local-name(),boolean(@e16)), 28)))))]/o:R20[boolean(./descendant-or-self::*/preceding-sibling::*[boolean(position())])]/ancestor::G2[boolean(math:cos(round(avg(math:cos(position()))))) and not(count(not(boolean(.))) = 103666399)]/(/Z10/X2/D18/Y8/S7/T3/C2/S7/G2,F28)'
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



#!/usr/bin/env bash

############################################################
# Usage
# $0 <test_script> <input_file> <database_name>
#
# use the generated 'wrapped_<test_script>' as the test
# script to reducers.
############################################################

set -o pipefail
set -o nounset
set -o errexit


readonly TEST_SCRIPT=$(realpath "$1")
readonly SOURCE_FILE=$2
readonly DATABASE=$(realpath "$3")
readonly SCRIPT_NAME=$(basename "${TEST_SCRIPT}")
readonly SOURCE_NAME=$(basename "${SOURCE_FILE}")

# sanity test
bash "$SCRIPT_NAME"
readonly RET_CODE=$?
[[ -z "$RET_CODE" ]] && echo "unexpected. no return code from script test." && exit 119
[[ "$RET_CODE" -ne 0 ]] && echo "sanity test failed. exit code not equal to zero" && exit 120

# init/find database
sqlite3 "$DATABASE"  "CREATE TABLE IF NOT EXISTS GlobalCache (digest VARCHAR(70) NOT NULL PRIMARY KEY, ret INTEGER);"

# generate wrapper file
cat <<EOT >>"wrapped_$SCRIPT_NAME"
#!/usr/bin/env bash

set -o nounset

readonly DATABASE=$DATABASE
readonly SCRIPT_NAME=$SCRIPT_NAME
readonly SOURCE_NAME=$SOURCE_NAME
readonly ABS_SCRIPT_PATH=$TEST_SCRIPT

# 0. prerequisite
[[ ! -f "\${DATABASE}" ]] && echo "unexpected. no database found" && exit 110
[[ ! -f "\${SCRIPT_NAME}" ]] && echo "copying over script to the reduction folder" && cp \$ABS_SCRIPT_PATH ./

# 1. query database, if found -> return result, else -> step 2
readonly DIGEST=\$(sha256sum "\${SOURCE_NAME}" | cut -c -64)
readonly RESULT=\$(sqlite3 "\$DATABASE"  "SELECT ret FROM GlobalCache WHERE \"digest\" = \"\${DIGEST}\"")
[[ -n "\${RESULT}" ]] && exit "\${RESULT}"

# 2. call script
bash "\${SCRIPT_NAME}"
readonly RET_CODE=\$?
[[ -z "\${RET_CODE}" ]] && echo "unexpected. no return code from script test." && exit 119

# 3. save to sqlite database, return exit code
sqlite3 "\$DATABASE" "INSERT INTO GlobalCache (digest, ret) VALUES(\"\${DIGEST}\", \${RET_CODE});"
exit "\${RET_CODE}"
EOT

chmod +x "wrapped_$SCRIPT_NAME"
echo "all set. wrapped_$SCRIPT_NAME is ready."

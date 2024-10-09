#!/usr/bin/env bash

readonly SQL="small.virtuoso_sql"

# remove the old one
docker container rm virtdb_test -f
# start virtuoso through docker
docker run --name virtdb_test -itd --env DBA_PASSWORD=dba openlink/virtuoso-opensource-7:7.2.9
# wait the server starting
sleep 10
# check whether the simple query works
echo "SELECT 1;" | docker exec -i virtdb_test isql 1111 dba
# run the poc
docker exec -i virtdb_test isql 1111 dba < "${SQL}"

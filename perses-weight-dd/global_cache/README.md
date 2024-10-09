# Program Reduction Global Property Result Database Tool

This utility tool speeds up multiple runs of benchmarking by saving
property results to a global database. The tool generates a wrapped 
property test file by adding a layer. This additional layer first queries
the database for a result before it calls the original property test.

## Database

sqlite3 (simple command-line sqlite tool)

Table GlobalCache looks like:

```markdown
|                              digest                              | ret |
|------------------------------------------------------------------|-----|
| feebf4d2c365cc1f53c2d2dc97b75b6f38aa63197901af1be2928a02461594cb | 0   |
| 7e5a02f21a733d5493ac4df70ce8be9654dd6c684670c0cfeaa9f6b9b49b8167 | 1   |
| 71b2a2109c7614eed01d5849ebfaf4fc0d80fcc7135431c4b37a66f4c9a414fa | 1   |
```

## Workflow

Inputs: `<test_script> <input_file> <database_name>`

1. sanity test
2. init database (create database and table if not found)
3. generate executable wrapper file `wrapped_r.sh`

`wrapped_r.sh` works as follow:

1. prerequisite
    1. check database path
    2. check original `[r.sh](http://r.sh)` in current tmp folder; copy over if not found
2. hash `t.c` with `sha256sum`, query database

   if found → return the query result

   else → execute script; insert to database; return exit code


## TODO
- A thread-safe version is desirable for parallelism.

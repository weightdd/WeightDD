# Artifact for "WDD: Weighted Delta Debugging"

## Introduction

Thank you for evaluating this artifact!

To evaluate this artifact, a Linux machine with [docker](https://docs.docker.com/get-docker/) installed is needed.

## List of Claims Supported by the Artifact

- WDD introduce the concept of weight to the classical delta debugging algorithms, supporting more rational partitioning strategy during delta debugging.
- Wddmin and WProbDD, the implementations of WDD in ddmin and ProbDD, outperform ddmin and ProbDD in both efficiecny and effectiveness in tree-based test input minimization techniques HDD and Perses.

## Notes

- All the experiments take long time to finish, so it is recommended to use tools like screen and tmux to manage sessions if the experiments are run on remote server.

## Docker Environment Setup

1. If docker is not installed, install it by following the [instructions](https://docs.docker.com/get-docker/).

2. Install the docker image.

   ```shell
   docker pull
   ```

3. Start a container

   ```shell
   docker container run --cap-add SYS_PTRACE --interactive --tty [xxx] /bin/bash
   
   ```



## Benchmark Suites

Under the root directory of the porject, the benchmarks are located:

- `./c_benchmarks`: benchmark-C consists of 30 C programs;
- `./xml_benchmarks`: benchmark-XML consists of 30 XML files.



## Build the Tools

We have implemented all the related algorithms in this paper based on [Perses](https://github.com/uw-pluverse/perses). Our implemenation locates

in `./perses-weight-dd`. Specifically:

- $W_{ddmin}$ is implemented in:

   `./perses-weight-dd/src/org/perses/delta/WeightSplitDeltaDebugger.kt`

- $W_{ProbDD}$ is implemented in:

  `./perses-weight-dd/src/org/perses/delta/WeightedPristineProbabilisticDeltaDebugger.kt`

- The baseline algorithms ddmin and ProbDD, and HDD are located in:

  ```
  // ddmin
  ./perses-weight-dd/src/org/perses/delta/PristineDeltaDebugger.kt
  // ProbDD
  ./perses-weight-dd/src/org/perses/delta/PristineProbabilisticDeltaDebugger.kt
  // HDD
  ./perses-weight-dd/src/org/perses/reduction/reducer/hdd/
  ```

To run the evaluation, we first need to build `perses-weight-dd` from the source according to the [document](https://github.com/uw-pluverse/perses?tab=readme-ov-file#obtain-and-run) of Perses, and put the JAR file under the `/tmp/binaries/` directory in docker.

Note: for convince, we have pre-built the tools and put them under `/tmp/binaries/` in the docker image.

```shell
> ls /tmp/binaries/
perses_deploy.jar  token_counter_deploy.jar
```

Both JAR files are **required** to run the evaluation.



## Reproduce RQ1



## Reproduce RQ2



## Reproduce RQ3


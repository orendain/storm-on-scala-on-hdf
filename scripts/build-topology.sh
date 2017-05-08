#!/usr/bin/env bash

projectDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

#sbt commonsJVM/compile
sbt stormTopology/assembly

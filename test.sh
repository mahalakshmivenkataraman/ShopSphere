#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p target/classes target/test-classes
find src/main/java -name '*.java' -print > target/sources.txt
javac -encoding UTF-8 -d target/classes @target/sources.txt
find src/test/java -name '*Runner.java' -print > target/test-sources.txt
javac -encoding UTF-8 -cp target/classes -d target/test-classes @target/test-sources.txt
java -cp target/classes:target/test-classes ecommerce.test.TestRunner

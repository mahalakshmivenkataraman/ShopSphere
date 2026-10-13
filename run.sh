#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p target/classes
find src/main/java -name '*.java' -print > target/sources.txt
javac -encoding UTF-8 -d target/classes @target/sources.txt
java -cp target/classes ecommerce.main.Main "$@"

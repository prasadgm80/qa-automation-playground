#!/usr/bin/env bash
# One-command test runner for macOS/Linux.
# Usage:  ./run_tests.sh               -> run all tests
#         ./run_tests.sh -m smoke      -> any pytest arguments are passed through
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -x .venv/bin/python ]; then
  echo "Creating virtual environment..."
  python3 -m venv .venv
fi

echo "Installing dependencies..."
.venv/bin/python -m pip install --quiet --disable-pip-version-check -r requirements.txt

exec .venv/bin/python -m pytest "$@"

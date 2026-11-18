# One-command test runner for Windows.
# Usage:  .\run_tests.ps1                 -> run all tests
#         .\run_tests.ps1 -m smoke        -> any pytest arguments are passed through
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$python = ".\.venv\Scripts\python.exe"
if (-not (Test-Path $python)) {
    Write-Host "Creating virtual environment..."
    python -m venv .venv
}

Write-Host "Installing dependencies..."
& $python -m pip install --quiet --disable-pip-version-check -r requirements.txt

& $python -m pytest @args
exit $LASTEXITCODE

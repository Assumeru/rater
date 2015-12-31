@echo off
setlocal enabledelayedexpansion
set files=

for %%i in (.\files\categories\*) do (
	set files=!files! -f "%%i"
)

java -jar rater.jar -a ".\files\absolute.txt" !files! -i .\files\weights.txt -c -t 10 1> .\files\grades.txt
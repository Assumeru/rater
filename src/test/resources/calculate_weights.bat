@echo off
setlocal enabledelayedexpansion
set files=

for %%i in (.\files\categories\*) do (
	set files=!files! -f "%%i"
)

java -jar rater.jar -a ".\files\absolute.txt" !files! -w 1> .\files\weights.txt
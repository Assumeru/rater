@echo off

set /p mu= mean? 
set /p sigma= standard deviation?
set /p minmax= min max?  

java -jar rater.jar -a .\files\grades.txt -d -t 0 -m %mu% -s %sigma% -r %minmax% 1> .\files\normal.txt
@echo off

"%~dp0\terminal.bat" -memory 512m -main adams.terminal.Main -title ADAMS-Incubator -env-modifier adams.core.management.WekaHomeEnvironmentModifier

@echo off

"%~dp0\terminal.bat" -memory 2g -main adams.terminal.Main -title ADAMS-Spectral -env-modifier adams.core.management.WekaHomeEnvironmentModifier

@echo off
cd /d %~dp0
echo Starting H2 Database Web Console...
echo When the browser opens, ensure your JDBC URL is:
echo jdbc:h2:file:./data/college_fallback;MODE=PostgreSQL;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE
echo.
echo Username: sa
echo Password: [Leave Blank]
echo.

mvn exec:java "-Dexec.mainClass=org.h2.tools.Console" "-Dexec.classpathScope=runtime" "-Dexec.args=-web -browser"

pause

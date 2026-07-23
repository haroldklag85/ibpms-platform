$env:MAVEN_OPTS = "-Xmx2048m -Dfile.encoding=UTF-8"
& "..\maven\apache-maven-3.9.6\bin\mvn.cmd" test '-pl' 'ibpms-core' '-Dproject.build.sourceEncoding=UTF-8' '-Dfile.encoding=UTF-8'

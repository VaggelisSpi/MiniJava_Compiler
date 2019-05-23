all: clean compile

compile:
	mkdir outputs
	java -jar ./jtb132di.jar -te miniJava.jj
	java -jar ./javacc5.jar miniJava-jtb.jj
	javac Main.java InformationStoringVisitor.java TypeCheckVisitor.java IR_GeneratorVisitor.java ClassInfo.java MethodInfo.java VariableInfo.java
	@echo
	@echo Compilation completed successfully

clean:
	rm -rf *.class *~ offsets.txt ./outputs a.out ./syntaxtree ./visitor *.class *~ JavaCharStream.java minijava-jtb.jj MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java
	@echo
	@echo All files have been deleted

all: clean compile

compile:
	mkdir outputs
	java -jar ../jtb132di.jar -te miniJava.jj
	java -jar ../javacc5.jar miniJava-jtb.jj
	javac Main.java InformationStoringVisitor.java TypeCheckVisitor.java IR_GeneratorVisitor.java ClassInfo.java MethodInfo.java VariableInfo.java

clean:
	rm -rf *.class *~ offsets.txt ./outputs a.out

clean_outputs:
	rm -f ./outputs/*.ll

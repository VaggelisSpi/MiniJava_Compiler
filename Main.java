import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.util.*;

class Main {
	public static void main (String [] args) throws Exception {
		if(args.length == 0){
			System.err.println("Usage: java Driver <inputFile>");
			System.exit(1);
		}
		PrintWriter out_offs = new PrintWriter("offsets.txt", "UTF-8");
		int i, j;
		for (i = 0; i < args.length; i++) {
			FileInputStream fis = null;
			String[] toks = args[i].split("/");
			String name;
			name = "./outputs/" + toks[toks.length  - 1].split("\\.")[0] + ".ll";
			PrintWriter out = new PrintWriter(name, "UTF-8");
			try{
				fis = new FileInputStream(args[i]);
				MiniJavaParser parser = new MiniJavaParser(fis);
				out_offs.println("++++++++++++++++ " + args[i] + " +++++++++++++++++");

				InformationStoringVisitor isv = new InformationStoringVisitor();
				Goal root = parser.Goal();
				System.err.println(args[i] + " parsed successfully");
				root.accept(isv, null);
				Map<String, ClassInfo> classes = isv.getClassInfo();

				TypeCheckVisitor tcv = new TypeCheckVisitor();
				tcv.setClassInfo(classes);
				PrintOffsets(classes, out_offs);
				root.accept(tcv, null);
				System.err.println("Schemantic check for " + args[i] + " is successful");

				IR_GeneratorVisitor irgv = new IR_GeneratorVisitor();
				irgv.setClassInfo(classes);
				irgv.setOut(out);
				root.accept(irgv, null);
				System.err.println("Code generated properly for " + args[i]);
				out_offs.println("**************** " + args[i] + " *****************");

				out_offs.println();
				out_offs.println();
				System.out.println();
			}
			catch(Exception ex){
				System.err.println(ex.getMessage());
				System.err.println("Error occured for " + args[i]);
			}
			finally{
				try{
					if(fis != null) fis.close();
				}
				catch(IOException ex){
					System.err.println(ex.getMessage());
				}
				System.out.println();
				out_offs.println();
				out_offs.println();
			}
			out.close();
			out_offs.close();
		}
	}

	static void PrintOffsets(Map<String, ClassInfo> classes, PrintWriter out) {
		List<String> cls = new ArrayList<String>(classes.keySet());
		int i;
		for (i = 1; i < cls.size(); i++) {
			classes.get( cls.get(i) ).PrintOffsets(out);
		}
	}
}

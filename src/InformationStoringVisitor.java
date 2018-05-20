import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class InformationStoringVisitor extends GJDepthFirst<String, String> {
	Map<String, ClassInfo> classes = new LinkedHashMap<String, ClassInfo>();
	Map<String, MethodInfo> CurrentMeths = new LinkedHashMap<String, MethodInfo>();
	Map<String, VariableInfo> CurrentParams  = new LinkedHashMap<String, VariableInfo>();
	Map<String, VariableInfo> CurrentVars = new LinkedHashMap<String, VariableInfo>();
	String CurrentClass;

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String"
	* f9 -> "["
	* f10 -> "]"
	* f11 -> Identifier()
	* f12 -> ")"
	* f13 -> "{"
	* f14 -> ( VarDeclaration() )*
	* f15 -> ( Statement() )*
	* f16 -> "}"
	* f17 -> "}"
	*/
	public String visit(MainClass n, String argu) throws Exception  {

		n.f0.accept(this, argu);
		//get the name of the class and make a new class info
		String name = new String(n.f1.accept(this, argu));
		ClassInfo main_class = new ClassInfo(name, null);
		MethodInfo main_meth = new MethodInfo("main", "void"); //Main class only has one method, main
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		n.f10.accept(this, argu);

		//set the parameters of main a String[] because it's a special only available to main
		String arg = new String(n.f11.accept(this, argu));
		VariableInfo par = new VariableInfo(arg, "String[]");
		CurrentParams.put(arg, par);
		main_meth.addParams(CurrentParams);

		n.f12.accept(this, argu);
		n.f13.accept(this, argu);

		//checks if the variables of the main class have different name than the argument and if not it adds them in the method
		n.f14.accept(this, argu);
		ArrayList<String> l = new ArrayList<String>(CurrentVars.keySet());
		int i;
		VariableInfo v;
		for (i = 0; i < l.size(); i++) {
			v = CurrentVars.get(l.get(i));
			if (CurrentParams.containsKey(v.getName()) ) {
				throw new Exception ("Variable " + v.getName() + " already defined as a parameter in main");
			}
		}
		main_meth.addVars(CurrentVars);
		// main_meth.setClassName(name);

		n.f15.accept(this, argu);
		n.f16.accept(this, argu);
		n.f17.accept(this, argu);

		//base class has only main method and is added in the class
		CurrentMeths.put("main", main_meth);
		main_class.addStaticMethod(CurrentMeths);
		CurrentVars.clear();
		main_class.addVars(CurrentVars, classes);
		classes.put(name, main_class);

		CurrentMeths.clear();
		CurrentParams.clear();
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> ( VarDeclaration() )*
	* f4 -> ( MethodDeclaration() )*
	* f5 -> "}"
	*/
	public String visit(ClassDeclaration n, String argu) throws Exception  {
		CurrentVars.clear();
		n.f0.accept(this, argu);
		String name = new String(n.f1.accept(this, argu));
		CurrentClass = new String(name);
		if (classes.containsKey(name)) {
			throw new Exception ("Redefintion of class " + name);
		}

		n.f2.accept(this, argu);

		//stores the vars in the class and then clears the CurrentVars because they will be used for the methods
		ClassInfo cl = new ClassInfo(name, null);
		n.f3.accept(this, name);
		cl.addVars(CurrentVars, classes);
		CurrentVars.clear();

		//stores the methods in the class and then clears the CurrentMeths because they will be used for other classes
		n.f4.accept(this, name);
		cl.addMethods(CurrentMeths, classes);
		classes.put(name, cl);
		n.f5.accept(this, argu);

		CurrentMeths.clear();


		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "extends"
	* f3 -> Identifier()
	* f4 -> "{"
	* f5 -> ( VarDeclaration() )*
	* f6 -> ( MethodDeclaration() )*
	* f7 -> "}"
	*/
	public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
		List<String> parent_meth_list, meth_list;
		ClassInfo parent;
		n.f0.accept(this, argu);
		String name = new String(n.f1.accept(this, argu));
		CurrentClass = new String(name);
		if (classes.containsKey(name)) {
			throw new Exception ("Redefintion of class " + name);
		}

		n.f2.accept(this, argu);

		//checks if parent class is defined
		String parent_name = new String(n.f3.accept(this, argu));
		if (!classes.containsKey(parent_name)) {
			throw new Exception ("Class " + name + " attempts to extend an undefined  class");
		}
		ClassInfo cl = new ClassInfo(name, parent_name);
		n.f4.accept(this, argu);

		//stores the vars in the class and then clears the CurrentVars because they will be used for the methods
		n.f5.accept(this, argu);
		cl.addVars(CurrentVars, classes);
		CurrentVars.clear();

		//stores the methods in the class and then clears the CurrentMeths because they will be used for other classes
		n.f6.accept(this, argu);
		cl.addMethods(CurrentMeths, classes);
		CurrentMeths.clear();

		//checks if the methods of the 2 classes with the same name have the same arguments and return types
		parent = classes.get(parent_name);
		if ( !cl.compareFunctions(parent, classes) ) {
			throw new Exception();
		}

		classes.put(name, cl);  //inserts the class


		n.f7.accept(this, argu);
		return null;
   }

	/**
	* f0 -> "public"
	* f1 -> Type()
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( FormalParameterList() )?
	* f5 -> ")"
	* f6 -> "{"
	* f7 -> ( VarDeclaration() )*
	* f8 -> ( Statement() )*
	* f9 -> "return"
	* f10 -> Expression()
	* f11 -> ";"
	* f12 -> "}"
	*/
	public String visit(MethodDeclaration n, String argu) throws Exception  {
		int i;
		ArrayList<String> l;
		VariableInfo var;

		//create a new MethodInfo to store all the information about the respctive method.
		n.f0.accept(this, argu);
		String r_type = n.f1.accept(this, argu);  //gets the return
		String name = n.f2.accept(this, argu);
		MethodInfo m = new MethodInfo(name, r_type);

		n.f3.accept(this, argu);
		//checks that the method is not defined in the same class
		if (!CurrentMeths.containsKey(m.getName())) {
			n.f4.accept(this, name);
			n.f5.accept(this, argu);
			n.f6.accept(this, argu);
			n.f7.accept(this, name);
			l = new ArrayList<String>(CurrentVars.keySet());  //stores all the varaibles in a list to check if they have same name with a parameter
			for (i = 0; i < l.size(); i++) {
				var = CurrentVars.get(l.get(i));
				if ( CurrentParams.containsKey(var.getName()) ) {
					throw new Exception ("Variable " + var.getName() + " already defined as a parameter at method " + name);
				}
			}
			n.f8.accept(this, argu);
			n.f9.accept(this, argu);
			n.f10.accept(this, argu);
			n.f11.accept(this, argu);
			n.f12.accept(this, argu);

			//adds the parameters and the variables in the method
			m.addParams(CurrentParams);
			m.addVars(CurrentVars);
			// m.setClassName(CurrentClass);

			CurrentMeths.put(name, m);

			//clears the CurrentParams and CurrentVars because they will be used again for other methods
			CurrentParams.clear();
			CurrentVars.clear();
		} else {
			throw new Exception ("Redefintion of method " + m.getName() + " in class " + " name");
		}

		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	//stores all the variable in the map CurrentVars which will be added in the method or class
	public String visit(VarDeclaration n, String argu) throws Exception  {
		String type = new String(n.f0.accept(this, argu));
		String name = new String(n.f1.accept(this, argu));
		VariableInfo var = new VariableInfo(name, type);
		if ( CurrentVars.containsKey(name)) {
			throw new Exception("Redefintion of variable " + name + " in " + argu);
		}
		CurrentVars.put(name, var);
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	//stores all the parameters in the map CurrentParams which will be added in the method
	public String visit(FormalParameter n, String argu) throws Exception  {
		String type = new String(n.f0.accept(this, argu));
		String name = new String(n.f1.accept(this, argu));
		VariableInfo var = new VariableInfo(name, type);
		if ( CurrentParams.containsKey(name)) {
			throw new Exception("Redefintion of parameter " + name + " in " + argu);
		}
		CurrentParams.put(name, var);
		return name;
	}

	/**
	* f0 -> ( FormalParameterTerm() )*
	*/
	public String visit(FormalParameterTail n, String argu) throws Exception  {
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> ","
	* f1 -> FormalParameter()
	*/
	public String visit(FormalParameterTerm n, String argu) throws Exception  {
		n.f0.accept(this, argu);
		String p = new String(n.f1.accept(this, argu));
		return p;
	}

///////// For the types the visitor returns the corresponding type. The type will be stored//////////////////////////////////

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n, String argu) throws Exception  {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return "int[]";
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n, String argu) throws Exception  {
		n.f0.accept(this, argu);
		return "boolean";
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n, String argu) throws Exception  {
		n.f0.accept(this, argu);
		return "int";
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	//Retuns the indetiifer as it is
	public String visit(Identifier n, String argu) throws Exception  {
		return n.f0.toString();
	}

	Map<String, ClassInfo> getClassInfo() { return classes; }
}

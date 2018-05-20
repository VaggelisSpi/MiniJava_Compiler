import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class TypeCheckVisitor extends GJDepthFirst<String, String> {
	String CurrentClass;
	String CurrentMethod;
	boolean checkedId = false;
	Map< String, ClassInfo> classes;
	List<String> CurrentArgs;

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
	public String visit(MainClass n, String argu) throws Exception {
		n.f0.accept(this, argu);
		CurrentClass = new String(n.f1.accept(this, argu));
		checkedId = false;
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		CurrentMethod = new String("main");
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		n.f10.accept(this, argu);
		n.f11.accept(this, argu);
		checkedId = false;
		n.f12.accept(this, argu);
		n.f13.accept(this, argu);
		n.f14.accept(this, argu);
		n.f15.accept(this, argu);
		n.f16.accept(this, argu);
		n.f17.accept(this, argu);
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
	public String visit(ClassDeclaration n, String argu) throws Exception {
		n.f0.accept(this, argu);
		CurrentClass = new String(n.f1.accept(this, argu));
		checkedId = false;
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
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
		n.f0.accept(this, argu);
		CurrentClass = new String(n.f1.accept(this, argu));
		checkedId = false;
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		checkedId = false;
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
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
	public String visit(MethodDeclaration n, String argu) throws Exception {
		n.f0.accept(this, argu);
		String r_type = new String(n.f1.accept(this, argu));
		CurrentMethod = new String(n.f2.accept(this, argu));
		checkedId = false;
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);

		//check if the type of the statement in eturn is same as the type that the method returns
		String r_stmt = new String(n.f10.accept(this, argu));
		if ( !r_stmt.equals(r_type) ) {
			ClassInfo cl1 = classes.get(r_type);
			ClassInfo cl2 = classes.get(r_stmt);

			//if cl1 or cl2 is null means that their respective type was not a defined so it was a base type.
			if (cl1 == null || cl2 == null)
				throw new Exception("Type of return statement of method " + CurrentMethod + " in class " + CurrentClass + " is " + r_stmt + " but expected " + r_type);
			//check if the return statement is subtype of the return type
			if ( !cl1.isSubType(cl2, classes) ) {
				throw new Exception("Type of return statement of method " + CurrentMethod + " in class " + CurrentClass + " is " + r_stmt + " but expected " + r_type);
			}
		}

		n.f11.accept(this, argu);
		n.f12.accept(this, argu);
		return null;
	}


	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	public String visit(VarDeclaration n, String argu) throws Exception  {
		String type = new String(n.f0.accept(this, argu));
		String name = new String(n.f1.accept(this, argu));
		checkedId = false;
		//if tye is not of the base types it's gonna be a class so we check if it's defined
		if ( !type.equals("int") && !type.equals("boolean") && !type.equals("int[]")) {
			if ( !classes.containsKey(type)) {
				throw new Exception("Undefined rederence to type " + type);
			}
		}
		return name;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public String visit(FormalParameter n, String argu) throws Exception  {
		String type = new String(n.f0.accept(this, argu));
		String name = new String(n.f1.accept(this, argu));
		checkedId = false;
		//if tye is not of the base types it's gonna be a class so we check if it's defined
		if ( !type.equals("int") && !type.equals("boolean") && !type.equals("int[]")) {
			if ( !classes.containsKey(type)) {
				throw new Exception("Undefined rederence to type " + type);
			}
		}
		return name;
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n, String argu) throws Exception {
		return "int[]";
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n, String argu) throws Exception {
		return "boolean";
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n, String argu) throws Exception {
		return "int";
	}

	/**
	* f0 -> Identifier()
	* f1 -> "="
	* f2 -> Expression()
	* f3 -> ";"
	*/
	public String visit(AssignmentStatement n, String argu) throws Exception {
		checkedId = false;
		String id = new String(n.f0.accept(this, argu));
		checkedId = false;
		ClassInfo cl = classes.get(CurrentClass);
		MethodInfo meth = cl.getMethod(CurrentMethod, classes);
		//check if the identifier is declared in the current scope
		checkVarScope(cl, meth, id);
		String idType = new String(meth.getVarType(cl, id, classes)); //get the type of the identifier

		n.f1.accept(this, argu);
		String exp = new String(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		if ( idType.equals("int") || idType.equals("boolean") || idType.equals("int[]") ) {
			if ( !idType.equals(exp) ) {
				throw new Exception("Invalid assignment between " + idType + " identifier and " + exp + " expression");
			}
		} else {
			ClassInfo cl1 = classes.get(idType);
			ClassInfo cl2 = classes.get(exp);
			if (cl1 == null || cl2 == null)
				throw new Exception("Invalid assignment between " + idType + " identifier and " + exp + " expression");
			if ( !cl1.isSubType(cl2, classes) ) {
				throw new Exception("Invalid assignment between " + idType + " identifier and " + exp + " expression");
			}
		}

		return null;
	}

	/**
	* f0 -> Identifier()
	* f1 -> "["
	* f2 -> Expression()
	* f3 -> "]"
	* f4 -> "="
	* f5 -> Expression()
	* f6 -> ";"
	*/
	public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
		checkedId = false;
		String id = new String(n.f0.accept(this, argu));
		checkedId = false;
		ClassInfo cl = classes.get(CurrentClass);
		MethodInfo meth = cl.getMethod(CurrentMethod, classes);
		checkVarScope(cl, meth, id);
		String idType = new String(meth.getVarType(cl, id, classes));
		if ( !idType.equals("int[]")) {
			throw new Exception("Invalid array assignment. " + id + " is not of type int[] but it is of type " + idType);
		}
		n.f1.accept(this, argu);
		String index = new String(n.f2.accept(this, argu));
		if ( !index.equals("int")) {
			throw new Exception("Invalid array assignment. Index of array expeceted to be type int but it is of type " + index);
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		String exp = new String(n.f5.accept(this, argu));
		if ( !exp.equals("int")) {
			throw new Exception("Invalid array assignment. Right opernat was expeceted to be of type int but is of type " + exp);
		}
		n.f6.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "if"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	* f5 -> "else"
	* f6 -> Statement()
	*/
	public String visit(IfStatement n, String argu) throws Exception {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String exp = new String(n.f2.accept(this, argu));
		if ( !exp.equals("boolean") ) {
			throw new Exception("Condition of if statement expected to be boolean but it's of type " + exp);
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "while"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	*/
	public String visit(WhileStatement n, String argu) throws Exception {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String exp = new String(n.f2.accept(this, argu));
		if ( !exp.equals("boolean") ) {
			throw new Exception("Condition of while statement expected to be boolean but it's of type " + exp);
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "System.out.println"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> ";"
	*/
	public String visit(PrintStatement n, String argu) throws Exception {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String exp = new String(n.f2.accept(this, argu));
		if ( !exp.equals("int") ) {
			throw new Exception("Argument of println statement is expected to be int but is of type " + exp);
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		return null;
	}

	/**
	* f0 -> AndExpression()
	*		| CompareExpression()
	*		| PlusExpression()
	*		| MinusExpression()
	*		| TimesExpression()
	*		| ArrayLookup()
	*		| ArrayLength()
	*		| MessageSend()
	*		| Clause()
	*/
	public String visit(Expression n, String argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> Clause()
	* f1 -> "&&"
	* f2 -> Clause()
	*/
	public String visit(AndExpression n, String argu) throws Exception {
		checkedId = false;
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		if ( !lv.equals("boolean") || !rv.equals("boolean")) {
			throw new Exception("Bad opperants for operator '&&'. Expceted boolean operants but they are type of " + lv + " and " + rv);
		}
		return "boolean";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public String visit(CompareExpression n, String argu) throws Exception {
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		if ( !lv.equals("int") || !rv.equals("int")) {
			throw new Exception("Bad opperants for operator '<'. Expceted int operants but they are type of " + lv + " and " + rv);
		}
		return "boolean";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n,  String argu) throws Exception {
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		if ( !lv.equals("int") || !rv.equals("int")) {
		throw new Exception("Bad opperants for operator '+'. Expceted int operants but they are type of " + lv + " and " + rv);
		}
		return "int";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	*f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n,  String argu) throws Exception {
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		if ( !lv.equals("int") || !rv.equals("int")) {
			throw new Exception("Bad opperants for operator '+'. Expceted int operants but they are type of " + lv + " and " + rv);
		}
		return "int";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public String visit(TimesExpression n, String argu) throws Exception {
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		if ( !lv.equals("int") || !rv.equals("int")) {
			throw new Exception("Bad opperants for operator '+'. Expceted int operants but they are type of " + lv + " and " + rv);
		}
		return "int";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public String visit(ArrayLookup n, String argu) throws Exception {
		String arr = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String index = new String(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		if (!arr.equals("int[]") || !index.equals("int")) {
			throw new Exception("Invalid array lookup. Requires int[] data type with int index but array is of type " + arr + " and index is of type " + index);
		}
		return "int";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public String visit(ArrayLength n, String argu) throws Exception {
		String arr = new String(n.f0.accept(this, argu));
		if (!arr.equals("int[]")) {
			throw new Exception("Invalid opperant for operator length. Expected int[] operant but it's of type " + arr);
		}
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return "int";
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	public String visit(MessageSend n, String argu) throws Exception {
		//get the type of the primary expression. It has to be an already declared class
		String cl_type = new String(n.f0.accept(this, argu));
		ClassInfo cl = classes.get(cl_type);
		n.f1.accept(this, argu);

		//checks if the identifier is declared and is a type of an already declared class
		String id = new String(n.f2.accept(this, argu));
		checkedId = false;
		if (!cl.containsMethod(id, classes)) {
			throw new Exception("Method " + id + " is undefined in class " + cl_type);
		}

		String ret_type = cl.getMethodReturnType(id, classes); //get the return type of the method we call so we can pass it above
		n.f3.accept(this, argu);

		//we read the args and then use acceptArgs to check if they are compatible
		n.f4.accept(this, argu);
		MethodInfo meth = cl.getMethod(id, classes);
		if ( !meth.acceptArgs(CurrentArgs, classes) ) {
			throw new Exception();
		}

		n.f5.accept(this, argu);
		//clear CurrentArgs because they will be used for other calls
		if (CurrentArgs != null)
			CurrentArgs.clear();
		return ret_type;
	}

	/**
	* f0 -> IntegerLiteral()
	*	| TrueLiteral()
	*	| FalseLiteral()
	*	| Identifier()
	*	| ThisExpression()
	*	| ArrayAllocationExpression()
	*	| AllocationExpression()
	*	| BracketExpression()
	*/
	public String visit(PrimaryExpression n, String argu) throws Exception {
		checkedId = false;
		String pr = new String(n.f0.accept(this, argu));
		ClassInfo cl = classes.get(CurrentClass);
		MethodInfo meth = cl.getMethod(CurrentMethod, classes);
		//checkId is set as true when an identifier is read. When a primary Expression is an identidifer we need to check if it's declared and we will return its type
		if (checkedId == true) {
			checkVarScope(cl, meth, pr);
			String idType = new String(meth.getVarType(cl, pr, classes));
			checkedId = false;
			return idType;
		}
		//if no id was read then we return the type of the Expression we read
		checkedId = false;
		return pr;
	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	//when we have an ExpressionList for the argumetns of a method we store the type of each one of them in a list
	public String visit(ExpressionList n, String argu) throws Exception {
		CurrentArgs = new ArrayList<String>();  //create the list for the argumetns that will be used in the tail too
		String expr = null;
		String r = n.f0.accept(this, argu);
		if (r != null)  //if r is null means we have no Expression and therefore no argumetns at all. This id is used to avoid null pointer exception
			expr = new String(r);
		CurrentArgs.add(expr);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	//when we have an ExpressionList for the argumetns of a method we store the type of each one of them in a list
	public String visit(ExpressionTerm n, String argu) throws Exception {
		n.f0.accept(this, argu);
		String expr = new String(n.f1.accept(this, argu));
		CurrentArgs.add(expr);
		return expr;
	}

//////////////////// FOr the literals we return the corresponding type //////////////////////////////////

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public String visit(IntegerLiteral n, String argu) throws Exception {
		return "int";
	}

	/**
	* f0 -> "true"
	*/
	public String visit(TrueLiteral n,  String argu) throws Exception {
		return "boolean";
	}

	/**
	* f0 -> "false"
	*/
	public String visit(FalseLiteral n,  String argu) throws Exception {
		return "boolean";
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public String visit(Identifier n, String argu) throws Exception  {
		checkedId = true;
		return n.f0.toString();
	}

	/**
	* f0 -> "this"
	*/
	//return the type of the curent class we process
	public String visit(ThisExpression n,  String argu) throws Exception {
		return CurrentClass;
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public String visit(ArrayAllocationExpression n,  String argu) throws Exception {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		String typeOfExp = new String(n.f3.accept(this, argu));
		if (!typeOfExp.equals("int")) {
			throw new Exception("Array length in allocation is of invalid type.");
		}
		n.f4.accept(this, argu);
		return "int[]";
	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public String visit(AllocationExpression n,  String argu) throws Exception {
		checkedId = false;
		n.f0.accept(this, argu);
		String id = new String(n.f1.accept(this, argu));
		checkedId = false;
		if ( !classes.containsKey(id)) {
			throw new Exception("Undecalred class " + id + " in allocation");
		}
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		return id;
	}

	/**
	* f0 -> "!"
	* f1 -> Clause()
	*/
	public String visit(NotExpression n,  String argu) throws Exception {
		n.f0.accept(this, argu);
		String clauseType = new String(n.f1.accept(this, argu));
		if (!clauseType.equals("boolean")) {
			throw new Exception("Bad oppernats for operator '!'. Must be a boolean expression");
		}
		return "boolean";
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	//for a BracketExpression return the type of the Expression between the brackerts
	public String visit(BracketExpression n,  String argu) throws Exception {
		n.f0.accept(this, argu);
		String type = new String(n.f1.accept(this, argu));
		n.f2.accept(this, argu);
		return type;
	}

	//checks if a variable is defined
	void checkVarScope(ClassInfo cl, MethodInfo meth, String id) throws Exception {
		if ( !meth.isInScope(cl, id, classes) ) {
			throw new Exception("Undefined reference to " + id);
		}
	}

	void setClassInfo(Map< String, ClassInfo> cl) {
		classes = new LinkedHashMap< String, ClassInfo>(cl);
	}

}

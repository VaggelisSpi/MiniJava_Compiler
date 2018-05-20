import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import java.io.*;

class IR_GeneratorVisitor extends GJDepthFirst<String, String> {
	Map<String, ClassInfo> classes = new LinkedHashMap<String, ClassInfo>();
	List<String> cur_params = new ArrayList<String>();
	PrintWriter out;
	String CurrentClass;
	String CurrentMeth;
	String invClass;
	int var = 0;
	int if_label = 0;
	int loop_label = 0;
	int oob_label = 0;
	int tabs = 0;
	int and_label = 0;
	int alloc_label = 0;
	boolean inMeth;
	boolean checkedId = false;

	public void emit(String s) {
		int i;
		for (i = 0; i < tabs; i++)
			out.print("\t");
		out.println(s);
	}

	public String get_temp() {
		return "%_" + (var++);
	}

	public String get_if_label() {
		return "if" + (if_label++);
	}

	public String get_loop_label() {
		return "loop" + (loop_label++);
	}

	public String get_oob_label() {
		return "oob" + (oob_label++);
	}

	public String get_and_label() {
		return "andclause" + (and_label++);
	}

	public String get_array_label() {
		return "arrayallocation" + (alloc_label++);
	}

	void reset() {
		var = 0;
		if_label = 0;
		loop_label = 0;
		oob_label = 0;
		tabs = 0;
	}

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
		int i;
		List<String> class_list = new ArrayList<String>(classes.keySet());
		emit("@." + classes.get( class_list.get(0) ).getName() + "_vtable = global [0 x i8*] []");
		for (i = 1; i < class_list.size(); i++) {
			emit("@." + classes.get( class_list.get(i) ).getName() + "_vtable = global [" + classes.get( class_list.get(i) ).getMethCount() + " x i8*] [" + classes.get( class_list.get(i) ).get_Vtable(classes) + "]");
		}
		emit("declare i8* @calloc(i32, i32)\n" +
			"declare i32 @printf(i8*, ...)\n" +
			"declare void @exit(i32)\n\n" +
			"@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
			"@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
			"define void @print_int(i32 %i) {\n" +
				"\t%_str = bitcast [4 x i8]* @_cint to i8*\n" +
				"\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
				"\tret void\n" +
			"}\n\n" +
			"define void @throw_oob() {\n" +
				"\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
				"\tcall i32 (i8*, ...) @printf(i8* %_str)\n" +
				"\tcall void @exit(i32 1)\n" +
				"\tret void\n" + "}\n\n" +
			"define i32 @main() {");
		n.f0.accept(this, argu);
		String na = new String(n.f1.accept(this, argu));
		CurrentClass = new String(na);
		CurrentMeth = new String("main");
		checkedId = false;
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		n.f10.accept(this, argu);
		n.f11.accept(this, argu);
		checkedId = false;
		n.f12.accept(this, argu);
		n.f13.accept(this, argu);
		tabs++;
		inMeth = true;
		n.f14.accept(this, argu);
		inMeth = false;
		n.f15.accept(this, argu);
		tabs--;
		n.f16.accept(this, argu);
		n.f17.accept(this, argu);
		emit("\tret i32 0");
		emit("}\n");
		var = 0;
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
		String name = new String(n.f1.accept(this, argu));
		CurrentClass = new String(name);
		checkedId = false;
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, name);
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
		String name = new String(n.f1.accept(this, argu));
		CurrentClass = new String(name);
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
		// String rt = new String(n.f1.accept(this, argu));
		String t = new String(n.f1.accept(this, argu));
		String na = new String(n.f2.accept(this, argu));
		CurrentMeth = new String(na);
		MethodInfo m = classes.get(CurrentClass).getMethod(na, classes);
		emit("define " + t + " @" + CurrentClass + "." + na + m.get_LLVM_Params_withName() + " {");
		checkedId = false;
		tabs++;
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		inMeth = true;
		n.f7.accept(this, argu);
		inMeth = false;
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		String r = new String(n.f10.accept(this, argu));
		n.f11.accept(this, argu);
		n.f12.accept(this, argu);
		tabs--;
		var = 0;
		emit("\tret " + m.get_LLVM_ret_type() + " " + r);
		emit("}\n");
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	public String visit(VarDeclaration n, String argu) throws Exception {
		//if it's a VarDeclaration in a meth we should generate code, but not whene it's for the class variables
		if (inMeth) emit("%" + n.f1.accept(this, argu) + " = alloca " + n.f0.accept(this, argu) + "\n");
		checkedId = false;
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public String visit(FormalParameter n, String argu) throws Exception {
		String t = new String(n.f0.accept(this, argu));
		String id = new String(n.f1.accept(this, argu));
		checkedId = false;
		emit("%" + id + " = alloca " + t);
		emit("store " + t + " " + "%." + id + ", " + t + "* %" + id + "\n");
		return null;
	}

	/**
	* f0 -> ArrayType()
	*		| BooleanType()
	*		| IntegerType()
	*		| Identifier()
	*/
	public String visit(Type n, String argu) throws Exception {
		String  t = new String(n.f0.accept(this, argu));
		if (t.equals("i32*") || t.equals("i32") || t.equals("i1")) {
			checkedId = false;
			return t;
		} else {
			checkedId = false;
			return "i8*";
		}
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n, String argu) throws Exception {
		return "i32*";
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n, String argu) throws Exception {
		return "i1";
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n, String argu) throws Exception {
		return "i32";
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
		n.f1.accept(this, argu);
		String ex = new String(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		ClassInfo cl = classes.get(CurrentClass);
		MethodInfo meth = cl.getMethod(CurrentMeth, classes);

		if (meth.containsVar(id)) {
			String type = new String(meth.get_LLVM_VarType(cl, id, classes) );
			emit("store " + type + " " + ex + ", " + type + "* " + "%" + id + "\n");
		} else {
			String t0 = new String(get_temp());
			String t1 = new String(get_temp());
			String type = new String(cl.get_LLVM_VarType(id, classes));

			emit(t0 + " = getelementptr i8, i8* %this, " + "i32 " + (cl.getOffsetOfVar(id, classes) + 8));
			emit(t1 + " = bitcast i8* " + t0 + " to " + type + "*");
			emit("store " + type + " " + ex + ", " + type + "* " + t1 + "\n");
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
		String arr = new String(n.f0.accept(this, argu));
		checkedId = false;
		n.f1.accept(this, argu);
		String ind = new String(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		ClassInfo cl = classes.get(CurrentClass);
		MethodInfo meth = cl.getMethod(CurrentMeth, classes);
		n.f4.accept(this, argu);
		String ex = new String(n.f5.accept(this, argu));
		n.f6.accept(this, argu);

		String t0 = new String(get_temp());
		String t1 = new String(get_temp());
		String t2= new String(get_temp());
		String t3= new String(get_temp());
		String t4= new String(get_temp());

		String oob0 = new String(get_oob_label());
		String oob1 = new String(get_oob_label());
		String oob2 = new String(get_oob_label());

		if (meth.containsVar(arr)) {
			arr = "%" + arr;
		} else {
			emit(t0 + " = getelementptr i8, i8* %this, " + "i32 " + (cl.getOffsetOfVar(arr, classes) + 8));
			emit(t1 + " = bitcast i8* " + t0 + " to " + "i32**");
			arr = t1;
		}

		emit(t2 + " = load i32*, i32** " + arr);
		emit(t3 + " = load i32, " + "i32* " + t2);
		emit(t4 + " = icmp ult i32 " + ind + ", " + t3);
		emit("br i1 " + t4 + ", label %" + oob0 + ", label %" + oob1);

		tabs--;
		emit("");
		emit(oob0 + ":");
		tabs++;

		String new_ind = new String(get_temp());
		String res_ptr = new String(get_temp());
		String res = new String(get_temp());
		emit(new_ind + " = add i32 1, " + ind);
		emit(res_ptr + " = getelementptr i32, i32* " + t2 + ", i32 " + new_ind);
		emit("store i32 " + ex + ", i32* " + res_ptr);
		emit("br label %" + oob2);

		tabs--;
		emit("");
		emit(oob1 + ":");
		tabs++;
		emit("call void @throw_oob()");
		emit("br label %" + oob2);

		tabs--;
		emit("");
		emit(oob2 + ":");
		tabs++;

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
		String cond = new String(n.f2.accept(this, argu));
		String if0 = new String(get_if_label());
		String if1 = new String(get_if_label());
		String if2 = new String(get_if_label());
		emit("br i1 " + cond + ", label %" + if0 + ", label %" + if1);
		n.f3.accept(this, argu);
		tabs--;
		emit("");
		emit(if0 + ":");
		tabs++;
		n.f4.accept(this, argu);
		emit("br label %" + if2);

		n.f5.accept(this, argu);
		tabs--;
		emit("");
		emit(if1 + ":");
		tabs++;
		n.f6.accept(this, argu);
		emit("br label %" + if2);

		tabs--;
		emit(if2 + ":");
		tabs++;

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
		String loop0 = new String(get_loop_label());
		String loop1 = new String(get_loop_label());
		String loop2 = new String(get_loop_label());
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);

		emit("br label %" + loop0);
		tabs--;
		emit(loop0 + ":");
		tabs++;
		String cond = new String(n.f2.accept(this, argu));
		emit("br i1 " + cond + ", label %" + loop1 + ", label %" + loop2);

		n.f3.accept(this, argu);

		tabs--;
		emit(loop1 + ":");
		tabs++;
		n.f4.accept(this, argu);
		emit("br label %" + loop0);

		tabs--;
		emit(loop2 + ":");
		tabs++;

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
		String e = new String(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);

		emit("call void (i32) @print_int(i32 " + e + ")\n");
		return null;
	}


	/**
	* f0 -> Clause()
	* f1 -> "&&"
	* f2 -> Clause()
	*/
	public String visit(AndExpression n, String argu) throws Exception {
		String and0 = new String(get_and_label()), and1 = new String(get_and_label());
		String and2 = new String(get_and_label()), and3 = new String(get_and_label());
		String lv = new String(n.f0.accept(this, argu));
		emit("br label %" + and0 + "\n");

		tabs--;
		emit(and0 + ":");
		tabs++;
		emit("br i1 " + lv + ", label %" + and1 + ", label %" + and3);

		tabs--;
		emit(and1 + ":");
		tabs++;

		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		emit("br label %" + and2 + "\n");

		tabs--;
		emit(and2 + ":");
		tabs++;
		emit("br label %" + and3 + "\n");

		tabs--;
		emit(and3 + ":");
		tabs++;

		String r = new String(get_temp());
		emit(r + " = phi i1 [0, %" + and0 + " ], [ " + rv + ", %" + and2 + " ]");
		return r;
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
		String r = new String(get_temp());
		emit(r + " = icmp slt i32 " + lv + ", " + rv + "\n");
		return r;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n, String argu) throws Exception {
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		String r = new String(get_temp());
		emit(r + " = add i32 " + lv + ", " + rv + "\n");
		return r;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n, String argu) throws Exception {
		String lv = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		String rv = new String(n.f2.accept(this, argu));
		String r = new String(get_temp());
		emit(r + " = sub i32 " + lv + ", " + rv + "\n");
		return r;
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
		String r = new String(get_temp());
		emit(r + " = mul i32 " + lv + ", " + rv + "\n");
		return r;
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
		String ind = new String(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		String t0= new String(get_temp());
		String t1= new String(get_temp());
		String oob0 = new String(get_oob_label());
		String oob1 = new String(get_oob_label());
		String oob2 = new String(get_oob_label());
		emit(t0 + " = load i32, i32* " + arr);
		emit(t1 + " = icmp ult i32 " + ind + ", " + t0);
		emit("br i1 " + t1 + ", label %" + oob0 + ", label %" + oob1);

		tabs--;
		emit("");
		emit(oob0 + ":");
		tabs++;
		String new_ind = new String(get_temp());
		String res_ptr = new String(get_temp());
		String res = new String(get_temp());
		emit(new_ind + " = add i32 1, " + ind);
		emit(res_ptr + " = getelementptr i32, i32* " + arr + ", i32 " + new_ind);
		emit(res + " = load i32, i32* " + res_ptr);
		emit("br label %" + oob2);

		tabs--;
		emit("");
		emit(oob1 + ":");
		tabs++;
		emit("call void @throw_oob()");
		emit("br label %" + oob2);

		tabs--;
		emit("");
		emit(oob2 + ":");
		tabs++;

		return res;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public String visit(ArrayLength n, String argu) throws Exception {
		String arr = new String(n.f0.accept(this, argu));
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		String ln = new String(get_temp());
		emit(ln + " = load i32, i32* " + arr);
		return ln;
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
		String pr = new String(n.f0.accept(this, argu));
		ClassInfo cl = classes.get(invClass);
		n.f1.accept(this, argu);

		checkedId = false;
		String id = new String(n.f2.accept(this, argu));
		checkedId = false;
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);

		MethodInfo meth = cl.getMethod(id, classes);
		List<String> par_types = new ArrayList<String>(meth.get_LLVM_Param_Types());
		int i;
		String arg = "";
		arg += "(i8* " + pr;
		if (par_types.size() > 0) arg += ", ";
		for (i = 0; i < par_types.size(); i++) {
			if (i < par_types.size() - 1)
				arg += par_types.get(i) + " " + cur_params.get(i) + ", ";
			else
				arg += par_types.get(i) + " " + cur_params.get(i);
		}
		arg += ")";
		String t0 = new String(get_temp()), t1 = new String(get_temp()), t2 = new String(get_temp());
		String t3 = new String(get_temp()), t4 = new String(get_temp()), t5 = new String(get_temp());
		emit(t0 + " = bitcast i8* " + pr + " to i8***");
		emit(t1 + " = load i8**, i8*** " + t0);
		emit(t2 + " = getelementptr i8*, i8** " + t1 + ", i32 " + (cl.getOffsetOfMeth(id, classes)/8));
		emit(t3 + " = load i8*, i8** " + t2);
		emit(t4 + " = bitcast i8* " + t3 + " to " + meth.get_LLVM_ret_type() + " " + meth.get_LLVM_Params());
		emit(t5 + " = call " + meth.get_LLVM_ret_type() + " " + (t4 + arg) + "\n");
		cur_params.clear();
		return t5;
	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	public String visit(ExpressionList n, String argu) throws Exception {
		String e = new String(n.f0.accept(this, argu));
		cur_params.add(e);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public String visit(ExpressionTerm n, String argu) throws Exception {
		n.f0.accept(this, argu);
		String e = new String(n.f1.accept(this, argu));
		cur_params.add(e);
		return null;
	}

	/**
	* f0 -> IntegerLiteral()
	*		| TrueLiteral()
	*		| FalseLiteral()
	*		| Identifier()
	*		| ThisExpression()
	*		| ArrayAllocationExpression()
	*		| AllocationExpression()
	*		| BracketExpression()
	*/
	public String visit(PrimaryExpression n, String argu) throws Exception {
		checkedId = false;
		String pr = new String(n.f0.accept(this, argu));
		ClassInfo cl = classes.get(CurrentClass);
		MethodInfo meth = cl.getMethod(CurrentMeth, classes);
		if (checkedId == true) {
			//if an identifier was read then we need to determine from where we'll load it, the method or the class.
			String id = new String(pr);
			if (meth.containsVar(id)) {
				invClass = new String(meth.getVarType(cl, id, classes));
				String type = new String(meth.get_LLVM_VarType(cl, id, classes));
				pr = new String(get_temp());
				emit(pr + " = load " + type + ", " + type + "* %" + id);
			} else {
				invClass = new String(cl.getVarType(id, classes));
				String t0 = new String(get_temp());
				String t1 = new String(get_temp());
				String type = new String(cl.get_LLVM_VarType(id, classes));

				emit(t0 + " = getelementptr i8, i8* %this, " +  "i32 " + (cl.getOffsetOfVar(id, classes) + 8));
				emit(t1 + " = bitcast i8* " + t0 + " to " + type + "*");
				pr = new String(get_temp());
				emit(pr + " = load " + type + ", " + type + "* " + t1);
			}
			checkedId = false;
			return pr;
		}
		//if no id was read then we return the Expression we read
		checkedId = false;
		return pr;
	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public String visit(IntegerLiteral n, String argu) throws Exception {
		return n.f0.toString();
	}

	/**
	* f0 -> "true"
	*/
	public String visit(TrueLiteral n, String argu) throws Exception {
		return "1";
	}

	/**
	* f0 -> "false"
	*/
	public String visit(FalseLiteral n, String argu) throws Exception {
		return "0";
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public String  visit(Identifier n, String  argu) throws Exception {
		checkedId = true;
		return n.f0.toString();
	}

	/**
	* f0 -> "this"
	*/
	public String visit(ThisExpression n, String argu) throws Exception {
		invClass = new String(CurrentClass);
		return "%this";
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public String visit(ArrayAllocationExpression n, String argu) throws Exception {
		String aux1,aux2;
		n.f0.accept(this, argu);
		checkedId = false;
		n.f1.accept(this, argu);
		checkedId = false;
		n.f2.accept(this, argu);
		String ln = new String(n.f3.accept(this, argu));

		String alloc0 = new String(get_array_label());
		String alloc1 = new String(get_array_label());
		String t = new String(get_temp());
		emit(t + " = icmp slt i32 " + ln + ", 0");
		emit("br i1 " + t + ", label %" + alloc0 + ", label %" + alloc1);

		tabs--;
		emit(alloc0 + ":");
		tabs++;

		emit("call void @throw_oob()");
		emit("br label %" + alloc1);

		tabs--;
		emit(alloc1 + ":");
		tabs++;

		aux1 = new String(get_temp());
		emit(aux1 + " = add i32 " + ln + ", 1");
		aux2 = new String(get_temp());
		emit(aux2 + " =  call i8* @calloc(i32 4, i32 " + aux1 + ")");
		aux1 = new String(get_temp());
		emit(aux1 + " = bitcast i8* " + aux2 + " to i32*");
		emit("store i32 " + ln + ", i32* " + aux1 + "\n");
		n.f4.accept(this, argu);
		return aux1;
	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public String visit(AllocationExpression n, String argu) throws Exception {
		n.f0.accept(this, argu);
		checkedId = false;
		String c = new String(n.f1.accept(this, argu));
		invClass = new String(c);
		checkedId = false;
		ClassInfo cl = classes.get(c);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		String t0,t1,t2;
		t0 = new String(get_temp());
		t1 = new String(get_temp());
		t2 = new String(get_temp());
		emit(t0 + " = call i8* @calloc(i32 1, i32 " + (cl.getVarOffset() + 8) + ")");
		emit(t1 + " = bitcast i8* " + t0 + " to i8***");
		emit(t2 + " = getelementptr [" + cl.getMethCount() + " x i8*], " + "[" + cl.getMethCount() + " x i8*]* @." + c + "_vtable, i32 0, i32 0");
		emit("store i8** " + t2 + ", i8*** " + t1 + "\n");
		return t0;
	}

	/**
	* f0 -> "!"
	* f1 -> Clause()
	*/
	public String visit(NotExpression n, String argu) throws Exception {
		n.f0.accept(this, argu);
		String c = new String(n.f1.accept(this, argu));
		String t = new String(get_temp());
		emit(t + " = xor i1 1, " + c);
		return t;
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	public String visit(BracketExpression n, String argu) throws Exception {
		return n.f1.accept(this, argu);
	}

	void setClassInfo(Map< String, ClassInfo> cl) {
		classes = new LinkedHashMap< String, ClassInfo>(cl);
	}

	void setOut(PrintWriter o) {
		out = new PrintWriter(o);
	}

}

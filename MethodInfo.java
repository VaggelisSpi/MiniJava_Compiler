import java.util.*;
import java.io.*;

public class MethodInfo {
	String name;
	String returnType;
	String className;
	Map<String, VariableInfo> params;
	Map<String, VariableInfo> vars;
	int offset;

	MethodInfo(String name, String returnType) {
		this.name = new String(name);
		this.returnType = new String(returnType);
	}

	void addParams (Map<String, VariableInfo> p) {
		this.params = new LinkedHashMap<String, VariableInfo>(p);
	}

	void addVars(Map<String, VariableInfo> v) {
		this.vars = new LinkedHashMap<String, VariableInfo>(v);
	}

	boolean isSame(MethodInfo toCheck) {
		if ( !this.returnType.equals(toCheck.getReturnType()) ) {
			System.out.println("The return type of " + name + " method is incompatibble with the return type of parent class");
			return false;
		}

		if (params.size() != toCheck.getParams().size()) {
			System.out.println("Incompatible number of parameters with the definition in parent class for method " + name);
			return false;
		} else {
			int i;
			List<String> toCheckParams = new ArrayList<String>(toCheck.getParams().keySet());
			List<String> cur_params = new ArrayList<String>(this.getParams().keySet());
			for (i = 0; i < toCheckParams.size(); i++) {
				String toCheckCurParam = toCheckParams.get(i);
				String cur_param = cur_params.get(i);
				if ( !toCheck.getParam(toCheckCurParam).getType().equals( this.getParam(cur_param).getType() ) ) {
					System.out.println("Incompatible parameter type with the definition in parent class for method " + name);
					return false;
				}
			}
		}
		return true;
	}

	boolean isInScope(ClassInfo cl, String var, Map<String, ClassInfo> classes) {
		if (vars != null) {
			if ( vars.get(var) != null ) {
				return true;
			}
		}
		if (params != null) {
			if ( params.get(var) != null ) {
				return true;
			}
		}
		return cl.isInScope(var, classes);
	}

	boolean acceptArgs(List<String> CurrentArgs, Map<String, ClassInfo> classes) {
		int i;
		List<String> parList = new ArrayList<String>(params.keySet());
		if (CurrentArgs == null && params.size() == 0) {
			return true;
		} else if (CurrentArgs == null && params.size() != 0){
			System.out.println("Wrong number of arguments passed on method " + name + ". Expected " + params.size() + " arguments but got none");
			return false;
		} else {
			if (CurrentArgs.size() != params.size()) {
				System.out.println("Wrong number of arguments passed on method " + name + ". Expected " + params.size() + " arguments but got " + CurrentArgs.size());
				return false;
			} else {
				for (i = 0; i < CurrentArgs.size(); i++) {
					String arg = CurrentArgs.get(i);
					String par = params.get(parList.get(i)).getType();
					if (arg.equals("int") || arg.equals("boolean") || arg.equals("int[]")) {
						if ( !arg.equals(par)) {
							System.out.println("Conflicting types for arguments of method " + name + ". Expected type of " + par + " but got type of " + arg);
							return false;
						}
					} else {
						ClassInfo cl = classes.get(arg);
						ClassInfo cl2 = classes.get(par);
						if (cl == null || cl2 == null) {
							System.out.println("Conflicting types for arguments of method " + name + ". Expected type of " + par + " but got type of " + arg);
							return false;
						}
						if ( !cl2.isSubType(cl, classes) ) {
							System.out.println("Conflicting types for arguments of method " + name + ". Expected type of " + par + " but got type of " + arg);
							return false;
						} else {
							return true;
						}
					}
				}
			}
		}
		return true;
	}

	String getVarType(ClassInfo cl, String var,  Map<String, ClassInfo> classes) {
		if (vars != null) {
			if ( vars.get(var) != null ) {
				return vars.get(var).getType();
			}
		}
		if (params != null) {
			if ( params.get(var) != null ) {
				return params.get(var).getType();
			}
		}
		return cl.getVarType(var, classes);
	}

	String get_LLVM_VarType(ClassInfo cl, String var, Map<String, ClassInfo> classes) {
		if (vars != null) {
			if ( vars.get(var) != null ) {
				return vars.get(var).get_LLVM_type();
			}
		}
		if (params != null) {
			if ( params.get(var) != null ) {
				return params.get(var).get_LLVM_type();
			}
		}
		return cl.get_LLVM_VarType(var, classes);
	}

	String getName() { return name; }

	String getReturnType() { return returnType; }

	VariableInfo getVar(String name) { return vars.get(name); }

	VariableInfo getParam(String name) { return params.get(name); }

	Map<String, VariableInfo> getParams() { return params; }

	Map<String, VariableInfo> getVars() { return vars; }

	int getOffset() { return offset; }

	String get_LLVM_ret_type() {
		if (returnType.equals("int")) {
			return "i32";
		} else if (returnType.equals("boolean")) {
			return "i1";
		} else if (returnType.equals("int[]")) {
			return "i32*";
		} else {
			return "i8*";
		}
	}

	List<String> get_LLVM_Param_Types() {
		int i;
		List<String> parList = new ArrayList<String>(params.keySet());
		List<String> r = new ArrayList<String>();
		for (i = 0; i < parList.size(); i++) {
			r.add(params.get(parList.get(i)).get_LLVM_type());
		}
		return r;
	}

	//returns n expression of the form (i8*, type.....) and it would be used for the V_table
	String get_LLVM_Params() {
		int i;
		List<String> parList = new ArrayList<String>(params.keySet());
		String r = new String();
		r += "(i8*";
		if ( parList.size() > 0) {
			r += ", ";
		}
		for (i = 0; i < parList.size(); i++) {
			if (i < parList.size() - 1) {
				r += params.get(parList.get(i)).get_LLVM_type() + ", ";
			} else {
				r += params.get(parList.get(i)).get_LLVM_type();
			}
		}
		r += ")* ";
		return r;
	}

	//returns n expression of the form (i8* %this, type %t.....) and it would be used for the method definitions at the .ll files
	String get_LLVM_Params_withName() {
		int i;
		List<String> parList = new ArrayList<String>(params.keySet());
		String r = new String();
		r += "(i8* %this";
		if (parList.size() > 0)
			r += ", ";
		for (i = 0; i < parList.size(); i++) {
			if (i < parList.size() - 1) {
				r += params.get(parList.get(i)).get_LLVM_type() + " %." + params.get(parList.get(i)).getName() + ", ";
			} else {
				r += params.get(parList.get(i)).get_LLVM_type() + " %." + params.get(parList.get(i)).getName();
			}
		}
		r += ")";
		return r;
	}

	boolean isParam(String s) {
		if (params.containsKey(s)) return true;
		return false;
	}

	boolean isVar(String s) {
		if (vars.containsKey(s)) return true;
		return false;
	}

	void setOffset(int offset) { this.offset = offset; }

	void setClassName(String c) { className = new String(c); }

	String getClassName() { return className; }

	boolean containsVar(String id) {
		if (params.containsKey(id)) {
			return true;
		} else if (vars.containsKey(id)) {
			return true;
		} else
			return false;
	}
}

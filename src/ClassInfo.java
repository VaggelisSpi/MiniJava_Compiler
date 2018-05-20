import java.util.*;
import java.io.*;


public class ClassInfo {
	Map<String, MethodInfo> meths;
	Map<String, VariableInfo> vars;
	List<MethodInfo> allMeths;
	String name;
	String parentName;
	int varOffset;
	int methOffset;
	int methCount;

	ClassInfo(String name, String parentName) {
		this.name = new String(name);
		if (parentName != null)
			this.parentName = new String(parentName);
	}

	//adds the methods and sets the offset for each one
	void addMethods(Map<String, MethodInfo> m, Map<String, ClassInfo> classes) {
		int i;
		int offs;
		this.meths = new LinkedHashMap<String, MethodInfo>(m);
		methCount = 0;
		List<String> meth_list = new ArrayList<String>(m.keySet());
		if (parentName != null) {
			ClassInfo par = classes.get(parentName);
			offs = par.getMethOffset();
			methCount = par.getMethCount();
			for (i = 0; i < meth_list.size(); i++) {
				meths.get(meth_list.get(i)).setClassName(name);
				if ( !par.containsMethod( meth_list.get(i), classes ) ) {
					meths.get(meth_list.get(i)).setOffset(offs);
					offs+=8;
					methCount++;
				} else {
					meths.get(meth_list.get(i)).setOffset(-1);
				}
			}
		} else {
			offs = 0;
			for (i = 0; i < meth_list.size(); i++) {
				meths.get(meth_list.get(i)).setClassName(name);
				meths.get(meth_list.get(i)).setOffset(offs);
				methCount++;
				offs+=8;
			}
		}
		this.methOffset = offs;
	}

	//adds the methods and sets the offset for each one
	void addStaticMethod(Map<String, MethodInfo> m) {
		this.meths = new LinkedHashMap<String, MethodInfo>(m);
		this.methOffset = 0;
		this.methCount = 0;
		m.get("main").setOffset(-2);
	}

	//adds the variables and sets the offset for each one
	void addVars(Map<String, VariableInfo> v, Map<String, ClassInfo> classes) {
		int i;
		int offs = 0;
		this.vars = new LinkedHashMap<String, VariableInfo>(v);
		List<String> varList = new ArrayList<String>(vars.keySet());
		if (parentName != null) {
			ClassInfo par = classes.get(parentName);
			offs = par.getVarOffset();
		}
		for (i = 0; i < varList.size(); i++) {
			vars.get( varList.get(i) ).setOffset(offs);
			if ( vars.get( varList.get(i) ).getType().equals("int") ) {
				offs += 4;
			} else if (vars.get( varList.get(i) ).getType().equals("boolean")) {
				offs += 1;
			} else {
				offs += 8;
			}
		}
		this.varOffset = offs;
	}

	int getMethOffset() { return methOffset;}

	int getVarOffset() { return varOffset;}

	boolean compareFunctions(ClassInfo parent, Map<String, ClassInfo> classes) {
		List<String> meth_list = new ArrayList<String>(meths.keySet());
		int i;
		for (i = 0; i < meth_list.size(); i++) {
				if (parent.getMethod(meth_list.get(i), classes) != null) {
					if ( parent.getMethod( meth_list.get(i), classes ).getName().equals( meth_list.get(i) ) ) {
						if ( !parent.getMethod( meth_list.get(i), classes ).isSame( meths.get(meth_list.get(i) ) ) ) {
							return false;
						}
					}
				}
			}

		if (parent.getParentName() != null) {
			ClassInfo superParent = classes.get(parent.getParentName());
			if ( !this.compareFunctions(superParent, classes)) {
				return false;
			}
		}
		return true;
	}

	boolean isInScope(String var, Map<String, ClassInfo> classes) {
		if (vars != null) {
			if ( this.vars.get(var) == null ) {
				if ( this.parentName != null) {
					return classes.get(parentName).isInScope(var, classes);
				} else {
					return false;
				}
			} else {
				return true;
			}
		}

		if ( this.parentName != null) {
			return classes.get(parentName).isInScope(var, classes);
		} else {
			return false;
		}
	}

	String getVarType(String var,  Map<String, ClassInfo> classes) {
		if (vars != null) {
			if ( vars.get(var) == null ) {
				if ( parentName != null) {
					return classes.get(parentName).getVarType(var, classes);
				} else {
					return null;
				}
			} else {
				return vars.get(var).getType();
			}
		}

		if ( parentName != null) {
			return classes.get(parentName).getVarType(var, classes);
		} else {
			return null;
		}
	}

	String get_LLVM_VarType(String var,  Map<String, ClassInfo> classes) {
		if (vars != null) {
			if ( vars.get(var) == null ) {
				if ( parentName != null) {
					return classes.get(parentName).get_LLVM_VarType(var, classes);
				} else {
					return null;
				}
			} else {
				return vars.get(var).get_LLVM_type();
			}
		}

		if ( parentName != null) {
			return classes.get(parentName).get_LLVM_VarType(var, classes);
		} else {
			return null;
		}
	}

	boolean containsMethod(String meth, Map<String, ClassInfo> classes) {
		if (this.meths.containsKey(meth)) {
			return true;
		} else {
			if (parentName != null) {
				return classes.get(parentName).containsMethod(meth, classes);
			} else {
				return false;
			}
		}
	}

	boolean containsVar(String var, Map<String, ClassInfo> classes) {
		if (this.vars.containsKey(var)) {
			return true;
		} else {
			if (parentName != null) {
				return classes.get(parentName).containsVar(var, classes);
			} else {
				return false;
			}
		}
	}

	Map<String, MethodInfo> getMethods() { return meths; }

	Map<String, VariableInfo> getVars() { return vars; }

	MethodInfo getMethod(String methName, Map<String, ClassInfo> classes) {
		if (meths.get(methName) != null) {
			return meths.get(methName);
		} else {
			if (parentName != null) {
				return classes.get(parentName).getMethod(methName, classes);
			} else {
				return null;
			}
		}
	 }

	String getMethodReturnType(String name, Map<String, ClassInfo> classes) {
		if (meths.get(name) != null) {
			return meths.get(name).getReturnType();
		} else {
			if (parentName != null) {
				return classes.get(parentName).getMethodReturnType(name, classes);
			} else {
				return null;
			}
		}
	}

	//checks if cl is cubtype of the current class
	boolean isSubType(ClassInfo cl, Map<String, ClassInfo> classes) {
		if ( name.equals(cl.getName() ) ) {
			return true;
		} else {
			//if not the same class checks if cl was extented from current class
			if ( classes.get( cl.getParentName() ) != null ) {
				return this.isSubType(classes.get( cl.getParentName() ), classes);
			} else {
				return false;
			}
		}
	}

	void PrintOffsets(PrintWriter out) {
		int i;
		List<String> ms;
		List<String> vs;
		out.println("-----------Class " + name +"-----------");
		out.println("--Variables---");
		if (vars != null) {
			vs = new ArrayList<String>(vars.keySet());
			for (i = 0; i < vs.size(); i++) {
				out.println(name + "." + vars.get( vs.get(i) ).getName() + " : " +  vars.get( vs.get(i) ).getOffset());
			}
		}

		out.println("---Methods---");
		if (meths != null) {
			ms = new ArrayList<String>(meths.keySet());
			for (i = 0; i < ms.size(); i++) {
				if (meths.get( ms.get(i) ).getOffset() != -1 && meths.get( ms.get(i) ).getOffset() != -2)
					out.println(name + "." + meths.get( ms.get(i) ).getName() + " : " +  meths.get( ms.get(i) ).getOffset());
			}
		}
		out.println();
	}

	VariableInfo getVar(String name) { return vars.get(name); }

	String getName() { return name; }

	int getMethCount() {
		return methCount;
	}

	String get_Vtable(Map<String, ClassInfo> classes) {
		int i;
		List<String> meth_list = new ArrayList<String>(meths.keySet());
		List<MethodInfo> par_meth_list;
		String vs = "";
		int funcs_wrtn = 0;

		if (parentName != null) {
			par_meth_list = new ArrayList<MethodInfo>(classes.get(parentName).getAllMeths(classes));

			for (i = 0; i < par_meth_list.size(); i++) {
				//a method is defined in the parent but not here so the V_table would point to it
				if (par_meth_list.get(i).getOffset() != -2 && !meths.containsKey(par_meth_list.get(i).getName())) {
					vs += "i8* bitcast (" + par_meth_list.get(i).get_LLVM_ret_type() + " " + par_meth_list.get(i).get_LLVM_Params() + "@" + par_meth_list.get(i).getClassName() + "." +  par_meth_list.get(i).getName() + " to i8*)";
					funcs_wrtn++;
					if (funcs_wrtn < methCount) vs += ", ";
				} else if (meths.containsKey(par_meth_list.get(i).getName())) { //the method is redifined here so we point to the current method
					vs += "i8* bitcast (" + par_meth_list.get(i).get_LLVM_ret_type() + " " + meths.get(par_meth_list.get(i).getName()).get_LLVM_Params() + "@" + name + "." +  meths.get(par_meth_list.get(i).getName()).getName() + " to i8*)";
					funcs_wrtn++;
					if (funcs_wrtn < methCount) vs += ", ";
				}
			}
		}

		for (i = 0; i < meth_list.size(); i++) {
			if (meths.get(meth_list.get(i)).getOffset() != -2 && meths.get(meth_list.get(i)).getOffset() != -1) {
				vs += "i8* bitcast (" + meths.get(meth_list.get(i)).get_LLVM_ret_type() + " " + meths.get(meth_list.get(i)).get_LLVM_Params() + "@" + name + "." +  meths.get(meth_list.get(i)).getName() + " to i8*)";
				funcs_wrtn++;
				if (funcs_wrtn < methCount) vs += ", ";
			}
		}
		return vs;
	}

	int getOffsetOfVar(String v, Map<String, ClassInfo> classes) {
		if (vars.containsKey(v)) {
			return vars.get(v).getOffset();
		} else {
			return classes.get(parentName).getOffsetOfVar(v, classes);
		}
	}

	int getOffsetOfMeth(String m, Map<String, ClassInfo> classes) {
		if (meths.containsKey(m)) {
			if (meths.get(m).getOffset() != -1)
				return meths.get(m).getOffset();
			else
				return classes.get(parentName).getOffsetOfMeth(m, classes);
		} else {
			return classes.get(parentName).getOffsetOfMeth(m, classes);
		}
	}

	List<MethodInfo> getAllMeths(Map<String, ClassInfo> classes) {
		List<MethodInfo> meth_list = new ArrayList<MethodInfo>();
		List<String> meth_names = new ArrayList<String>(meths.keySet());

		if (parentName != null) {
			meth_list.addAll(classes.get(parentName).getAllMeths(classes));
		}

		int i;
		for (i = 0; i < meth_names.size(); i++) {
			if (meths.get(meth_names.get(i)).getOffset() != -1) {
				meth_list.add(meths.get(meth_names.get(i)));
			} else {
				//if a meth is defined in both a parent and here we should replace the previous one with this one so the V_table would be made properly
				int j;
				for (j = 0; j < meth_list.size() ; j++) {
					if ( meth_list.get(j).getName().equals( meths.get( meth_names.get(i)).getName() ) ) {
						meth_list.set(j, meths.get( meth_names.get(i)));
					}
				}
			}
		}

		return meth_list;
	}

	String getParentName() { return parentName; }
}

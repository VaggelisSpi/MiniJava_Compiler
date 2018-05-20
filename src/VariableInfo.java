import java.util.*;
import java.io.*;

public class VariableInfo {
	String name;
	String type;
	int offset;

	VariableInfo(String name, String type) {
		this.name = new String(name);
		this.type = new String(type);
	}

	String getType() {return type;}

	String getName() {return name;}

	int getOffset() { return offset; }

	void setOffset(int offset) { this.offset = offset; }

	String get_LLVM_type() {
		if (type.equals("int")) {
			return "i32";
		} else if (type.equals("boolean")) {
			return "i1";
		} else if (type.equals("int[]")) {
			return "i32*";
		} else {
			return "i8*";
		}
	}

}

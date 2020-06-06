package listener.main;

import java.util.HashMap;
import java.util.Map;
import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.Var_declContext;
import static listener.main.BytecodeGenListenerHelper.*;

public class USymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}

	static public class VarInfo {
		Type type;
		int id;
		int initVal;

		public VarInfo(Type type, int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}

		public VarInfo(Type type, int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}

	static public class FInfo {
		public String sigStr;
	}

	private Map<String, VarInfo> _lsymtable = new HashMap<>(); // local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>(); // global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>(); // function

	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;

	USymbolTable() {
		initFunDecl();
		initFunTable();
	}

	void initFunDecl() { // at each func decl
		_lsymtable = new HashMap<>(); // local v.
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;
	}

	void putLocalVar(String varname, Type type) {
		_lsymtable.put(varname, new VarInfo(type, _localVarID));
		_localVarID++;
	}

	void putGlobalVar(String varname, Type type) {
		_gsymtable.put(varname, new VarInfo(type, _globalVarID));
		_globalVarID++;
	}

	void putLocalVarWithInitVal(String varname, Type type, int initVar) {
		_lsymtable.put(varname, new VarInfo(type, _localVarID, initVar));
		_localVarID++;
	}

	void putGlobalVarWithInitVal(String varname, Type type, int initVar) {
		_gsymtable.put(varname, new VarInfo(type, _globalVarID, initVar));
		_globalVarID++;
	}

	void putParams(MiniCParser.ParamsContext params) {
		for (int i = 0; i < params.param().size(); i++) {
			if (params.param(i) instanceof MiniCParser.ParamContext) {
				if (params.param(i).type_spec().INT() != null) {
					putLocalVar(params.param(i).IDENT().getText(), Type.INT);
				}
			}
		}
	}

	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "_print";

		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}

	public String getFunSpecStr(String fname) {
		FInfo finfo = _fsymtable.get(fname);
		if (finfo != null) {
			return finfo.sigStr;
		}

		return null;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		FInfo finfo = _fsymtable.get(ctx.IDENT().getText());
		if (finfo != null) {
			return finfo.sigStr;
		}

		return null;
	}

	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = "";
		String rtype = "";
		String res = "";

		argtype = BytecodeGenListenerHelper.getParamTypesText(ctx.params());
		rtype = BytecodeGenListenerHelper.getTypeText(ctx.type_spec());

		res = fname + "(" + argtype + ")" + rtype;

		FInfo finfo = new FInfo();
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);

		return res;
	}

	String getVarId(String name) {
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return String.valueOf(lvar.id);
		}

		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return String.valueOf(gvar.id);
		}
		return null;
	}

	Type getVarType(String name) {
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}

		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}

		return Type.ERROR;
	}

	String newLabel() {
		return "label" + _labelID++;
	}

	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	// local
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	public int getLocalVarCount() {
		return _localVarID;
	}

}

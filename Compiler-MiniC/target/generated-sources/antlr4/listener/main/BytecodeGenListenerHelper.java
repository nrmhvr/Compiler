package listener.main;

import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;

public class BytecodeGenListenerHelper {

	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof MiniCParser.Fun_declContext;
	}

	// param : type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}

	// <global vars>
	// var_decl : type_spec IDENT ';'
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl : type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5;
	}

	// var_decl : type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// local_decl : type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// local_decl : type_spec IDENT '=' LITERAL ';'
	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5;
	}

	static boolean isVoidF(Fun_declContext ctx) {
		return ctx.type_spec().VOID() != null;
	}

	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 3;
	}

	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}

	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}

	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}

	static String getTypeText(Type_specContext typespec) {
		if (typespec.INT() != null) {
			return "I";
		} else {
			return "V";
		}
	}

	// params
	static String getParamName(ParamContext param) {
		return null;
	}

	static String getParamTypesText(ParamsContext params) {
		String typeText = "";

		for (int i = 0; i < params.param().size(); i++) {
			MiniCParser.Type_specContext typespec = (MiniCParser.Type_specContext) params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}

	static String getLocalVarName(Local_declContext local_decl) {
		return local_decl.IDENT().getText();
	}

	static String getFunName(Fun_declContext ctx) {
		return ctx.IDENT().getText();
	}

	static String getFunName(ExprContext ctx) {
		return ctx.IDENT().getText();
	}

	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() < 6;
	}

	static String getFunProlog() {
		return ".class public " + getCurrentClassName()
				+ "\n.super java/lang/Object\n\n.method public <init>()V\naload_0\ninvokenonvirtual java/lang/Object/<init>()V\nreturn\n.end method\n\n";
	}

	static String getCurrentClassName() {
		return "Test";
	}
}

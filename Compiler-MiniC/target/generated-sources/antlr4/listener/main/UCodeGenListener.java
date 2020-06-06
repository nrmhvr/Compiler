package listener.main;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ParamsContext;
import listener.main.USymbolTable.Type;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.USymbolTable.*;

public class UCodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	USymbolTable UsymbolTable = new USymbolTable();

	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
		// fun_decl : type_spec IDENT '(' params ')' compound_stmt ;
		UsymbolTable.initFunDecl();

		String fname = getFunName(ctx);
		ParamsContext params;

		if (fname.equals("main")) {
			UsymbolTable.putLocalVar("args", Type.INTARRAY);
		} else {
			UsymbolTable.putFunSpecStr(ctx);
			params = (MiniCParser.ParamsContext) ctx.getChild(3);
			UsymbolTable.putParams(params);
		}
	}

	@Override
	public void enterVar_decl(MiniCParser.Var_declContext ctx) {
		// 전역변수는 없다고 가정
//		String varName = ctx.IDENT().getText();
//
//		if (isArrayDecl(ctx)) {
//			// var_decl : type_spec IDENT '[' LITERAL ']' ';'
//			symbolTable.putGlobalVar(varName, Type.INTARRAY);
//		} else if (isDeclWithInit(ctx)) {
//			// var_decl : type_spec IDENT '=' LITERAL ';'
//			symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
//		} else {
//			// var_decl : type_spec IDENT ';'
//			symbolTable.putGlobalVar(varName, Type.INT);
//		}
	}

	@Override
	public void enterLocal_decl(MiniCParser.Local_declContext ctx) {
		if (isArrayDecl(ctx)) {
			// local_decl : type_spec IDENT '[' LITERAL ']' ';'
			UsymbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		} else if (isDeclWithInit(ctx)) {
			// local_decl : type_spec IDENT '=' LITERAL ';'
			UsymbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
		} else {
			// local_decl : type_spec IDENT ';'
			UsymbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}
	}

	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		// program : decl+

		String fun_decl = "", var_decl = "";

		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (isFunDecl(ctx, i))
				fun_decl += newTexts.get(ctx.decl(i));
			else
				var_decl += newTexts.get(ctx.decl(i));
		}

		newTexts.put(ctx, var_decl + fun_decl);

		System.out.println(newTexts.get(ctx));
	}

	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if (ctx.getChildCount() == 1) {
			// decl : var_decl
			if (ctx.var_decl() != null)
				decl += newTexts.get(ctx.var_decl());
			else
				// decl : fun_decl
				decl += newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, decl);
	}

	@Override
	public void exitStmt(MiniCParser.StmtContext ctx) {
		String stmt = "";
		if (ctx.getChildCount() > 0) {
			if (ctx.expr_stmt() != null) {
				// stmt : expr_stmt
				stmt += newTexts.get(ctx.expr_stmt());
			} else if (ctx.compound_stmt() != null) {
				// stmt : compound_stmt
				stmt += newTexts.get(ctx.compound_stmt());
			} else if (ctx.if_stmt() != null) {
				// stmt : if_stmt
				stmt += newTexts.get(ctx.if_stmt());
			} else if (ctx.while_stmt() != null) {
				// stmt : while_stmt
				stmt += newTexts.get(ctx.while_stmt());
			} else {
				// stmt : return_stmt
				stmt += newTexts.get(ctx.return_stmt());
			}
		}
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if (ctx.getChildCount() == 2) {
			// expr_stmt : expr ';'
			stmt += newTexts.get(ctx.expr());
		}
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		// while_stmt : WHILE '(' expr ')' stmt
		String stmt = "";
		String condExpr = newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt());

		String loop = UsymbolTable.newLabel();
		String end = UsymbolTable.newLabel();

		stmt += "\t" + loop + ":\n" + condExpr + "\tifeq " + end + "\n" + thenStmt + "\tgoto " + loop + "\n\t" + end
				+ ":\n";
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
		// fun_decl : type_spec IDENT '(' params ')' compound_stmt ;
		String header = getFunName(ctx) + "\tproc " + UsymbolTable.getLocalVarCount() + " 2 2 \n";
		for (int i = 1; i <= UsymbolTable.getLocalVarCount(); i++) {
			header += "\tsym 2 " + i + " 1\n";
		}
		String stmt = newTexts.get(ctx.compound_stmt());
		if (ctx.IDENT().getText().equals("main") && ctx.type_spec().VOID() != null) {
			newTexts.put(ctx, header + stmt + "\t" + "ret" + "\n\t" + "end\n\n");
		} else {
			newTexts.put(ctx, header + stmt + "\t" + "end\n\n");
		}
	}

	@Override
	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		// 전역변수는 없다고 가정
//		String varName = ctx.IDENT().getText();
//		String varDecl = "";
//
//		if (isArrayDecl(ctx)) {
//			// var_decl : type_spec IDENT '[' LITERAL ']' ';'
//			// 배열 없다고 가정
//		} else if (isDeclWithInit(ctx)) {
//			// var_decl : type_spec IDENT '=' LITERAL ';'
//			varDecl += "putfield " + varName + "\n";
//			// v. initialization => Later! skip now..: 값 초기화는 나중에
//		} else {
//			// var_decl : type_spec IDENT ';'
//			varDecl += "putfield " + varName + "\n";
//		}
//		newTexts.put(ctx, varDecl);
	}

	@Override
	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		String varDecl = "";

		if (isArrayDecl(ctx)) {
			// local_decl : type_spec IDENT '[' LITERAL ']' ';'
			// 배열 없다고 가정
		} else if (isDeclWithInit(ctx)) {
			// local_decl : type_spec IDENT '=' LITERAL ';'
			String vId = UsymbolTable.getVarId(ctx);
			varDecl += "\t" + "sym 2 " + vId + " 1\n";
			varDecl += "\t" + "ldc " + ctx.LITERAL().getText() + "\n\t" + "str 2 " + vId + "\n";
		} else {
			// local_decl : type_spec IDENT ';'
		}

		newTexts.put(ctx, varDecl);
	}

	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// compound_stmt : '{' local_decl* stmt* '}'
		String stmt = "";
		for (int i = 0; i < ctx.local_decl().size(); i++) {
			stmt += newTexts.get(ctx.local_decl(i));
		}
		for (int i = 0; i < ctx.stmt().size(); i++) {
			stmt += newTexts.get(ctx.stmt(i));
		}
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr = newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt(0));

		String lend = UsymbolTable.newLabel();
		String lelse = UsymbolTable.newLabel();

		if (noElse(ctx)) {
			// if_stmt : IF '(' expr ')' stmt
			stmt += condExpr + "\tifeq " + lend + "\n" + thenStmt + "\t" + lend + ":\n";
		} else {
			// if_stmt : IF '(' expr ')' stmt ELSE stmt
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr + "\tifeq " + lelse + "\n" + thenStmt + "\tgoto " + lend + "\n\t" + lelse + ":\n"
					+ elseStmt + "\t" + lend + ":\n";
		}

		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		String rtn = "";

		if (isVoidReturn(ctx)) {
			// return_stmt : RETURN ';'
			rtn += "\t" + "retv" + "\n";
		} else {
			// return_stmt : RETURN expr ';'
			String expr = newTexts.get(ctx.expr());
			rtn += expr + "\t" + "retv" + "\n";
		}

		newTexts.put(ctx, rtn);
	}

	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String expr = "";

		if (ctx.getChildCount() <= 0) {
			newTexts.put(ctx, "");
			return;
		}

		if (ctx.getChildCount() == 1) { // IDENT | LITERAL
			if (ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();
				if (UsymbolTable.getVarType(idName) == Type.INT) {
					expr += "\t" + "lod 2 " + (Integer.parseInt(UsymbolTable.getVarId(idName)) + 1) + " \n";
				}
				// else // Type int array => Later! skip now..
				// expr += " lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
			} else if (ctx.LITERAL() != null) {
				String literalStr = ctx.LITERAL().getText();
				expr += "\t" + "ldc " + literalStr + " \n";
			}
		} else if (ctx.getChildCount() == 2) { // UnaryOperation
			expr = handleUnaryExpr(ctx, newTexts.get(ctx) + expr);
		} else if (ctx.getChildCount() == 3) {
			if (ctx.getChild(0).getText().equals("(")) { // '(' expr ')'
				expr = newTexts.get(ctx.expr(0));
			} else if (ctx.getChild(1).getText().equals("=")) { // IDENT '=' expr
				expr = newTexts.get(ctx.expr(0)) + "\t" + "str " + UsymbolTable.getVarId(ctx.IDENT().getText()) + " \n";

			} else { // binary operation
				expr = handleBinExpr(ctx, expr);
			}
		}
		// IDENT '(' args ')' | IDENT '[' expr ']'
		else if (ctx.getChildCount() == 4) {
			if (ctx.args() != null) { // function calls
				expr = newTexts.get(ctx.args()) + "\tcall " + getFunName(ctx) + "\n";
//				expr = handleFunCall(ctx, expr);
			} else { // expr
				// Arrays: TODO
			}
		}
		// IDENT '[' expr ']' '=' expr
		else { // Arrays: TODO */
		}
		newTexts.put(ctx, expr);
	}

	private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) {
		String l1 = UsymbolTable.newLabel();
		String l2 = UsymbolTable.newLabel();
		String lend = UsymbolTable.newLabel();

		if (expr.equals("null")) {
			expr = newTexts.get(ctx.expr(0));
		} else {
			expr += newTexts.get(ctx.expr(0));
		}
		switch (ctx.getChild(0).getText()) {
		case "-":
			expr += "\tineg \n";
			break;
		case "--":
			expr += "\tldc 1" + "\n\t" + "sub" + "\n";
			if (ctx.expr(0).IDENT() != null) {
				expr += "\tstr 2 " + UsymbolTable.getVarId(ctx.expr(0).IDENT().getText()) + " 1\n";
			}
			break;
		case "++":
			expr += "\tldc 1" + "\n\t" + "add" + "\n";
			if (ctx.expr(0).IDENT() != null) {
				expr += "\tstr 2 " + UsymbolTable.getVarId(ctx.expr(0).IDENT().getText()) + " 1\n";
			}
			break;
		case "!":
			expr += "\tifeq " + l2 + "\n\t" + l1 + ":\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2 + ":\n\t"
					+ "ldc 1" + "\n\t" + lend + ":\n";
		}
		return expr;
	}

	private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
		String l2 = UsymbolTable.newLabel();
		String lend = UsymbolTable.newLabel();

		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));

		switch (ctx.getChild(1).getText()) {
		case "*":
			expr += "\tmul\n";
			break;
		case "/":
			expr += "\tdiv\n";
			break;
		case "%":
			expr += "\trem\n";
			break;
		case "+":
			expr += "\tadd\n";
			break;
		case "-":
			expr += "\tsub\n";
			break;
		case "==":
			expr += "\tsub " + "\n\t" + "ifeq " + l2 + "\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2
					+ ":\n\t" + "ldc 1" + "\n\t" + lend + ": " + "\n";
			break;
		case "!=":
			expr += "\tsub " + "\n\t" + "ifne " + l2 + "\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2
					+ ":\n\t" + "ldc 1" + "\n\t" + lend + ": " + "\n";
			break;
		case "<=":
			expr += "\tif_icmple " + l2 + "\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2 + ":\n\t" + "ldc 1"
					+ "\n\t" + lend + ": " + "\n";
			break;
		case "<":
			expr += "\tif_icmplt " + l2 + "\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2 + ":\n\t" + "ldc 1"
					+ "\n\t" + lend + ": " + "\n";
			break;
		case ">=":
			expr += "\tif_icmpge " + l2 + "\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2 + ":\n\t" + "ldc 1"
					+ "\n\t" + lend + ": " + "\n";
			break;
		case ">":
			expr += "\tif_icmpgt " + l2 + "\n\t" + "ldc 0" + "\n\t" + "goto " + lend + "\n\t" + l2 + ":\n\t" + "ldc 1"
					+ "\n\t" + lend + ": " + "\n";
			break;
		case "and":
			expr += "\tifne " + lend + "\n\t" + "pop" + "\n\t" + "ldc 0" + "\n\t" + lend + ":\n";
			break;
		case "or":
			expr += "\tifeq " + lend + "\n\t" + "pop" + "\n\t" + "ldc 1" + "\n\t" + lend + ":\n";
			break;
		}
		return expr;
	}

	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {
		// args : expr (',' expr)* | ;
		String argsStr = "";

		for (int i = 0; i < ctx.expr().size(); i++) {
			argsStr += newTexts.get(ctx.expr(i));
		}
		newTexts.put(ctx, argsStr);
	}
}

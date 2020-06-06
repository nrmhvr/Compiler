package listener.main;

import java.io.FileWriter;
import java.io.IOException;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ParamsContext;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();

	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
		// fun_decl : type_spec IDENT '(' params ')' compound_stmt ;
		symbolTable.initFunDecl();

		String fname = getFunName(ctx);
		ParamsContext params;

		if (fname.equals("main")) {
			symbolTable.putLocalVar("args", Type.INTARRAY);
		} else {
			symbolTable.putFunSpecStr(ctx);
			params = (MiniCParser.ParamsContext) ctx.getChild(3);
			symbolTable.putParams(params);
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
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		} else if (isDeclWithInit(ctx)) {
			// local_decl : type_spec IDENT '=' LITERAL ';'
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
		} else {
			// local_decl : type_spec IDENT ';'
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}
	}

	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		// program : decl+
		String classProlog = getFunProlog();

		String fun_decl = "", var_decl = "";

		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (isFunDecl(ctx, i))
				fun_decl += newTexts.get(ctx.decl(i));
			else
				var_decl += newTexts.get(ctx.decl(i));
		}

		newTexts.put(ctx, classProlog + var_decl + fun_decl);

		System.out.println(newTexts.get(ctx));

		// export file
		try (FileWriter fw = new FileWriter("Test.j");) {
			fw.write(newTexts.get(ctx));
			fw.close();
			System.out.println("Test.j 파일이 생성되었습니다.");
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		String loop = symbolTable.newLabel();
		String end = symbolTable.newLabel();

		stmt += loop + ":\n" + condExpr + "ifeq " + end + "\n" + thenStmt + "goto " + loop + "\n" + end + ":\n";
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
		// fun_decl : type_spec IDENT '(' params ')' compound_stmt ;
		String header = ".method public static " + symbolTable.getFunSpecStr(ctx.IDENT().getText()) + "\n"
				+ ".limit stack " + getStackSize(ctx) + "\n" + ".limit locals " + getLocalVarSize(ctx) + "\n";
		String stmt = newTexts.get(ctx.compound_stmt());
		if (ctx.IDENT().getText().equals("main") && ctx.type_spec().VOID() != null) {
			newTexts.put(ctx, header + stmt + "return\n.end method\n\n");
		} else {
			newTexts.put(ctx, header + stmt + ".end method\n\n");
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
			String vId = symbolTable.getVarId(ctx);
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n" + "istore_" + vId + "\n";
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

		String lend = symbolTable.newLabel();
		String lelse = symbolTable.newLabel();

		if (noElse(ctx)) {
			// if_stmt : IF '(' expr ')' stmt
			stmt += condExpr + "ifeq " + lend + "\n" + thenStmt + lend + ":\n";
		} else {
			// if_stmt : IF '(' expr ')' stmt ELSE stmt
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr + "ifeq " + lelse + "\n" + thenStmt + "goto " + lend + "\n" + lelse + ":\n" + elseStmt
					+ lend + ":\n";
		}

		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		String rtn = "";

		if (isVoidReturn(ctx)) {
			// return_stmt : RETURN ';'
			rtn += ctx.RETURN().getText();
		} else {
			// return_stmt : RETURN expr ';'
			String expr = newTexts.get(ctx.expr());
			rtn += expr + "i" + ctx.RETURN().getText() + "\n";
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
				if (symbolTable.getVarType(idName) == Type.INT) {
					expr += "iload_" + symbolTable.getVarId(idName) + " \n";
				}
				// else // Type int array => Later! skip now..
				// expr += " lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
			} else if (ctx.LITERAL() != null) {
				String literalStr = ctx.LITERAL().getText();
				expr += "ldc " + literalStr + " \n";
			}
		} else if (ctx.getChildCount() == 2) { // UnaryOperation
			expr = handleUnaryExpr(ctx, newTexts.get(ctx) + expr);
		} else if (ctx.getChildCount() == 3) {
			if (ctx.getChild(0).getText().equals("(")) { // '(' expr ')'
				expr = newTexts.get(ctx.expr(0));
			} else if (ctx.getChild(1).getText().equals("=")) { // IDENT '=' expr
				expr = newTexts.get(ctx.expr(0)) + "istore_" + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";

			} else { // binary operation
				expr = handleBinExpr(ctx, expr);
			}
		}
		// IDENT '(' args ')' | IDENT '[' expr ']'
		else if (ctx.getChildCount() == 4) {
			if (ctx.args() != null) { // function calls
				expr = handleFunCall(ctx, expr);
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
		String l1 = symbolTable.newLabel();
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		if (expr.equals("null")) {
			expr = newTexts.get(ctx.expr(0));
		} else {
			expr += newTexts.get(ctx.expr(0));
		}
		switch (ctx.getChild(0).getText()) {
		case "-":
			expr += "ineg\n";
			break;
		case "--":
			expr += "ldc 1" + "\n" + "isub" + "\n";
			if (ctx.expr(0).IDENT() != null) {
				expr += "istore_" + symbolTable.getVarId(ctx.expr(0).IDENT().getText()) + "\n";
			}
			break;
		case "++":
			expr += "ldc 1" + "\n" + "iadd" + "\n";
			if (ctx.expr(0).IDENT() != null) {
				expr += "istore_" + symbolTable.getVarId(ctx.expr(0).IDENT().getText()) + "\n";
			}
			break;
		case "!":
			expr += "ifeq " + l2 + "\n" + l1 + ":\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1"
					+ "\n" + lend + ":\n";
		}
		return expr;
	}

	private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));

		switch (ctx.getChild(1).getText()) {
		case "*":
			expr += "imul\n";
			break;
		case "/":
			expr += "idiv\n";
			break;
		case "%":
			expr += "irem\n";
			break;
		case "+":
			expr += "iadd\n";
			break;
		case "-":
			expr += "isub\n";
			break;
		case "==":
			expr += "isub " + "\n" + "ifeq " + l2 + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1"
					+ "\n" + lend + ": " + "\n";
			break;
		case "!=":
			expr += "isub " + "\n" + "ifne " + l2 + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1"
					+ "\n" + lend + ": " + "\n";
			break;
		case "<=":
			expr += "if_icmple " + l2 + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1" + "\n"
					+ lend + ": " + "\n";
			break;
		case "<":
			expr += "if_icmplt " + l2 + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1" + "\n"
					+ lend + ": " + "\n";
			break;
		case ">=":
			expr += "if_icmpge " + l2 + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1" + "\n"
					+ lend + ": " + "\n";
			break;
		case ">":
			expr += "if_icmpgt " + l2 + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ":\n" + "ldc 1" + "\n"
					+ lend + ": " + "\n";
			break;
		case "and":
			expr += "ifne " + lend + "\n" + "pop" + "\n" + "ldc 0" + "\n" + lend + ":\n";
			break;
		case "or":
			expr += "ifeq " + lend + "\n" + "pop" + "\n" + "ldc 1" + "\n" + lend + ":\n";
			break;
		}
		return expr;
	}

	private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
		String fname = getFunName(ctx);

		if (fname.equals("_print")) { // System.out.println
			expr = "getstatic java/lang/System/out Ljava/io/PrintStream;" + "\n" + newTexts.get(ctx.args())
					+ "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
		} else {
			expr = newTexts.get(ctx.args()) + "invokestatic " + getCurrentClassName() + "/"
					+ symbolTable.getFunSpecStr(fname) + "\n";
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

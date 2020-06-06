package listener.main;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import generated.*;

public class MiniCPrintListener extends MiniCBaseListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	int dot_count = 0;

	// 2진 연산자 확인
	boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr();
	}

	// 전위 연산자 확인
	boolean isPreOperation(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 2 && ctx.getChild(0) != ctx.expr();
	}

	// dot print
	public String dotPrint() {
		String dot = "";
		for (int i = 0; i < this.dot_count; i++) {
			dot += "....";
		}
		return dot;
	}

	// print
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		for (int i = 0; i < ctx.decl().size(); i++) {
			System.out.println(newTexts.get(ctx.decl(i)));
		}
	}

	public void exitDecl(MiniCParser.DeclContext ctx) {
		String s1 = null;

		// decl : var_decl
		if (ctx.getChild(0) instanceof MiniCParser.Var_declContext) {
			s1 = newTexts.get(ctx.var_decl());
		} else {
			// decl : fun_decl
			s1 = newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, s1);
	}

	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		String s1 = null, s2 = null;

		// var_decl : type_spec IDENT ';'
		s1 = newTexts.get(ctx.type_spec());
		s2 = ctx.IDENT().getText();
		if (ctx.getChildCount() == 3) {
			s2 += ctx.getChild(2).getText();
		}

		// var_decl : type_spec IDENT '=' LITERAL ';'
		if (ctx.getChildCount() == 5) {
			s2 += " " + ctx.getChild(2).getText() + " " + ctx.LITERAL().getText() + ctx.getChild(4).getText();
		}

		// var_decl : type_spec IDENT '[' LITERAL ']' ';'
		if (ctx.getChildCount() == 6) {
			s2 += ctx.getChild(2).getText() + ctx.LITERAL().getText() + ctx.getChild(4).getText()
					+ ctx.getChild(5).getText();
		}
		newTexts.put(ctx, s1 + " " + s2);
	}

	public void exitType_spec(MiniCParser.Type_specContext ctx) {
		String s1 = null;
		// type_spec : VOID
		// type_spec : INT
		s1 = ctx.getChild(0).getText();
		newTexts.put(ctx, s1);
	}

	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
		String s1 = null, s2 = null, s3 = null, s4 = null, s5 = null, s6 = null;

		// fun_decl : type_spec IDENT '(' params ')' compound_stmt
		s1 = newTexts.get(ctx.type_spec());
		s2 = ctx.IDENT().getText();
		s3 = ctx.getChild(2).getText();
		s4 = newTexts.get(ctx.params());
		s5 = ctx.getChild(4).getText();
		s6 = newTexts.get(ctx.compound_stmt());
		newTexts.put(ctx, s1 + " " + s2 + " " + s3 + s4 + s5 + "\r\n" + s6);
	}

	public void exitParams(MiniCParser.ParamsContext ctx) {
		String s1 = null, s2 = null;
		int params_size = ctx.param().size();

		// params : param ( ',' param )*
		if (params_size == 1) {
			s1 = newTexts.get(ctx.param(0));
		} else if (params_size > 1) {
			s1 = newTexts.get(ctx.param(0));
			s2 = ctx.getChild(1).getText();
			for (int i = 1; i < params_size; i++) {
				s1 += s2 + " " + newTexts.get(ctx.param(i));
			}
		} else {
			// params : VOID
			if (ctx.getChild(0) != null) {
				s1 = ctx.VOID().getText();
			} else {
				// params :
				s1 = "";
			}
		}
		newTexts.put(ctx, s1);
	}

	public void exitParam(MiniCParser.ParamContext ctx) {
		String s1 = null, s2 = null;

		// param : type_spec IDENT
		s1 = newTexts.get(ctx.type_spec());
		s2 = ctx.IDENT().getText();

		// param : type_spec IDENT '[' ']'
		if (ctx.getChildCount() > 2) {
			s2 += ctx.getChild(2).getText() + ctx.getChild(3).getText();
		}

		newTexts.put(ctx, s1 + " " + s2);
	}

	public void exitStmt(MiniCParser.StmtContext ctx) {
		String s1 = null;

		ParseTree stmt = ctx.getChild(0);
		// stmt : expr_stmt
		if (stmt instanceof MiniCParser.Expr_stmtContext) {
			s1 = dotPrint() + newTexts.get(ctx.expr_stmt());
		} else if (stmt instanceof MiniCParser.Compound_stmtContext) {
			// stmt : compound_stmt
			s1 = dotPrint() + newTexts.get(ctx.compound_stmt());
		} else if (stmt instanceof MiniCParser.If_stmtContext) {
			// stmt : if_stmt
			s1 = dotPrint() + newTexts.get(ctx.if_stmt());
		} else if (stmt instanceof MiniCParser.While_stmtContext) {
			// stmt : while_stmt
			s1 = dotPrint() + newTexts.get(ctx.while_stmt());
		} else if (stmt instanceof MiniCParser.Return_stmtContext) {
			// stmt : return_stmt
			s1 = dotPrint() + newTexts.get(ctx.return_stmt());
		}

		newTexts.put(ctx, s1);
	}

	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String s1 = null, s2 = null;

		// expr_stmt : expr ';'
		s1 = newTexts.get(ctx.expr());
		s2 = ctx.getChild(1).getText();

		newTexts.put(ctx, s1 + s2);
	}

	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		String s1 = null, s2 = null, s3 = null, s4 = null, s5 = null;

		// while_stmt : WHILE '(' expr ')' stmt
		s1 = ctx.WHILE().getText();
		s2 = ctx.getChild(1).getText();
		s3 = newTexts.get(ctx.expr());
		s4 = ctx.getChild(3).getText();
		s5 = newTexts.get(ctx.stmt());

		// {} 없을때 넣어줌
		if (!(ctx.stmt().getChild(0) instanceof MiniCParser.Compound_stmtContext)) {
			s5 = dotPrint() + "{ \r\n...." + s5 + "\r\n" + dotPrint() + "}";
		}

		newTexts.put(ctx, s1 + " " + s2 + s3 + s4 + "\r\n" + s5);
	}

	public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		this.dot_count++;
	}

	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		String s1 = null, s2 = "", s3 = "", s4 = null;
		int local_decl_size = ctx.local_decl().size();
		int stmt_size = ctx.stmt().size();

		// compound_stmt : '{' local_decl* stmt* '}'
		s1 = ctx.getChild(0).getText();

		// local_decl*
		if (local_decl_size != 0) {
			for (int i = 0; i < local_decl_size; i++) {
				s2 += dotPrint() + newTexts.get(ctx.local_decl(i)) + "\r\n";
			}
		}

		// stmt*
		if (stmt_size != 0) {
			for (int i = 0; i < stmt_size; i++) {
				s3 += newTexts.get(ctx.stmt(i)) + "\r\n";
			}
		}
		this.dot_count--; // 닫는 }

		s4 = dotPrint() + ctx.getChild(local_decl_size + stmt_size + 1).getText();
		newTexts.put(ctx, s1 + "\r\n" + s2 + s3 + s4);
	}

	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		String s1 = null, s2 = null;

		// local_decl : type_spec IDENT ';'
		s1 = newTexts.get(ctx.type_spec());
		s2 = ctx.IDENT().getText();
		if (ctx.getChildCount() == 3) {
			s2 += ctx.getChild(2).getText();
		}

		// local_decl : type_spec IDENT '=' LITERAL ';'
		if (ctx.getChildCount() == 5) {
			s2 += " " + ctx.getChild(2).getText() + " " + ctx.LITERAL().getText() + ctx.getChild(4).getText();
		}

		// local_decl : type_spec IDENT '[' LITERAL ']' ';'
		if (ctx.getChildCount() == 6) {
			s2 += ctx.getChild(2).getText() + ctx.LITERAL().getText() + ctx.getChild(4).getText()
					+ ctx.getChild(5).getText();
		}
		newTexts.put(ctx, s1 + " " + s2);
	}

	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String s1 = null, s2 = null, s3 = null, s4 = null, s5 = null, s6 = null;

		// if_stmt : IF '(' expr ')' stmt
		s1 = ctx.IF().getText();
		s2 = ctx.getChild(1).getText();
		s3 = newTexts.get(ctx.expr());
		s4 = ctx.getChild(3).getText();
		s5 = newTexts.get(ctx.stmt(0));

		if (ctx.getChildCount() == 5) {
			// {} 없을때 넣어줌 (if 의 stmt)
			if (!(ctx.stmt(0).getChild(0) instanceof MiniCParser.Compound_stmtContext)) {
				s5 = dotPrint() + "{ \r\n...." + s5 + "\r\n" + dotPrint() + "}";
			}
			newTexts.put(ctx, s1 + " " + s2 + s3 + s4 + "\r\n" + s5);
		}

		// if_stmt : IF '(' expr ')' stmt ELSE stmt
		if (ctx.getChildCount() > 5) {
			s6 = " " + ctx.ELSE().getText() + "\r\n" + newTexts.get(ctx.stmt(1));
			// {} 없을때 넣어줌 (else 의 stmt)
			if (!(ctx.stmt(1).getChild(0) instanceof MiniCParser.Compound_stmtContext)) {
				s6 = " " + ctx.ELSE().getText() + "\r\n" + dotPrint() + "{ \r\n...." + newTexts.get(ctx.stmt(1))
						+ "\r\n" + dotPrint() + "}";
			}
			newTexts.put(ctx, s1 + " " + s2 + s3 + s4 + "\r\n" + s5 + s6);
		}
	}

	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		String s1 = null, s2 = null;

		// return_stmt : RETURN ';'
		s1 = ctx.RETURN().getText();
		if (ctx.getChildCount() == 2) {
			s2 = ctx.getChild(1).getText();
		} else {
			// return_stmt : RETURN expr ';'
			s2 = " " + newTexts.get(ctx.expr()) + ctx.getChild(2).getText();
		}

		newTexts.put(ctx, s1 + s2);
	}

	public void exitExpr(MiniCParser.ExprContext ctx) {
		String s1 = null, s2 = null, op = null, s3 = null, s4 = null, s5 = null;
		int child = ctx.getChildCount();

		// expr : LITERAL
		// expr : IDENT
		if (child == 1) {
			s1 = ctx.getChild(0).getText();
			newTexts.put(ctx, s1);
		} else {
			if (child == 2) {
				// expr : 'op' expr
				if (isPreOperation(ctx)) {
					op = ctx.getChild(0).getText();
					s1 = newTexts.get(ctx.expr(0));
					newTexts.put(ctx, op + s1);
				}
			} else if (child == 3) {
				if (ctx.getChild(0).getText().equals("(")) {
					// expr : '(' expr ')'
					s1 = ctx.getChild(0).getText();
					s2 = newTexts.get(ctx.expr(0));
					s3 = ctx.getChild(2).getText();
					newTexts.put(ctx, s1 + s2 + s3);
				} else if (isBinaryOperation(ctx) && !(ctx.getChild(0) instanceof MiniCParser.ExprContext)) {
					// expr : IDENT '=' expr
					// expr : 'opop' expr (전위연산자)
					s1 = ctx.getChild(0).getText();
					op = ctx.getChild(1).getText();
					s2 = newTexts.get(ctx.expr(0));
					newTexts.put(ctx, s1 + " " + op + " " + s2);
				} else {
					// expr : expr 'op' expr
					s1 = newTexts.get(ctx.expr(0));
					op = ctx.getChild(1).getText();
					s2 = newTexts.get(ctx.expr(1));
					newTexts.put(ctx, s1 + " " + op + " " + s2);
				}
			} else if (child == 4) {
				// expr : IDENT '[' expr ']'
				s1 = ctx.IDENT().getText();
				s2 = ctx.getChild(1).getText();

				if (s2.equals("[")) {
					s3 = newTexts.get(ctx.expr(0));
				} else {
					// expr : IDENT '(' args ')'
					s3 = newTexts.get(ctx.args());
				}
				s4 = ctx.getChild(3).getText();
				newTexts.put(ctx, s1 + s2 + s3 + s4);
			} else {
				// expr : IDENT '[' expr ']' '=' expr
				s1 = ctx.IDENT().getText();
				s2 = ctx.getChild(1).getText();
				s3 = newTexts.get(ctx.expr(0));
				s4 = ctx.getChild(3).getText();
				op = ctx.getChild(4).getText();
				s5 = newTexts.get(ctx.expr(1));
				newTexts.put(ctx, s1 + s2 + s3 + s4 + " " + op + " " + s5);
			}
		}
	}

	public void exitArgs(MiniCParser.ArgsContext ctx) {
		String s1 = null, s2 = null;
		int expr_size = ctx.expr().size();

		// args : expr (',' expr)*
		if (expr_size == 1) {
			s1 = newTexts.get(ctx.expr(0));
		} else if (expr_size > 1) {
			s1 = newTexts.get(ctx.expr(0));
			s2 = ctx.getChild(1).getText();
			for (int i = 1; i < expr_size; i++) {
				s1 += s2 + " " + newTexts.get(ctx.expr(i));
			}
		} else {
			// args :
			s1 = "";
		}
		newTexts.put(ctx, s1);
	}
}
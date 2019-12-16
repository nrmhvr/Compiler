package listener.main;

import java.util.Scanner;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import generated.*;

public class Translator {
	static Scanner scan = new Scanner(System.in);

	public static void main(String[] args) throws Exception {
		CharStream codeCharStream = CharStreams.fromFileName("test.c");
		MiniCLexer lexer = new MiniCLexer(codeCharStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniCParser parser = new MiniCParser(tokens);
		ParseTree tree = parser.program();

		ParseTreeWalker walker = new ParseTreeWalker();
		int choose = 0;
		System.out.println("\n\t원하는 작업을 선택해 주세요.");
		while (choose != 4) {
			System.out.println("_____________________________________________________________");
			System.out.println("1. PrettyPrint\t 2. C to JVM\t 3. C to Ucode\t 4. end");
			System.out.print("choose : ");
			choose = scan.nextInt();
			System.out.println();

			switch (choose) {
			case 1:
				walker.walk(new MiniCPrintListener(), tree);
				break;
			case 2:
				walker.walk(new BytecodeGenListener(), tree);
				break;
			case 3:
				walker.walk(new UCodeGenListener(), tree);
				break;
			case 4:
				break;
			default:
				break;
			}
		}
	}
}
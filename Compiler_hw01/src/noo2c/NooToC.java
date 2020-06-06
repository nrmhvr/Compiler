package noo2c;

import java.io.FileWriter;
import java.io.IOException;
import noo2c.CmdExtractor.Cmds;

public class NooToC {
	CmdExtractor cmdExtractor;
	FileWriter fw;
	String nooPgm;

	// init
	public NooToC(FileWriter fw, String nooPgm) throws IOException {
		this.fw = fw;
		this.nooPgm = nooPgm;
		// 전체 input 을 CmdExtractor 생성자에게 전달
		this.cmdExtractor = new CmdExtractor(this.nooPgm);
		// 공통으로 들어가는 코드부분을 파일에 미리 저장해줌
		fw.write("#include<stdio.h>\r\n" + "int main(){\r\n" + "	int r, t1, t2, t3;\r\n");
	}

	// translate cmd to C code for each case.
	public void translate(CmdExtractor.Cmds cmd) {
		try {
			// 앞, 뒤 빼고 중간 코드부분을 재귀로 저장해줌
			translateR(cmd);
			// 코드 뒷부분을 파일에 저장해줌
			fw.write("	return 1;\r\n" + "}\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 코드의 중간부분을 write함
	public void translateR(CmdExtractor.Cmds cmd) throws IOException {
		// Cmds 종류에 따라 각 코드를 파일에 write
		switch (cmd) {
		case PRINT:
			// 뒤에꺼 먼저 실행
			translateR(next());
			// r 출력하는 c 코드
			fw.write("	print(\"%d\", r);\r\n");
			return;
		case PLUS_ONE:
			// 뒤에꺼 먼저 실행
			translateR(next());
			// + 1 해주는 c 코드
			fw.write("	t1 = r;\r\n" + "	r = t1 + 1;\r\n");
			return;
		case ZERO:
			// 뒤에꺼 없으니까 write 만 하고 return
			// r = 0 해주는 c 코드
			fw.write("	r = 0;\r\n");
			return;
		case RUNXY_RETURNY:
			// 뒤에꺼가 X, Y 두 개 이므로 재귀 두 번
			// write 하는건 따로 없음
			translateR(next()); // X 실행
			translateR(next()); // Y 실행
			return;
		case IF:
			// 뒤에꺼가 X, Y, Z 세 개 이므로 재귀 세 번
			// if 문 형식에 맞춰서 write 해줘야됨
			translateR(next()); // X 실행
			// X 실행했는데 r = 0 이 아니면 if 문 안을 실행하는 c 코드
			fw.write("	t1 = r;\r\n" + "	if( t1 != 0) { \r\n");

			// if 문 안을 채워줄 Y, 재귀를 통해 Y 를 write
			translateR(next());
			// if 문을 닫고 else 열어주는 c 코드
			fw.write("	} else { \r\n");

			// else 안을 채워줄 Z, 재귀를 통해 Z 를 write
			translateR(next());
			// else 를 닫아주는 c 코드
			fw.write("	}\r\n");
			return;
		default:
			break;
		}
	}

	// 다음 Cmds 를 가져옴
	public Cmds next() {
		return cmdExtractor.findMatch();
	}
}

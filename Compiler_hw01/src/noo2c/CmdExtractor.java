package noo2c;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// find command pattern from input, and convert it to cmd
// CmdExtractor는 input으로부터 CMD pattern을 찾아 parsing
public class CmdExtractor {
	public enum Cmds {
		// pattern
		PRINT("'\""), PLUS_ONE("'\"\""), ZERO("'\"\"\""), RUNXY_RETURNY("'\"\"\"\""), IF("'\"\"\"\"\"");

		private String matchedStr;

		// 생성자 역할
		Cmds(String str) {
			this.matchedStr = str;
		}

		// getter
		// ex) Cmds.PRINT.getCmds = '"
		String getCmds() {
			return this.matchedStr;
		}
	}

	// '하나 "하나이상 오는 패턴(모든 Cmds 의 공통부분)
	Pattern p = Pattern.compile("'\"+");
	Matcher m = null;

	// 패턴에 따른 Cmds 를 저장하는 HashMap
	// ex) id: '" , value: PRINT
	private static HashMap<String, Cmds> hmap = new HashMap<>();
	{
		hmap.put(Cmds.PRINT.getCmds(), Cmds.PRINT);
		hmap.put(Cmds.PLUS_ONE.getCmds(), Cmds.PLUS_ONE);
		hmap.put(Cmds.ZERO.getCmds(), Cmds.ZERO);
		hmap.put(Cmds.RUNXY_RETURNY.getCmds(), Cmds.RUNXY_RETURNY);
		hmap.put(Cmds.IF.getCmds(), Cmds.IF);
	}

	// 생성자
	// 전체 input 을 받아서 Matcher 로 '"+ 패턴들을 찾기
	// 찾아진 패턴들은 m.group() 임
	public CmdExtractor(String nooPgm) {
		m = p.matcher(nooPgm);
	}

	// input 에 패턴이 있다면 해당하는 enum(Cmds) 을 반환해줌
	public Cmds findMatch() {
		// 패턴이 있으면 true, 아니면 false
		if (m.find()) {
			// 패턴에 따른 Cmds 를 HashMap에서 찾아서 반환
			return hmap.get(m.group());
		}
		return null;
	}
}

package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryGenerator {

	List<Byte> mem = new ArrayList<Byte>();
	Map<String, Integer> VarMap = new HashMap<String, Integer>();
	Map<String, Integer> FuncMap = new HashMap<String, Integer>();
	// Armazena funções não encontradas e o endereço de onde ficará
	Map<Integer, String> FuncLocation = new HashMap<Integer, String>();
	Map<String, Byte> InstMap = new HashMap<String, Byte>();
	private int cpp = 6;
	private int lv = 4097;
	private int pc = 1024;
	private int sp = 4097;

	static BinaryGenerator bg = null;

	private BinaryGenerator() {
		InstMap.put("nop", (byte) 0x01);
		InstMap.put("iadd", (byte) 0x02);
		InstMap.put("isub", (byte) 0x05);
		InstMap.put("iand", (byte) 0x08);
		InstMap.put("ior", (byte) 0x0B);
		InstMap.put("dup", (byte) 0x0E);
		InstMap.put("pop", (byte) 0x10);
		InstMap.put("swap", (byte) 0x13);
		InstMap.put("bipush", (byte) 0x19);
		InstMap.put("iload", (byte) 0x1C);
		InstMap.put("istore", (byte) 0x22);
		InstMap.put("ldc_w", (byte) 0x32);
		InstMap.put("iinc", (byte) 0x36);
		InstMap.put("goto", (byte) 0x3C);
		InstMap.put("iflt", (byte) 0x43);
		InstMap.put("ifeq", (byte) 0x47);
		InstMap.put("if_icmpeq", (byte) 0x4B);
		InstMap.put("invokevirtual", (byte) 0x55);
		InstMap.put("ireturn", (byte) 0x6B);
		/*
		 * nop 0x01 iadd 0x02 isub 0x05 iand 0x08 ior 0x0B dup 0x0E pop 0x10 swap 0x13
		 * bipush 0x19 iload 0x1C istore 0x22 wide 0x28 ldc_w 0x32 iinc 0x36 goto 0x3C
		 * iflt 0x43 ifeq 0x47 if_icmpeq 0x4B invokevirtual 0x55 ireturn 0x6B
		 */
	}

	public static BinaryGenerator getInstance() {
		if (bg == null) {
			bg = new BinaryGenerator();
		}
		return bg;
	}

	int byteCount = 0;
	int varnum = 0;

	public byte[] mount() {
		byte[] init = new byte[20];
		init[0] = 0;
		init[1] = 0x73;
		init[2] = 00;
		init[3] = 00;
		for (int i = 0; i < 4; i++) {
			init[4 + i] = (byte) (cpp & 0xff);
			cpp >>>= 8;
		}
		for (int i = 0; i < 4; i++) {
			init[8 + i] = (byte) (lv & 0xff);
			lv >>>= 8;
		}
		for (int i = 0; i < 4; i++) {
			init[12 + i] = (byte) (pc & 0xff);
			pc >>>= 8;
		}
		sp += varnum;
		for (int i = 0; i < 4; i++) {
			init[16 + i] = (byte) (sp & 0xff);
			sp >>>= 8;
		}

		int qInt = 20 + byteCount;
		byte[] q = new byte[4];
		for (int i = 0; i < 4; i++) {
			q[i] = (byte) (qInt & 0xff);
			qInt >>>= 8;
		}

		Byte[] memoryO = mem.toArray(new Byte[1]);

		byte memory[] = new byte[memoryO.length];
		for (int j = 0; j < memoryO.length; j++) {

			if (!FuncLocation.containsKey(j)) {
				System.out.println("J" + j + "MemLen" + memoryO.length);
				memory[j] =  memoryO[j];
			} else {
				if (FuncMap.containsKey(FuncLocation.get(j))) {
					int val = FuncMap.get(FuncLocation.get(j)) - j;
					memory[j++] = (byte) ((val & 0xff00) >> 8);
					memory[j] = (byte) (val & 0xff);

				} else {

					System.err.println("Undefined Function");
					System.exit(-1);
				}
			}
		}
		System.out.println("header length " + q.length);
		System.out.println("init length " + init.length);
		System.out.println("binary length " + memory.length);
//		for(byte a :memory) {
//			System.out.print(Integer.toHexString(a&0xff)+" ");
//		}
		byte[] binary = new byte[memory.length + q.length + init.length];
		for (int i = 0; i < q.length; i++) {
			binary[i] = q[i];
		}
		for (int i = 0; i < init.length; i++) {
			binary[i + 4] = init[i];
		}
		for (int i = 0; i < memory.length; i++) {
			binary[i + init.length + q.length] = memory[i];
		}
//		System.arraycopy(q, 0, binary, 0, 4);
//		System.arraycopy(init, 0, binary, 4, 20);
//		System.arraycopy(memory, 0, binary, 24, memory.length);
		return binary;
	}

	public void decode(String memruction) {
		int len = memruction.trim().split("\\s+").length;
		// Tamanho da instrução em numero de argumentos(Não em numero de bytes)
		String[] s = memruction.trim().split("\\s+");
//		for (String f : s) {
//			System.out.println(f);
//		}
		if (len == 1) {
			mem.add(InstMap.get(s[0]));
			byteCount++;
		}

		if (len == 2) {
			if (!InstMap.containsKey(s[0])) {
				mem.add(InstMap.get(s[1]));
				byteCount++;
				FuncMap.put(s[0], byteCount);
			} else {
				mem.add(InstMap.get(s[0]));
				int var = 0;
				switch (InstMap.get(s[0])) {
				case 0x19:// bipush
					mem.add((byte) (int) (Integer.valueOf(s[1]) & 0xff));
					byteCount += 2;
					break;
				case 0x3c:
				case 0x43:
				case 0x47:
				case 0x4B:// goto//ifeq//iflq
					var = (mem.size());
					FuncLocation.put(var, s[1]);
					mem.add((byte) 0);
					mem.add((byte) 0);
					byteCount += 3;
					break;
				case 0x22:// istore
					if (!VarMap.containsKey(s[1])) {
						VarMap.put(s[1], varnum);
						mem.add((byte) varnum++);
					} else {
						var = VarMap.get(s[1]);
						mem.add((byte) var);
					}
					byteCount += 2;
					break;
				case 0x1C:// iload
					var = VarMap.get(s[1]);
					mem.add((byte) var);
					byteCount += 2;
					break;
				case 0x55:// invokeVirtual
					var = Integer.valueOf(s[1]);
					mem.add((byte) ((var & 0xff)));
					mem.add((byte) ((var & 0xff00) >> 8));
					byteCount += 3;
					break;
				case 0x32:// ldc_w
					var = Integer.valueOf(s[1]);
					mem.add((byte) ((var & 0xff)));
					mem.add((byte) ((var & 0xff00) >> 8));
					byteCount += 3;
					break;

				}
			}
		}
		if (len == 3) {
			if (!InstMap.containsKey(s[0])) {
//				mem.add(InstMap.get(s[1]));
//				byteCount++;
				int oldBcount = byteCount;
				decode(s[1] + " " + s[2]);
				FuncMap.put(s[0], ++oldBcount);
			} else {
				int value;
				mem.add(InstMap.get(s[0]));
				switch (InstMap.get(s[0])) {
				case 0x36:
					mem.add((byte) (int) VarMap.get(s[1]));
					value = Integer.valueOf(s[2]);
					mem.add((byte) (value & 0xff));
					byteCount+=3;
					break;
				}
			}
		}
		if (len == 4) {
			int oldBcount = byteCount;
			decode(s[1] + " " + s[2] + " " + " " + s[3]);
			FuncMap.put(s[0], ++oldBcount);
		}

	}

	// System.out.println();

}

package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;

public class Assembler {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFileChooser fileChooser = new JFileChooser();
		int returnValue = fileChooser.showOpenDialog(fileChooser);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();

			handleFile(file);
		}

	}

	public static void handleFile(File file) {
		// BufferedReader br;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				BinaryGenerator.getInstance().decode(st);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] rom = BinaryGenerator.getInstance().mount();
		for (byte a : rom) {
			System.out.print(Integer.toHexString(a & (0xff)).toUpperCase() + " ");
		}
		String PATH = "";

		// String s = file.getName().split("\\.")[0];

		// System.out.println(s);
		// s = s + ".bin";
		PATH = file.getPath().replaceAll(file.getName(), "program.bin");
		// System.out.println(PATH);
		File outFile = new File(PATH);
		try {
			OutputStream os = new FileOutputStream(outFile);
			os.write(rom);
			System.out.println("The bin has been saved succesfully at: \n" + PATH);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// int returnValue = fileChooser.showOpenDialog(fileChooser);

	}

}
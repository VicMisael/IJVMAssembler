package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Assembler {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (args.length == 0) {
			JFileChooser fileChooser = new JFileChooser();
			try {
				fileChooser = new JFileChooser((Assembler.class.getProtectionDomain().getCodeSource().getLocation()
						.toURI().getPath().toString()));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();

				handleFile(file);
			}

		} else if (args.length == 1) {
			File f = Paths.get(args[0]).toFile();
			handleFile(f);
		}

	}

	public static void handleFile(File file) {
		// BufferedReader br;
		long time = System.currentTimeMillis();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				BinaryGenerator.getInstance().decode(st);
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo não encontrado");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] rom = BinaryGenerator.getInstance().mount();

//		int j = 0;
//		System.out.println("Init bytes");
//		while (j < 24) {
//			System.out.print(Integer.toHexString(rom[j] & 0xff).toUpperCase() + " ");
//			j++;
//		}
//		System.out.println("\n Program");
//		for (j = 24; j < rom.length; j++) {
//			System.out.print(Integer.toHexString(rom[j] & 0xff).toUpperCase() + " ");
//		}	
//		System.out.println();

		String PATH = "";
		PATH = file.getPath().replaceAll(file.getName(), "program.bin");

		File outFile = new File(PATH);
		try {
			OutputStream os = new FileOutputStream(outFile);
			os.write(rom);
			System.out.println("The bin has been saved succesfully at: \n" + PATH);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Assembled in : " + (System.currentTimeMillis() - time) + "ms");

	}

}

package DataProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class DurationsProcessing {

	public static void main(String[] args) {
		ArrayList<Validation> validations = new ArrayList<>();
		BufferedReader bf;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data_processing/durations/InfoForDurations.csv")), "UTF-8"));
			String line;
			line = bf.readLine();
			while (line!=null){
				line = bf.readLine();
				if (line == null) {
					break;
				} else {
					String[] parts = line.split(",");
					int badge = Integer.parseInt(parts[0]);
					long segundo = Integer.parseInt(parts[2]);
					int mes = Integer.parseInt(parts[3]);
					int dia = Integer.parseInt(parts[4]);
					int periodo = Integer.parseInt(parts[5]);
					String rol = parts[6];
					String tipo = parts[7];
					Validation v = new Validation(mes, dia, badge, tipo, rol, segundo, periodo);
					validations.add(v);
				}
			}
			bf.close();
			System.out.println(validations.size());
			Collections.sort(validations);

			PrintWriter pw;
//			PrintWriter pw = new PrintWriter("./data_processing/InfoForDurationsSorted.csv");
//			pw.println("Badge,mes,dia,rol,segundo,periodo,tipo");
//			for (Iterator<Validation> iterator = validations.iterator(); iterator.hasNext();) {
//				Validation v = (Validation) iterator.next();
//				pw.println(v.badge + "," + v.mes + "," + v.dia + "," + v.rol + "," + v.segundo + "," + v.periodo + "," + v.tipo);
//			}
//			pw.close();
//			System.out.println("ready");

			DecimalFormat formato1 = new DecimalFormat("#.00");
			ArrayList<SingleDuration> duraciones = new ArrayList<>();
			pw = new PrintWriter("./data/simulation/durations.csv");
			pw.println("mes,dia,periodo,rol,duración");
			for (int i = 0; i < validations.size()-1; i++) {
				Validation a = validations.get(i);
				Validation b = validations.get(i+1);

				if (a.badge == b.badge && a.mes == b.mes && a.dia == b.dia && a.tipo.equals("IN") && b.tipo.equals("OUT")) {
					double mins = Double.parseDouble(formato1.format((b.segundo - a.segundo)/60.0));
					if (mins>=2.0) {						
						SingleDuration d = new SingleDuration(a.mes, a.dia, a.periodo, a.rol, mins);
						duraciones.add(d);
						pw.println(d.mes + "," + d.dia + "," + d.periodo + "," + d.rol + "," + d.duracion);
					}
				}
			}
			pw.close();
			System.out.println("ready");

		} catch (Exception e) {
			e.printStackTrace();
		} 

	}



}

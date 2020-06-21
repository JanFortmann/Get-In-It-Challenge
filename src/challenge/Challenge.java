package challenge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author jan
 *
 */
public class Challenge {

	/**
	 * In der Main werden die Methoden zum Einlesen der Standorte, zum  Berechnen der Entfernung, 
	 * zum Berechnen des kürzesten Weges und zum Schreiben des kürzesten Weges in eine neue
	 * Datei nacheinander ausgeführt.
	 * 
	 * @param standorte : Liste mit allen Standorten aus der gegebenen CSV-Datei
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		List<Standort> standorte = readCSV("msg_standorte_deutschland.csv");
		
		double[][] distanceMatrix = getDistance(standorte);
		
		TSP tspInst = new TSP();
		tspInst.tsp(distanceMatrix);
		
		double totalDistance = 0.0;
		List<Integer> order = tspInst.getOrder();
		
		for(int i = 0; i < order.size() - 1; i++) {
			totalDistance += distanceMatrix[i][i + 1];
		}
		
		/*
		 * Laut Quelle "http://kartenkunde-leichtgemacht.de/handbuch.php?page=Koordinatensysteme"
		 * beträgt der Durchschnittliche Abstand der Längen- und Breitengrade in Deutschland ca.
		 * 71 km.
		 */
		totalDistance *= 71;
		
		FileWriter csvWriter = new FileWriter("result.txt");
		csvWriter.append("Gesamtstrecke: " + totalDistance + "km\nReihenfolge: \n");
		for(int i : order) {
			csvWriter.append(standorte.get(i - 1).standort + ", \n");
		}
		csvWriter.flush();
		csvWriter.close();
	}
	
	/**
	 * Liest aus der CSV-Datei die Standorte ein
	 * 
	 * @param dateiname : Name der Datei, aus der die Standorte gelesen werden
	 * @return standorte : eingelesene Standorte aus Datei
	 */
	private static List<Standort> readCSV(String dateiname) {
		List<Standort> standorte = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(dateiname))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] values = line.split(",");
		    	for(int i = 0; i < values.length; i++) {
		    		values[i] = values[i].replace(';', ' ');
		    	}
		    	
		    	standorte.add(new Standort(Integer.parseInt(values[0]), values[1], values[2], 
		    			values[3], Integer.parseInt(values[4]), values[5], Double.parseDouble(values[6]),
		    			Double.parseDouble(values[7])));
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return standorte;
	}
	
	/**
	 * Berechnet mit Pythagoras alle Abstände zwischen den Städten und speichert diese in einer Adjazenzmatrix.
	 * Für den Abstand von Stadt i nach Stadt i gebe 0 zurück.
	 * 
	 * @param standorte : Liste aller Standorte, deren Abstände berechnet werden sollen
	 * @return Adjazenzmatrix mit allen Abständen
	 */
	private static double[][] getDistance(List<Standort> standorte) {
		double[][] distanceMatrix = new double[standorte.size()][standorte.size()];
		
		for(int i = 0; i < standorte.size(); i++) {
			for(int j = i + 1; j < standorte.size(); j++) {
				/* Abstandsberechnung mit Pythagoras; Weltweit auf ca. 20km genau, da aufgrund der 
				* unterschiedlichen Erdkruemmung nicht ueberall jeder Laengen- und Breitengrad gleich 
				* weit auseinander sind. In Deutschland ist dies aber zu vernachlässigen
				*/
				distanceMatrix[i][j] = Math.sqrt(Math.pow(standorte.get(i).breitengrad - standorte.get(j).breitengrad, 2)
						+ Math.pow(standorte.get(i).laengengrad - standorte.get(j).laengengrad, 2));
				distanceMatrix[j][i] = distanceMatrix[i][j];
			}
		}
		
		return distanceMatrix;
	}

	
}

/**
 * @author jan
 *
 */
class Standort {
	
	public int 		nummer = 0, plz = 0; 
	public String 	standort = "", strasse = "", hausnummer = "", ort = "";
	public double 	breitengrad = 0.0, laengengrad = 0.0;
	
	/**
	 * Der Konstruktor erstellt ein Standort-Objekt mit gegebenen Werten. 
	 * Strasse, Hausnummer, PLZ und Ort werden eigentlich nicht benötigt. 
	 */
	public Standort(int nummer, String standort, String strasse, String hausnummer, 
			int plz, String ort, double breitengrad, double laengengrad) {
		this.nummer = nummer;
		this.standort = standort;
		this.strasse = strasse;
		this.hausnummer = hausnummer;
		this.plz = plz;
		this.ort = ort;
		this.breitengrad = breitengrad;
		this.laengengrad = laengengrad;
	}
}

/**
 * Klasse, um kürzeste Rundwege zu berechnen. Erweiterung des unter Quelle:
 * https://www.sanfoundry.com/java-program-implement-traveling-salesman-problem-using-nearest-neighbour-algorithm/
 * vorgestellten Algorithmus mit Nearest Neighbour Heuristik
 * 
 * @author jan
 *
 */
class TSP {
    private int numberOfNodes;
    private Stack<Integer> stack;
    private List<Integer> list;
 
    public TSP() {
        stack = new Stack<Integer>();
        list = new ArrayList<Integer>();
    }
 
    public void tsp(double distanceMatrix[][]) {
        numberOfNodes = distanceMatrix[1].length - 1;
        boolean[] visited = new boolean[numberOfNodes + 1];
        visited[1] = true;
        stack.push(1);
        int i, element, dst = 0;
        double min = Double.MAX_VALUE;
        boolean minFlag = false;
        list.add(1);
 
        while (!stack.isEmpty()) {
            element = stack.peek();
            i = 1;
            min = Double.MAX_VALUE;
            while (i <= numberOfNodes) {
                if (distanceMatrix[element][i] > 1 && !visited[i]) {
                    if (min > distanceMatrix[element][i]) {
                        min = distanceMatrix[element][i];
                        dst = i;
                        minFlag = true;
                    }
                }
                i++;
            }
            if (minFlag) {
                visited[dst] = true;
                stack.push(dst);
                list.add(dst);
                minFlag = false;
                continue;
            }
            stack.pop();
        }
        list.add(1);
    }

    public List<Integer> getOrder() {
    	return this.list;
    }
}

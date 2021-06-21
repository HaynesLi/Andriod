package com.vrp.app.wrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import com.vrp.app.components.Node;

public class Utils {
	
	private static Random random = new Random();
	
	public static String generateId() {
		return String.valueOf(Math.abs(random.nextInt())).substring(0, 6);
	}
	
	public static float distanceBetween(Location start, Location end) {
		float dX = end.getPosX() - start.getPosX();
		float dY = end.getPosY() - start.getPosY();
		return (float) Math.sqrt(dX * dX + dY * dY);
	}
	
	public static float distanceBetween(Node start, Node end) {
		float dX = end.getX() - start.getX();
		float dY = end.getY() - start.getY();
		return (float) Math.sqrt(dX * dX + dY * dY);
	}

	public static boolean checkArrayBounds(int i, int upperBound) {
		if(i < 0 || i >= upperBound) {
			throw new ArrayIndexOutOfBoundsException(
					String.format("Index %d out of bounds for [0,%d]", i, upperBound)
			);
		}
		return true;
	}
	
	public static void writeToFile(String filename, String content) {
		try {
			File file = new File(filename);
			File directory = file.getParentFile();
			if(!directory.exists()) {
				System.out.printf("Creating parent directory: %s\n", directory.getAbsolutePath());
				directory.mkdirs();
			}
		    FileWriter fileWriter = new FileWriter(file);
		    PrintWriter printWriter = new PrintWriter(fileWriter);
			System.out.println("Writing to file: " + file.getAbsolutePath());
		    printWriter.print(content);
		    printWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String formatNanos(long nanoSeconds) {
		long millis = (nanoSeconds / 1000000);
		long seconds = millis / (1000);
		long minute = seconds / 60;
		
		if(minute > 0) {
			return String.format("%dm %d.%03ds", minute, seconds % 60, millis % 1000);
		} else {
			return String.format("%d.%03ds", seconds % 60, millis % 1000);
		}

	}
	
	public static void assertTrue(boolean condition, String message) {
		assertTrue(condition, message, false);
	}
	
	public static void assertTrue(boolean condition, String message, boolean fatal) {
		if(!condition) {
			assertFail(message, fatal);
		}
	}
	
	public static void assertFail(String message, boolean fatal) {
		System.out.printf("ERROR: %s\n", message);
		if(fatal) {
			System.out.println("Exiting.");
			System.exit(1);
		}
	}
}

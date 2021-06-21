package com.vrp.app.wrapper;

public class Args {
	
	public int numVehicles;
	public int numLocations;
	public boolean noSave;
	
	public Args(int numVehicles, int numLocations, boolean noSave) {
		this.numVehicles = numVehicles;
		this.numLocations = numLocations;
		this.noSave = noSave;
	}


	public static Args parse(String[] args) {
		if (2 > args.length || args.length > 3) {
			System.out.printf("Expected 2-3 arguments, got %d. Usage:\n\t<number of vehicles> <number of locations> [optional: nosave]\n", args.length);
			System.exit(1);
		}

		int numVehicles = safeParseInt(args[0], "numVehicles");
		int numLocations = safeParseInt(args[1], "numLocations");
		boolean noSave = args.length == 3 && args[2].toLowerCase().equals("nosave");

		Utils.assertTrue(1 <= numVehicles && numVehicles <= 20, "Parameter numVehicles has to be in range [1, 20]", true);
		Utils.assertTrue(1 <= numLocations && numLocations <= 10000, "Parameter numLocations has to be in range [1, 10000]", true);
		
		return new Args(numVehicles, numLocations, noSave);
	}
	
	private static int safeParseInt(String in, String name) {
		int res = -1;
		try {
			res = Integer.parseInt(in);
		} catch(NumberFormatException ex) {
			Utils.assertFail(String.format("Parameter %s could not be parsed to integer!", name), true);
		}
		return res;
	}

}

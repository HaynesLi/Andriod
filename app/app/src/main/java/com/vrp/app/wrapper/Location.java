package com.vrp.app.wrapper;

import java.util.Scanner;

public class Location {
	
	private float posX;
	private float posY;
	
	public Location(float x, float y) {
		this.posX = x;
		this.posY = y;
	}
	
	public static Location parseFrom(Scanner scanner) {
		float x = scanner.nextFloat();
		float y = scanner.nextFloat();
		return new Location(x, y);
	}
	
	public float getPosX() {
		return posX;
	}

	public void setPosX(float posX) {
		this.posX = posX;
	}

	public float getPosY() {
		return posY;
	}

	public void setPosY(float posY) {
		this.posY = posY;
	}
}

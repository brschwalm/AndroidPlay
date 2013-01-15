package com.anythinksolutions.cannongame;

import android.graphics.Point;


public class VectorMotion{
	
	public int velocityX = 0;
	public int velocityY = 0;
	public Point target = new Point(0,0);
	
	public VectorMotion(){
		
	}
	
	public VectorMotion(int xVelocity, int yVelocity){
		this.start(xVelocity, yVelocity);
	}
	
	public Point increment(double ms){
		double duration = ms/1000;
		
		target.x += (this.velocityX * duration);
		target.y += (this.velocityY * duration);
		
		return target;
	}
	
	public Point decrement(double ms){
		double duration = ms/1000.0;
		
		target.x -= (this.velocityX * duration);
		target.y -= (this.velocityY * duration);
		
		return target;
	}
	
	public void stop(){
		this.velocityX = 0;
		this.velocityY = 0;
	}
	
	public void start(int xVelocity, int yVelocity){
		this.velocityX = xVelocity;
		this.velocityY = yVelocity;
	}
	
	public void startHorizontal(double angle, int speed){
		this.velocityX = (int)(speed * Math.sin(angle));
		this.velocityY = (int)(-speed * Math.cos(angle));
	}
	public void startVertical(double angle, int speed){
		this.velocityX = (int)(-speed * Math.sin(angle));
		this.velocityY = (int)(speed * Math.cos(angle));
	}

	public void reverseX(){
		this.velocityX *= -1;
	}
	
	public void reverseY(){
		this.velocityY *= -1;
	}
	
	public void reverse(){
		this.reverseX();
		this.reverseY();
	}
}

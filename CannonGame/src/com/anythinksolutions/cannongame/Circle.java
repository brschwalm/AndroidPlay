package com.anythinksolutions.cannongame;

import android.graphics.Point;
import android.graphics.Rect;

public class Circle extends Point {

	public int radius = 1;
	public VectorMotion motion = new VectorMotion();
	
	public Circle(){
		
	}
	public Circle(Point center, int radius){
		this.x = center.x;
		this.y = center.y;
		this.radius = radius;
	}
	
	public Circle(int x, int y, int radius){
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	public int left(){
		return this.x - this.radius;
	}
	
	public int right(){
		return this.x + this.radius;
	}
	
	public int top(){
		return this.y - this.radius;
	}
	
	public int bottom(){
		return this.y + this.radius;
	}
	
	public void center(int x, int y){
		this.x = x; this.y = y;
	}
	
	public Circle move(int xDelta, int yDelta){
		this.offset(xDelta, yDelta);
		return this;
	}
	
	public boolean collideWithLine(int distance, Line candidate){
		return this.right() > distance &&
				this.left() < distance &&
				this.top() > candidate.start.y &&
				this.bottom() < candidate.end.y;
	}

	public boolean inBounds(Rect bounds){
		return this.left() >= 0 && this.right() <= bounds.right && this.top() >= 0 && this.bottom() <= bounds.bottom;
	}
}

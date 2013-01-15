package com.anythinksolutions.cannongame;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback{

	private CannonThread cannonThread;
	private Activity activity;
	private boolean dialogIsDisplayed = false;
	
	public static final int TARGET_PIECES = 7;
	public static final int MISS_PENALTY = 2;
	public static final int HIT_REWARD = 3;
	
	private boolean gameOver;
	private double timeLeft;
	private int shotsFired;
	private double totalTimeElapsed;
	
	//Variables for the blocker and target
	private Line blocker;
	private int blockerDistance;
	private int blockerBeginning;
	private int blockerEnd;
	private int initialBlockerVelocity;
	private float blockerVelocity;
	
	private Line target;
	private int targetDistance;
	private int targetBeginning;
	private double pieceLength;
	private int targetEnd;
	private int initialTargetVelocity;
	private float targetVelocity;
	
	private int lineWidth;
	private boolean[] hitStates;
	private int targetPiecesHit;
	
	//variables for the cannon and cannonball
	private Point cannonball;
	//private Circle cannonballCircle;
	private int cannonballVelocityX;
	private int cannonballVelocityY;
	private boolean cannonballOnScreen;
	private int cannonballRadius;
	private int cannonballSpeed;
	private int cannonBaseRadius;
	private int cannonLength;
	private Point barrelEnd;
	
	private int screenWidth;
	private int screenHeight;
	private Rect screenBounds;
	
	//Constants and variables for managing sounds
	private static final int TARGET_SOUND_ID = 0;
	private static final int CANNON_SOUND_ID = 1;
	private static final int BLOCKER_SOUND_ID = 2;
	private SoundPool soundPool;
	private Map<Integer, Integer> soundMap;
	
	//Paint variables used to draw
	private Paint textPaint;
	private Paint cannonballPaint;
	private Paint cannonPaint;
	private Paint blockerPaint;
	private Paint targetPaint;
	private Paint backgroundPaint;
	
	public CannonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (Activity)context;
		
		getHolder().addCallback(this);	//register the SurfaceHolder.Callback listener
		
		//initialize the lines and points for game items
		blocker = new Line();
		target = new Line();
		cannonball = new Point();
		//cannonballCircle = new Circle();
		
		//initiate hitstates
		hitStates = new boolean[TARGET_PIECES];
		
		//Setup sound
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap = new HashMap<Integer, Integer>();
		soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
		soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
		soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));
		
		//Paints for drawing
		textPaint = new Paint();
		cannonPaint = new Paint();
		cannonballPaint = new Paint();
		blockerPaint = new Paint();
		targetPaint = new Paint();
		backgroundPaint = new Paint();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		screenWidth = w;
		screenHeight = h;
		//screenBounds = new Rect(0, 0, w, h);
		
		cannonBaseRadius = h/18;
		cannonLength = w/8;		
		cannonballRadius = w/36;
		cannonballSpeed = w * 3 / 2;
		lineWidth = w/24;
		
		//cannonballCircle.radius = cannonballRadius;
		
		blockerDistance = w * 5 / 8;
		blockerBeginning = h / 8;
		blockerEnd = h * 3 / 8;
		initialBlockerVelocity = h/2;
		blocker.start = new Point(blockerDistance, blockerBeginning);
		blocker.end = new Point(blockerDistance, blockerEnd);
		
		targetDistance = w * 7 / 8;
		targetBeginning = h / 8;
		targetEnd = h * 7 / 8;
		pieceLength = (targetEnd - targetBeginning) / TARGET_PIECES;
		initialTargetVelocity = -h/4;
		target.start = new Point(targetDistance, targetBeginning);
		target.end = new Point(targetDistance, targetEnd);
		
		barrelEnd = new Point(cannonLength, h/2);
		
		textPaint.setTextSize(w/20);
		textPaint.setAntiAlias(true);
		cannonPaint.setStrokeWidth(lineWidth * 1.5f);
		blockerPaint.setStrokeWidth(lineWidth);
		targetPaint.setStrokeWidth(lineWidth);
		backgroundPaint.setColor(Color.WHITE);
		
		newGame();
	}
	
	public void newGame(){
		for(int i = 0; i < TARGET_PIECES; ++i){
			hitStates[i] = false;
		}
		
		targetPiecesHit = 0;
		blockerVelocity = initialBlockerVelocity;
		targetVelocity = initialTargetVelocity;
		timeLeft = 20;
		cannonballOnScreen = false;
		shotsFired = 0;
		totalTimeElapsed = 0.0;
		blocker.start.set(blockerDistance, blockerBeginning);
		blocker.end.set(blockerDistance, blockerEnd);
		target.start.set(targetDistance, targetBeginning);
		target.end.set(targetDistance, targetEnd);
		
		if(gameOver){
			gameOver = false;
			cannonThread = new CannonThread(getHolder());
			cannonThread.start();
		}
	}

	public void stopGame(){
		if(cannonThread != null)
			cannonThread.setRunning(false);
	}
	
	public void releaseResources(){
		soundPool.release();		//releases all resources used by the soundpool
		soundPool = null;
	}
	
	private void updatePositions(double elapsedTimeMS){
		double interval = elapsedTimeMS / 1000.0;	//convert to seconds
		
		if(cannonballOnScreen){
			cannonball.x += interval * cannonballVelocityX;
			cannonball.y += interval * cannonballVelocityY;
			//cannonballCircle.motion.increment(interval);
			
			//Check for collision with the blocker
//			boolean blockerCollide = cannonballCircle.collideWithLine(blockerDistance, blocker);
//			boolean targetCollide = cannonballCircle.collideWithLine(targetDistance, target);
//			boolean inBounds = cannonballCircle.inBounds(screenBounds);
//			
			if(cannonball.x + cannonballRadius > blockerDistance &&
					cannonball.x - cannonballRadius < blockerDistance &&
					cannonball.y + cannonballRadius > blocker.start.y &&
					cannonball.y - cannonballRadius < blocker.end.y){
				
				cannonballVelocityX *= -1;		//reverse direction of cannonball
				//cannonballCircle.motion.reverseX();
				
				timeLeft -= MISS_PENALTY;
				soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1, 0, 1, 1f);
			}
			
			//Check for a collision with the left and right walls
			else if(cannonball.x + cannonballRadius > screenWidth ||
					cannonball.x - cannonballRadius < 0){
				cannonballOnScreen = false;
			}
			
			//Check for a collision with top or bottom walls
			else if(cannonball.y + cannonballRadius > screenHeight ||
					cannonball.y - cannonballRadius < 0){
				cannonballOnScreen = false;
			}
			
			//check for a collision with target
			else if(cannonball.x + cannonballRadius > targetDistance &&
						cannonball.x - cannonballRadius < targetDistance &&
						cannonball.y + cannonballRadius > target.start.y &&
						cannonball.y - cannonballRadius < target.end.y){
				
				int section = (int)((cannonball.y - target.start.y)/pieceLength);
				if((section >= 0 && section < TARGET_PIECES) && !hitStates[section]){
					hitStates[section] = true;
					cannonballOnScreen = false;
					timeLeft += HIT_REWARD;
					soundPool.play(soundMap.get(TARGET_SOUND_ID), 1, 1, 0, 1, 1f);
					
					//check to see if all pieces are gone
					if(++targetPiecesHit == TARGET_PIECES){
						cannonThread.setRunning(false);
						showGameOverDialog(R.string.win);
						gameOver = true;
					}
				}
			}				
		}
		
		//update blocker's position
		double blockerUpdate = interval * blockerVelocity;
		blocker.start.y += blockerUpdate;
		blocker.end.y += blockerUpdate;
		
		double targetUpdate = interval * targetVelocity;
		target.start.y += targetUpdate;
		target.end.y += targetUpdate;
		
		if(blocker.start.y < 0 || blocker.end.y > screenHeight)
			blockerVelocity *= -1;
		if(target.start.y < 0 || target.end.y > screenHeight)
			targetVelocity *= -1;
		
		timeLeft -= interval;		
		if(timeLeft <= 0){
			timeLeft = 0.0;
			gameOver = true;
			cannonThread.setRunning(false);
			showGameOverDialog(R.string.lose);
		}
	}
	
	public void fireCannonball(MotionEvent event){
		if(cannonballOnScreen)
			return;
		
		double angle = alignCannon(event);
		cannonball.x = cannonballRadius;
		cannonball.y = screenHeight / 2;
		//cannonballCircle.center(cannonballRadius, screenHeight/2);
		
		cannonballVelocityX = (int)(cannonballSpeed * Math.sin(angle));
		cannonballVelocityY = (int)(-cannonballSpeed * Math.cos(angle));
		//cannonballCircle.motion.startHorizontal(angle, cannonballSpeed);
		
		cannonballOnScreen = true;
		++shotsFired;
		
		soundPool.play(soundMap.get(CANNON_SOUND_ID), 1, 1, 1, 0, 1f);		
	}
	
	public double alignCannon(MotionEvent event){
		
		Point touchPoint = new Point((int) event.getX(), (int)event.getY());
		double centerMinusY = (screenHeight/2 - touchPoint.y);
		double angle = 0;
		
		if(centerMinusY != 0)
			angle = Math.atan((double) touchPoint.x / centerMinusY);
		
		if(touchPoint.y > screenHeight / 2)
			angle += Math.PI;
		
		barrelEnd.x = (int)(cannonLength * Math.sin(angle));
		barrelEnd.y = (int)(-cannonLength * Math.cos(angle) + screenHeight / 2);
		
		return angle;
	}
	
	public void drawGameElements(Canvas canvas){
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
		canvas.drawText(getResources().getString(R.string.time_remaining_format, timeLeft), 30, 50, textPaint);
		
		//Cannonball
		if(cannonballOnScreen)
			canvas.drawCircle(cannonball.x, cannonball.y, cannonballRadius, cannonballPaint);
		//Cannon
		canvas.drawLine(0, screenHeight / 2, barrelEnd.x, barrelEnd.y, cannonPaint);
		//Cannon base
		canvas.drawCircle(0, (int)screenHeight/2, (int)cannonBaseRadius, cannonPaint);
		//blocker
		canvas.drawLine(blocker.start.x, blocker.start.y, blocker.end.x, blocker.end.y, blockerPaint);
		
		Point currentPoint = new Point();
		
		currentPoint.x = target.start.x;
		currentPoint.y = target.start.y;
		
		//Target (alternating color of the pieces)
		for(int i = 1; i <= TARGET_PIECES; ++i){
			
			if(!hitStates[i - 1]){
				if(i % 2 == 0)
					targetPaint.setColor(Color.YELLOW);
				else
					targetPaint.setColor(Color.BLUE);
				
				canvas.drawLine(currentPoint.x, currentPoint.y, target.end.x, (int)(currentPoint.y + pieceLength), targetPaint);
			}		

			currentPoint.y += pieceLength;		
		}
	}
	
	private void showGameOverDialog(int messageId){
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getResources().getString(messageId));
		builder.setCancelable(false);
		
		builder.setMessage(getResources().getString(R.string.results_format, shotsFired, totalTimeElapsed));
		builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogIsDisplayed = false;
				newGame();				
			}
		});
		
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				dialogIsDisplayed = true;
				builder.show();				
			}
		});
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(!dialogIsDisplayed){
			cannonThread = new CannonThread(holder);
			cannonThread.setRunning(true);
			cannonThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		cannonThread.setRunning(false);
		
		while(retry){
			try{
				cannonThread.join();
				retry = false;
			}
			catch(InterruptedException e){				
			}
		}
	}
	
	private class CannonThread extends Thread{
		private SurfaceHolder surfaceHolder;
		private boolean threadIsRunning = true;
		
		public CannonThread(SurfaceHolder holder){
			surfaceHolder = holder;
			setName("CannonThread");
		}
		
		public void setRunning(boolean running){
			threadIsRunning = running;
		}
		
		@Override
		public void run() {
			Canvas canvas = null;
			long previousFrameTime = System.currentTimeMillis();
			
			while(threadIsRunning){
				try{
					canvas = surfaceHolder.lockCanvas(null);
					
					synchronized(surfaceHolder){
						long currentTime = System.currentTimeMillis();
						double elapsedTimeMS = currentTime - previousFrameTime;
						totalTimeElapsed += elapsedTimeMS / 1000.0;
						updatePositions(elapsedTimeMS);
						drawGameElements(canvas);
						previousFrameTime = currentTime;
					}
				}
				finally{
					if(canvas != null)
						surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

}

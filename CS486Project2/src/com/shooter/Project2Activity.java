package com.shooter;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import android.util.Log;

import com.google.devtools.simple.runtime.components.android.Button;
import com.google.devtools.simple.runtime.components.android.Canvas;
import com.google.devtools.simple.runtime.components.android.Clock;
import com.google.devtools.simple.runtime.components.android.ComponentContainer;
import com.google.devtools.simple.runtime.components.android.Form;
import com.google.devtools.simple.runtime.components.android.HorizontalArrangement;
import com.google.devtools.simple.runtime.components.android.ImageSprite;
import com.google.devtools.simple.runtime.components.android.Label;
import com.google.devtools.simple.runtime.components.android.OrientationSensor;
import com.google.devtools.simple.runtime.components.android.Sound;
import com.google.devtools.simple.runtime.components.android.Sprite;
import com.google.devtools.simple.runtime.components.android.TinyWebDB;

public class Project2Activity extends Form {

	private GameCanvas myCanvas;
	private GameClock clock;
	private CountDown countDown;
	private Label scoreLabel;
	private Label timerLabel;
	private Label gameOverLabel;
	private Label highScoreLabel;
	private int time;
	private ResetButton resetButton;
	private ImageSprite spaceship;
	private BulletSprite bullet;
	private int score = 0;
	private int highScore = 0;
	private Controller controller;
	private TinyWebDB tinyDB;
	private Sound pewSound, boomSound, whoopSound;
	private boolean gameOver;

	void $define() {
		HorizontalArrangement ha = new HorizontalArrangement(this,
				this.LENGTH_FILL_PARENT);
		pewSound = new Sound(this);
		pewSound.Source("pew.wav");
		boomSound = new Sound(this);
		boomSound.Source("boom.wav");
		whoopSound = new Sound(this);
		whoopSound.Source("whoop.wav");

		tinyDB = new MyTinyWebDB(this);
		tinyDB.ServiceURL("http://shootercs486.appspot.com/");
		tinyDB.GetValue("myHighScore");

		myCanvas = new GameCanvas(this);
		myCanvas.Width(this.LENGTH_FILL_PARENT);
		myCanvas.Height(600);

		myCanvas.BackgroundColor(COLOR_NONE);
		myCanvas.BackgroundImage("space.png");

		scoreLabel = new Label(ha);
		scoreLabel.FontSize(14.0f);
		scoreLabel.Text("Score: 0");

		highScoreLabel = new Label(ha);
		highScoreLabel.Text("High Score: " + highScore);
		highScoreLabel.FontBold(true);

		time = 30;
		timerLabel = new Label(ha);
		timerLabel.FontSize(18.0f);
		timerLabel.Text("Time Left: " + time);

		gameOverLabel = new Label(ha);
		gameOverLabel.FontSize(25);
		gameOverLabel.FontBold(true);
		gameOverLabel.Visible(false);

		resetButton = new ResetButton(ha, "Reset");
		resetButton.Visible(false);

		clock = new GameClock(this);
		clock.TimerInterval(750);
		clock.TimerEnabled(true);

		countDown = new CountDown(this);
		countDown.TimerInterval(1000);
		countDown.TimerEnabled(true);

		spaceship = new ImageSprite(myCanvas);
		spaceship.Enabled(true);
		spaceship.Picture("spaceship.png");
		spaceship.X(610.0);
		spaceship.Y(530.0);
		spaceship.Initialize();

		controller = new Controller(this);
		controller.Enabled(true);

		gameOver = false;
	}

	private void fireMissle() {
		bullet = new BulletSprite(myCanvas);
		bullet.Interval(50);
		bullet.Speed(30);
		bullet.Heading(90);
		bullet.Rotates(false);
		bullet.Picture("apple.png");
		bullet.Visible(true);
		bullet.X(spaceship.X());
		bullet.Y(spaceship.Y() - 80);
		bullet.Initialize();
		bullet.playPew();
	}

	private void updateScore() {
		scoreLabel.Text("Score: " + score + " ");
	}

	private void updateTime() {
		timerLabel.Text("Time left: " + time + " ");
	}

	private void gameOver() {
		gameOver = true;
		clock.TimerEnabled(false);
		spaceship.Visible(false);
		gameOverLabel.Visible(true);
		resetButton.Visible(true);

		if (highScore <= score) {
			highScore = score;
		}


		highScoreLabel.Text("High Score: " + highScore);
		tinyDB.StoreValue("myHighScore", Integer.toString(highScore));
	}

	class ResetButton extends Button {

		public ResetButton(ComponentContainer container, String text) {
			super(container, text);
		}

		@Override
		public void Click() {
			gameOver = false;
			score = 0;
			updateScore();
			time = 30;
			updateTime();
			countDown.TimerEnabled(true);
			clock.TimerEnabled(true);
			spaceship.Visible(true);
			resetButton.Visible(false);
			gameOverLabel.Visible(false);
		}
	}

	// subclasses
	class GameCanvas extends Canvas {
		public GameCanvas(ComponentContainer container) {
			super(container);
		}

		@Override
		public void Touched(float x, float y, boolean touchedSprite) {
			if (!gameOver)
				fireMissle();
		}
	}

	class GameClock extends Clock {

		public GameClock(ComponentContainer container) {
			super(container);
		}

		@Override
		public void Timer() {
			// Create a new android object
			AndroidSprite android = new AndroidSprite(myCanvas);
			android.Picture("android.png");
			android.Interval(45);
			android.Speed(25);
			android.Heading(-90);
			android.Rotates(false);
			android.Initialize();
			Random r = new Random();
			int randX = r.nextInt(myCanvas.Width() - android.Width());
			android.MoveTo(randX, 0);
		}

	}

	class CountDown extends Clock {

		public CountDown(ComponentContainer container) {
			super(container);
		}

		@Override
		public void Timer() {
			time--;
			updateTime();
			if (time == 0) {
				this.TimerEnabled(false);
				gameOver();
			}
		}

	}

	class Controller extends OrientationSensor {
		boolean moveLeft;

		public Controller(ComponentContainer container) {
			super(container);
		}

		private void moveShip(float roll) {
			if (roll > 0 && !gameOver) { // move left
				spaceship.X(spaceship.X() - 20);
				if (!moveLeft) {
					moveLeft = true;
					whoopSound.Play();
				}
			} else if (roll < 0 && !gameOver) {
				spaceship.X(spaceship.X() + 20);
				if (moveLeft) {
					moveLeft = false;
					whoopSound.Play();
				}
			}
		}

		public void OrientationChanged(float yaw, float pitch, float roll) {
			moveShip(roll);
		}
	}

	class BulletSprite extends ImageSprite {

		public BulletSprite(ComponentContainer container) {
			super(container);
		}

		@Override
		public void CollidedWith(Sprite other) {
			super.CollidedWith(other);
			if (other instanceof AndroidSprite) {
				// deleteComponent(this);
				this.Visible(false);
				this.Speed(100);
				score++;
				updateScore();
			}
		}

		public void playPew() {
			pewSound.Play();
		}

		@Override
		public void EdgeReached(int edge) {
			deleteComponent(this);
		}
	}

	class AndroidSprite extends ImageSprite {

		ExplosionSprite explosion;

		public AndroidSprite(ComponentContainer container) {
			super(container);
			explosion = new ExplosionSprite(myCanvas);
			explosion.Picture("explosion.png");
			explosion.Initialize();
			explosion.Visible(false);
		}

		@Override
		public void CollidedWith(Sprite other) {
			super.CollidedWith(other);
			if (other instanceof BulletSprite) {
				boomSound.Play();
				explosion.MoveTo(this.X(), this.Y());
				explosion.Visible(true);
				this.Visible(false);
			}
		}

		@Override
		public void EdgeReached(int edge) {
			if (explosion != null) {
				deleteComponent(explosion);
			}
			deleteComponent(this);
		}
	}

	class ExplosionSprite extends ImageSprite {

		public ExplosionSprite(ComponentContainer container) {
			super(container);
		}

	}

	class MyTinyWebDB extends TinyWebDB {

		@Override
		public void WebServiceError(String message) {
			super.WebServiceError(message);
			Log.d("mine", message);
		}

		public MyTinyWebDB(ComponentContainer container) {
			super(container);
		}

		@Override
		public void GotValue(String tagFromWebDB, Object valueFromWebDB) {
			String result = (String) valueFromWebDB;
			if (!(result.length() == 0))
				highScore = Integer.valueOf(result);
		}
	}
}
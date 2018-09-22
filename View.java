import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame implements ActionListener {
	Controller controller;
	Model model;
	private Object secret_symbol; // limits access to methods that agents could potentially use to cheat
	public MyPanel panel;

	public View(Controller c, Model m, Object symbol) throws Exception {
		this.controller = c;
		this.model = m;
		secret_symbol = symbol;
		// Make the game window
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("AI Tournament");
		this.setSize(new Dimension(1203, 636));
		this.panel = new MyPanel();
		this.panel.addMouseListener(controller);
		this.getContentPane().add(this.panel);
		this.setVisible(true);
	}
	public void actionPerformed(ActionEvent evt) { repaint(); } // indirectly calls MyPanel.paintComponent

	class MyPanel extends JPanel {
		public static final int FLAG_IMAGE_HEIGHT = 25;
		Image img_bot_blue, img_bot_red, img_dead, img_flag_blue, img_flag_red;

		MyPanel() throws Exception {
			this.img_bot_blue = ImageIO.read(new File("robot_blue.png"));
			this.img_bot_red = ImageIO.read(new File("robot_red.png"));
			this.img_dead = ImageIO.read(new File("broken.png"));
			this.img_flag_blue = ImageIO.read(new File("flag_blue.png"));
			this.img_flag_red = ImageIO.read(new File("flag_red.png"));
		}
		public void paintComponent(Graphics g) {
			if(!controller.update()) // Give the agents a chance to make decisions
				checkIfOver();
			// Draw the view
			model.setPerspectiveBlue(secret_symbol);
			drawTerrain(g);
			drawFlags(g);
			drawSprites(g);
			drawBombs(g);
		}

		private void drawTerrain(Graphics g) {
			byte[] terrain = model.getTerrain(secret_symbol);
			int posBlue = 0;
			int posRed = (60 * 60 - 1) * 4;
			for(int y = 0; y < 60; y++) {
				for(int x = 0; x < 60; x++) {
					int bb = terrain[posBlue + 1] & 0xff;
					int gg = terrain[posBlue + 2] & 0xff;
					int rr = terrain[posBlue + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posBlue += 4;
				}
				for(int x = 60; x < 120; x++) {
					int bb = terrain[posRed + 1] & 0xff;
					int gg = terrain[posRed + 2] & 0xff;
					int rr = terrain[posRed + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posRed -= 4;
				}
			}
		}
		private void drawFlags(Graphics g) {	// Blue/Red
			g.drawImage(img_flag_blue, (int)Model.XFLAG, (int)Model.YFLAG - FLAG_IMAGE_HEIGHT, null);
			g.setColor(new Color(0, 0, 128));
			g.drawRect((int)Model.XFLAG - 3, (int)Model.YFLAG - 25, 3, 32);
			int energy = (int)(model.getScoreSelf() * 32.0f);
			g.fillRect((int)Model.XFLAG - 2, (int)Model.YFLAG + 7 - energy, 2, energy);

			g.drawImage(img_flag_red, (int)Model.XFLAG_OPPONENT,  (int)Model.YFLAG_OPPONENT - FLAG_IMAGE_HEIGHT, null);
			g.setColor(new Color(128, 0, 0));
			g.drawRect((int)Model.XFLAG_OPPONENT - 3, (int)Model.YFLAG_OPPONENT - 25, 3, 32);
			energy = (int)(model.getScoreOppo() * 32.0f);
			g.fillRect((int)Model.XFLAG_OPPONENT - 2, (int)Model.YFLAG_OPPONENT + 7 - energy, 2, energy);
		}
		private void drawSprites(Graphics g) {
			ArrayList<Model.Sprite> sprites_blue = model.getSpritesBlue(secret_symbol);
			for(int i = 0; i < sprites_blue.size(); i++) {
				// Draw the robot image
				Model.Sprite s = sprites_blue.get(i);
				if(s.energy >= 0) {
					g.drawImage(img_bot_blue, (int)s.x - 12, (int)s.y - 32, null);
					// Draw energy bar
					g.setColor(new Color(0, 0, 128));
					g.drawRect((int)s.x - 18, (int)s.y - 32, 3, 32);
					int energy = (int)(s.energy * 32.0f);
					g.fillRect((int)s.x - 17, (int)s.y - energy, 2, energy);
				}
				else
					g.drawImage(img_dead, (int)s.x - 12, (int)s.y - 32, null);
				// Draw selection box
				if(i == controller.getSelectedSprite()) {
					g.setColor(new Color(100, 0, 0));
					g.drawRect((int)s.x - 22, (int)s.y - 42, 44, 57);
				}
			}
			ArrayList<Model.Sprite> sprites_red = model.getSpritesRed(secret_symbol);
			for(int i = 0; i < sprites_red.size(); i++) {
				// Draw the robot image
				Model.Sprite s = sprites_red.get(i);
				if(s.energy >= 0) {
					g.drawImage(img_bot_red, (int)(Model.XMAX - 1 - s.x) - 12, (int)(Model.YMAX - 1 - s.y) - 32, null);
					// Draw energy bar
					g.setColor(new Color(128, 0, 0));
					g.drawRect((int)(Model.XMAX - 1 - s.x) + 14, (int)(Model.YMAX - 1 - s.y) - 32, 3, 32);
					int energy = (int)(s.energy * 32.0f);
					g.fillRect((int)(Model.XMAX - 1 - s.x) + 15, (int)(Model.YMAX - 1 - s.y) - energy, 2, energy);
				}
				else
					g.drawImage(img_dead, (int)(Model.XMAX - 1 - s.x) - 12, (int)(Model.YMAX - 1 - s.y) - 32, null);
			}
		}
		private void drawBombs(Graphics g) {
			ArrayList<Model.Bomb> bombs = model.getBombsFlying(secret_symbol);
			for(int i = 0; i < bombs.size(); i++) {
				Model.Bomb b = bombs.get(i);
				int x = (int)b.getX();
				int y = (int)b.getY();
				int height = (int)(0.01 * b.position * (b.distance - b.position));
				g.setColor(new Color(128, 64, 192));
				g.fillOval(x - 5, y - 5 - height, 10, 10);
				g.setColor(new Color(100, 100, 100));
				g.fillOval(x - 5, y - 5, 10, 10);
			}
			bombs = model.getBombsExploding(secret_symbol);
			for(int i = 0; i < bombs.size(); i++) {
				Model.Bomb b = bombs.get(i);
				int x = (int)b.getX();
				int y = (int)b.getY();
				g.setColor(new Color(128, 0, 64));
				int r = (int)(b.position - b.distance);
				g.drawOval(x - r, y - r, 2 * r, 2 * r);
				r = (int)Model.BLAST_RADIUS;
				g.drawOval(x - r, y - r, 2 * r, 2 * r);
			}
		}
		void checkIfOver() {
			model.setPerspectiveBlue(secret_symbol);
			if(model.getScoreSelf() < 0.0f && model.getScoreOppo() >= 0.0f)
				System.out.println("\nRed wins!");
			else if(model.getScoreOppo() < 0.0f && model.getScoreSelf() >= 0.0f)
				System.out.println("\nBlue wins!");
			else
				System.out.println("\nTie.");
			View.this.dispatchEvent(new WindowEvent(View.this, WindowEvent.WINDOW_CLOSING));
		}
	}
}
package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;

/**
 * A simple GUI, similar to the one in assignments 1 and 2, that you can base
 * your renderer off. It is abstract, and there are three methods you need to
 * implement: onLoad, onKeyPress, and render. There is a method to get the
 * ambient light level set by the sliders. You are free to use this class as-is,
 * modify it, or ignore it completely.
 * 
 * @author tony
 */
public abstract class GUI {

	/**
	 * Is called when the user has successfully selected a model file to load,
	 * and is passed a File representing that file.
	 */
	protected abstract void onLoad(File file);

	/**
	 * Is called every time the user presses a key. This can be used for moving
	 * the camera around. It is passed a KeyEvent object, whose methods of
	 * interest are getKeyChar() and getKeyCode().
	 */
	protected abstract void onKeyPress(KeyEvent ev);

	/**
	 * Is called every time the drawing canvas is drawn. This should return a
	 * BufferedImage that is your render of the scene.
	 */
	protected abstract BufferedImage render();

	/**
	 * Forces a redraw of the drawing canvas. This is called for you, so you
	 * don't need to call this unless you modify this GUI.
	 */
	public void redraw() {
		frame.repaint();
	}

	/**
	 * Returns the values of the three sliders used for setting the ambient
	 * light of the scene. The returned array in the form [R, G, B] where each
	 * value is between 0 and 255.
	 */
	public int[] getAmbientLight() {
		return new int[] { red.getValue(), green.getValue(), blue.getValue() };
	}
	/**
	 * Returns the values of the three sliders used for setting the ambient
	 * light of the scene. The returned array in the form [R, G, B] where each
	 * value is between 0 and 255.
	 */
	public int[] getIncidentLight() {
		return new int[] { red2.getValue(), green2.getValue(), blue2.getValue() };
	}

	public int[] getIncidentLight2() {
		return new int[] { red3.getValue(), green3.getValue(), blue3.getValue() };
	}

	public int[] getIncidentLight3() {
		return new int[] { red4.getValue(), green4.getValue(), blue4.getValue() };
	}

	public int[] getIncidentLight4() {
		return new int[] { red5.getValue(), green5.getValue(), blue5.getValue() };
	}

	public static final int CANVAS_WIDTH = 600;
	public static final int CANVAS_HEIGHT = 600;


	private JFrame frame;
	private final JSlider red = new JSlider(JSlider.HORIZONTAL, 0, 255, 10);
	private final JSlider green = new JSlider(JSlider.HORIZONTAL, 0, 255, 10);
	private final JSlider blue = new JSlider(JSlider.HORIZONTAL, 0, 255, 10);

	private final JSlider red2 = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);
	private final JSlider green2 = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);
	private final JSlider blue2 = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);

	private final JSlider red3 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	private final JSlider green3 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	private final JSlider blue3 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);

	private final JSlider red4 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	private final JSlider green4 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	private final JSlider blue4 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);

	private final JSlider red5 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	private final JSlider green5 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	private final JSlider blue5 = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);

	private static final Dimension DRAWING_SIZE = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
	private static final Dimension CONTROLS_SIZE = new Dimension(150, 600);

	private static final Font FONT = new Font("Courier", Font.BOLD, 36);

	public GUI() {
		initialise();
	}

	@SuppressWarnings("serial")
	private void initialise() {
		// make the frame
		frame = new JFrame();
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.LINE_AXIS));
		frame.setSize(new Dimension(DRAWING_SIZE.width + CONTROLS_SIZE.width, DRAWING_SIZE.height));
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set up the drawing canvas, hook it into the render() method, and give
		// it a nice default if render() returns null.
		JComponent drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				BufferedImage image = render();
				if (image == null) {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, DRAWING_SIZE.width, DRAWING_SIZE.height);
					g.setColor(Color.BLACK);
					g.setFont(FONT);
					g.drawString("IMAGE IS NULL", 50, DRAWING_SIZE.height - 50);
				} else {
					g.drawImage(image, 0, 0, null);
				}
			}
		};
		// fix its size
		drawing.setPreferredSize(DRAWING_SIZE);
		drawing.setMinimumSize(DRAWING_SIZE);
		drawing.setMaximumSize(DRAWING_SIZE);
		drawing.setVisible(true);

		// set up the load button
		final JFileChooser fileChooser = new JFileChooser();
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				// set up the file chooser
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select input file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				// run the file chooser and check the user didn't hit cancel
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					onLoad(file);
					redraw();
				}
			}
		});
		// we have to put the button in its own panel to ensure it fills the
		// full width of the control bar.
		JPanel loadpanel = new JPanel(new BorderLayout());
		loadpanel.setMaximumSize(new Dimension(1000, 25));
		loadpanel.setPreferredSize(new Dimension(1000, 25));
		loadpanel.add(load, BorderLayout.CENTER);

		// set up the sliders for ambient light. they were instantiated in
		// the field definition, as for some reason they need to be final to
		// pull the set background trick.
		red.setBackground(new Color(230, 50, 50));
		green.setBackground(new Color(50, 230, 50));
		blue.setBackground(new Color(50, 50, 230));

		red2.setBackground(new Color(230, 50, 50));
		green2.setBackground(new Color(50, 230, 50));
		blue2.setBackground(new Color(50, 50, 230));

		red3.setBackground(new Color(230, 50, 50));
		green3.setBackground(new Color(50, 230, 50));
		blue3.setBackground(new Color(50, 50, 230));

		red4.setBackground(new Color(230, 50, 50));
		green4.setBackground(new Color(50, 230, 50));
		blue4.setBackground(new Color(50, 50, 230));

		red5.setBackground(new Color(230, 50, 50));
		green5.setBackground(new Color(50, 230, 50));
		blue5.setBackground(new Color(50, 50, 230));

		JPanel sliderparty = new JPanel();
		sliderparty.setLayout(new BoxLayout(sliderparty, BoxLayout.PAGE_AXIS));
		sliderparty.setBorder(BorderFactory.createTitledBorder("Ambient Light"));

		sliderparty.add(red);
		sliderparty.add(green);
		sliderparty.add(blue);

		JPanel sliderparty2 = new JPanel();
		sliderparty2.setLayout(new BoxLayout(sliderparty2, BoxLayout.PAGE_AXIS));
		sliderparty2.setBorder(BorderFactory.createTitledBorder("Incident Light"));

		sliderparty2.add(red2);
		sliderparty2.add(green2);
		sliderparty2.add(blue2);

		JPanel sliderparty3 = new JPanel();
		sliderparty3.setLayout(new BoxLayout(sliderparty3, BoxLayout.PAGE_AXIS));
		sliderparty3.setBorder(BorderFactory.createTitledBorder("Incident Light 2"));

		sliderparty3.add(red3);
		sliderparty3.add(green3);
		sliderparty3.add(blue3);

		JPanel sliderparty4 = new JPanel();
		sliderparty4.setLayout(new BoxLayout(sliderparty4, BoxLayout.PAGE_AXIS));
		sliderparty4.setBorder(BorderFactory.createTitledBorder("Incident Light 3"));

		sliderparty4.add(red4);
		sliderparty4.add(green4);
		sliderparty4.add(blue4);

		JPanel sliderparty5 = new JPanel();
		sliderparty5.setLayout(new BoxLayout(sliderparty5, BoxLayout.PAGE_AXIS));
		sliderparty5.setBorder(BorderFactory.createTitledBorder("Incident Light 4"));

		sliderparty5.add(red5);
		sliderparty5.add(green5);
		sliderparty5.add(blue5);

		// this is not a best-practices way of doing key listening; instead you
		// should use either a KeyListener or an InputMap/ActionMap combo. but
		// this method neatly avoids any focus issues (KeyListener) and requires
		// less effort on your part (ActionMap).
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ev) {
				if (ev.getID() == KeyEvent.KEY_PRESSED) {
					onKeyPress(ev);
					redraw();
				}
				return true;
			}
		});

		// make the panel on the right, fix its size, give it a border!
		JPanel controls = new JPanel();
		controls.setPreferredSize(CONTROLS_SIZE);
		controls.setMinimumSize(CONTROLS_SIZE);
		controls.setMaximumSize(CONTROLS_SIZE);
		controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
		Border edge = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		controls.setBorder(edge);

		controls.add(loadpanel);
		controls.add(Box.createRigidArea(new Dimension(0, 15)));
		controls.add(sliderparty);
		controls.add(sliderparty2);
		controls.add(sliderparty3);
		controls.add(sliderparty4);
		controls.add(sliderparty5);
		// if i were going to add more GUI components, i'd do it here.
		controls.add(Box.createVerticalGlue());

		// put it all together.
		frame.add(drawing);
		frame.add(controls);

		frame.pack();
		frame.setVisible(true);
	}
}

// code for comp261 assignments

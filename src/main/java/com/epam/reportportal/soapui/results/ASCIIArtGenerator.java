package com.epam.reportportal.soapui.results;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * ASCII Art Generator in Java.
 * Prints a given text as an ASCII text art on the console.
 * This code is licensed under - CC Attribution CC BY 4.0.
 *
 * @author ASCIIArtGenerator
 */
public class ASCIIArtGenerator {

	public static final int ART_SIZE_SMALL = 12;
	public static final int ART_SIZE_MEDIUM = 18;
	public static final int ART_SIZE_LARGE = 24;
	public static final int ART_SIZE_HUGE = 32;

	public static final String DEFAULT_ART_SYMBOL = "@";

	public enum ASCIIArtFont {
		ART_FONT_DIALOG("Dialog"),
		ART_FONT_DIALOG_INPUT("DialogInput"),
		ART_FONT_MONO("Monospaced"),
		ART_FONT_SERIF("Serif"),
		ART_FONT_SANS_SERIF("SansSerif");

		private String value;

		public String getValue() {
			return value;
		}

		private ASCIIArtFont(String value) {
			this.value = value;
		}
	}

	private final int textHeight;
	private final ASCIIArtFont fontType;
	private final String artSymbol;

	/**
	 * @param textHeight - Use a predefined size or a custom type
	 * @param fontType   - Use one of the available fonts
	 * @param artSymbol  - Specify the character for printing the ascii art
	 */
	public ASCIIArtGenerator(int textHeight, ASCIIArtFont fontType, String artSymbol) {
		this.textHeight = textHeight;
		this.fontType = fontType;
		this.artSymbol = artSymbol;
	}

	public ASCIIArtGenerator() {
		this(ART_SIZE_SMALL, ASCIIArtFont.ART_FONT_MONO, DEFAULT_ART_SYMBOL);
	}

	/**
	 * Prints ASCII art for the specified text. For size, you can use predefined sizes or a custom size.
	 * Usage - printTextArt("Hi",30,ASCIIArtFont.ART_FONT_SERIF,"@");
	 *
	 * @param artText Text to draw
	 */
	public String getArtText(String artText) {
		String fontName = fontType.getValue();
		int imageWidth = findImageWidth(textHeight, artText, fontName);

		BufferedImage image = new BufferedImage(imageWidth, textHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		Font font = new Font(fontName, Font.BOLD, textHeight);
		g.setFont(font);

		Graphics2D graphics = (Graphics2D) g;
		graphics.drawString(artText, 0, getBaselinePosition(g, font));

		StringBuilder artTextBuilder = new StringBuilder();
		for (int y = 0; y < textHeight; y++) {
			StringBuilder sb = new StringBuilder();
			for (int x = 0; x < imageWidth; x++)
				sb.append(image.getRGB(x, y) == Color.WHITE.getRGB() ? artSymbol : " ");
			if (sb.toString().trim().isEmpty())
				continue;
			artTextBuilder.append(sb).append("\n");
		}
		return artTextBuilder.toString();
	}

	/**
	 * Using the Current font and current art text find the width of the full image
	 *
	 * @param textHeight Text Height
	 * @param artText    Art Text
	 * @param fontName   Font Name
	 * @return Image width
	 */
	private int findImageWidth(int textHeight, String artText, String fontName) {
		BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = im.getGraphics();
		g.setFont(new Font(fontName, Font.BOLD, textHeight));
		return g.getFontMetrics().stringWidth(artText);
	}

	/**
	 * Find where the text baseline should be drawn so that the characters are within image
	 *
	 * @param g    Graphics
	 * @param font Font
	 * @return Text Baseline
	 */
	private int getBaselinePosition(Graphics g, Font font) {
		FontMetrics metrics = g.getFontMetrics(font);
		return metrics.getAscent() - metrics.getDescent();
	}
}
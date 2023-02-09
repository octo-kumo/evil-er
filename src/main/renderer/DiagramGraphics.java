package main.renderer;

import model.Vector;
import model.er.Entity;
import org.jetbrains.annotations.NotNull;
import utils.callbacks.DrawContext;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DiagramGraphics extends Graphics2D {
    @NotNull
    public final DrawContext context;
    private final Graphics2D g;
    private Font fallback;

    public DiagramGraphics(Graphics2D g, @NotNull DrawContext context) {
        this.g = g;
        this.context = context;
        fallback = new Font(null, getFont().getStyle(), getFont().getSize());
    }


    public static Stream<Entity> flatten(List<? extends Entity> entities) {
        return Stream.concat(entities.parallelStream(), entities.parallelStream().flatMap(e -> flatten(e.attributes)));
    }

    public void drawStringCenter(String text, Color color) {
        drawStringCenter(text, 0, 0, color);
    }

    public void drawStringCenter(String string, Vector vector) {
        drawStringCenter(string, (float) vector.getX(), (float) vector.getY());
    }

    public void drawStringCenter(String string, Vector vector, Color color) {
        drawStringCenter(string, (float) vector.getX(), (float) vector.getY(), color);
    }

    public void drawStringCenter(String text, float cx, float cy) {
        Rectangle2D size = getStringBounds(text);
        cx += -size.getWidth() / 2f;
        cy += -size.getY() - size.getHeight() / 2f;
        drawString(text, cx, cy);
    }

    public void drawStringCenter(String text, float cx, float cy, Color color) {
        Color bak = getColor();
        setColor(color);
        Rectangle2D size = getStringBounds(text);
        cx += -size.getWidth() / 2f;
        cy += -size.getY() - size.getHeight() / 2f;
        AffineTransform at = AffineTransform.getTranslateInstance(cx, cy);
        fill(at.createTransformedShape(size));
        setColor(bak);
        drawString(text, cx, cy);
    }

    public Line2D lineUnderString(String text, double cx, double cy) {
        Rectangle2D size = getStringBounds(text);
        cy += -size.getY() - size.getHeight() / 2f;
        return new Line2D.Double(cx - size.getWidth() / 2, cy, cx + size.getWidth() / 2, cy);
    }

    public Line2D lineUnderString(String text, Vector vector) {
        return lineUnderString(text, vector.getX(), vector.getY());
    }

    public void draw(Shape shape, Color fill, Color outline) {
        setColor(fill);
        fill(shape);
        setColor(outline);
        draw(shape);
    }

    public void dashed(Shape shape) {
        BasicStroke back = (BasicStroke) g.getStroke();
        setStroke(new BasicStroke(back.getLineWidth(), back.getEndCap(), back.getLineJoin(), back.getMiterLimit(), new float[]{2}, 0));
        draw(shape);
        setStroke(back);
    }

    public void dashed(Shape shape, Color fill, Color outline) {
        setColor(fill);
        fill(shape);

        BasicStroke back = (BasicStroke) g.getStroke();
        setStroke(new BasicStroke(back.getLineWidth(), back.getEndCap(), back.getLineJoin(), back.getMiterLimit(), new float[]{2}, 0));
        setColor(outline);
        draw(shape);
        setStroke(back);
    }

    public @NotNull DrawContext getContext() {
        return context;
    }

    @Override
    public void draw(Shape s) {
        g.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return g.drawImage(img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        g.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        g.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        g.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        g.drawString(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        boolean canDisplay = -1 == getFont().canDisplayUpTo(str);
        Font font = getFont();
        if (!canDisplay) setFont(fallback);
        g.drawString(str, x, y);
        if (!canDisplay) setFont(font);
    }

    public Rectangle2D getStringBounds(String string) {
        boolean canDisplay = -1 == getFont().canDisplayUpTo(string);
        Font font = getFont();
        if (!canDisplay) font = fallback;
        return font.getStringBounds(string, getFontRenderContext());
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        g.drawString(iterator, x, y);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return g.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return g.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return g.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return g.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public void dispose() {
        g.dispose();
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        g.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        this.g.drawGlyphVector(g, x, y);
    }

    @Override
    public void fill(Shape s) {
        g.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return g.hit(rect, s, onStroke);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return g.getDeviceConfiguration();
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        g.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return g.getRenderingHint(hintKey);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        g.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return g.getRenderingHints();
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        g.setRenderingHints(hints);
    }

    @Override
    public DiagramGraphics create() {
        return new DiagramGraphics((Graphics2D) g.create(), getContext());
    }

    @Override
    public void translate(int x, int y) {
        g.translate(x, y);
    }

    @Override
    public Color getColor() {
        return g.getColor();
    }

    @Override
    public void setColor(Color c) {
        g.setColor(c);
    }

    @Override
    public void setPaintMode() {
        g.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        g.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return g.getFont();
    }

    @Override
    public void setFont(Font font) {
        fallback = new Font(null, fallback.getStyle(), fallback.getSize());
        g.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return g.getFontMetrics(f);
    }

    @Override
    public Rectangle getClipBounds() {
        return g.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        g.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        g.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return g.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        g.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        g.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        g.clearRect(x, y, width, height);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        g.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        g.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        g.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void translate(double tx, double ty) {
        g.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        g.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        g.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        g.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        g.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        g.transform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return g.getTransform();
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        g.setTransform(Tx);
    }

    @Override
    public Paint getPaint() {
        return g.getPaint();
    }

    @Override
    public void setPaint(Paint paint) {
        g.setPaint(paint);
    }

    @Override
    public Composite getComposite() {
        return g.getComposite();
    }

    @Override
    public void setComposite(Composite comp) {
        g.setComposite(comp);
    }

    @Override
    public Color getBackground() {
        return g.getBackground();
    }

    @Override
    public void setBackground(Color color) {
        g.setBackground(color);
    }

    @Override
    public Stroke getStroke() {
        return g.getStroke();
    }

    @Override
    public void setStroke(Stroke s) {
        g.setStroke(s);
    }

    @Override
    public void clip(Shape s) {
        g.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return g.getFontRenderContext();
    }

    public void translate(Point2D point) {
        translate(point.getX(), point.getY());
    }
}

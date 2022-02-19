package model.rs;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import model.Drawable;
import model.ImmutableVector;
import model.Vector;
import org.jetbrains.annotations.NotNull;
import shapes.lines.SchemaLine;

import java.awt.geom.Rectangle2D;

public class Column extends Vector implements Drawable, Comparable<Column> {

    public enum DataType {
        CHAR(true), VARCHAR(true),

        BOOL, INT(true), FLOAT(true), INTEGER,

        DATE, DATETIME,

        TIMESTAMP, TIME, YEAR,

        BINARY(true), VARBINARY(true), TINYBLOB, TINYTEXT, TEXT(true), BLOB(true), MEDIUMTEXT, MEDIUMBLOB, LONGTEXT, LONGBLOB, BIT(true), BOOLEAN, TINYINT(true), SMALLINT(true), MEDIUMINT(true), BIGINT(true);

        private final boolean numbered;

        DataType() {
            this(false);
        }

        DataType(boolean numbered) {
            this.numbered = numbered;
        }
    }

    public static final double WIDTH = 100;
    public static final double HEIGHT = 40;
    public static final ImmutableVector OFFSET = new ImmutableVector() {{
        x = WIDTH / 2;
        y = HEIGHT / 2;
    }};

    private Table parent;
    @Expose
    private int index;
    @Expose
    private String name;
    @Expose
    private DataType type;
    @Expose
    private String param = "8";
    @Expose
    private boolean key;
    @Expose
    private boolean notNull = false;

    public Column() {
        this("Unnamed", false);
    }

    public Column(String name, boolean key) {
        this(name, key, DataType.VARCHAR);
    }

    public Column(String name, boolean key, DataType columnType) {
        this.name = name;
        this.key = key;
        this.type = columnType;
    }

    public double getX() {
        return this.parent == null ? x : (x = parent.getX() + WIDTH * index);
    }

    public double getY() {
        return this.parent == null ? y : (y = parent.getY() + HEIGHT);
    }

    @Override
    public void draw(@NotNull DiagramGraphics g) {
        g.draw(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT), g.context.fill(), g.context.foreground());
        g.drawStringCenter(name, (float) (WIDTH / 2), (float) (HEIGHT / 2));
        if (key) g.draw(g.lineUnderString(name, (float) (WIDTH / 2), (float) (HEIGHT / 2 + 3)));
    }


    @Override
    public int compareTo(@NotNull Column other) {
        return Boolean.compare(other.key, this.key);
    }

    public void predrawAsForeign(@NotNull DiagramGraphics g, @NotNull Vector origin, Table parent) {
        g.draw(new SchemaLine(origin.add(OFFSET), parent.positionOf(this).add(OFFSET), g.getContext()));
    }

    public void drawAsForeign(@NotNull DiagramGraphics g, @NotNull Boolean key, @NotNull Vector origin) {
        g.draw(new Rectangle2D.Double(origin.getX(), origin.getY(), WIDTH, HEIGHT), g.context.fill(), g.context.foreground());
        g.drawStringCenter(name + " (FK)", origin.add(WIDTH / 2, HEIGHT / 2));
        if (key) g.draw(g.lineUnderString(name + " (FK)", origin.add(WIDTH / 2, HEIGHT / 2 + 3)));
    }

    @Override
    public String toString() {
        return "Column{" + "index=" + index + ", parent=" + parent + ", name='" + name + '\'' + ", key=" + key + '}';
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Table getParent() {
        return parent;
    }

    public void setParent(Table parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String toSQL() {
        return toSQL("");
    }

    public String toSQL(String prefix) {
        return String.format("%-16s %s%s %s", prefix + name, type, type.numbered ? "(" + param + ")" : "", notNull ? "NOT NULL" : "").trim();
    }
}

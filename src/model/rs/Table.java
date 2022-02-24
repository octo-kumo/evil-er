package model.rs;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import main.rs.RSDiagram;
import model.Drawable;
import model.Vector;
import org.jetbrains.annotations.NotNull;
import shapes.lines.RangeLine;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table extends Vector implements Drawable {
    private static final String[] SQL_KEYWORDS = new String[]{"ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGIDENTITY", "BIT", "BIT_LENGTH", "BOTH", "BY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH", "CHARACTER_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "FUNCTION", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LIMIT", "LOCAL", "LOWER", "MASK", "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NLONGVARCHAR", "NULL", "NULLIF", "NUMERIC", "NVARCHAR", "OCTET_LENGTH", "OF", "OFFSET", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "PASSWORD", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVERSE", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE", "SMALLIDENTITY", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "STDEV", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "TRY_CAST", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "YEAR", "ZONE"};

    static {
        Arrays.sort(SQL_KEYWORDS);
    }

    private static final Font title = new Font(null, Font.BOLD, 12);
    private static final Font normal = new Font(null, Font.PLAIN, 12);
    private static final Font small = new Font(null, Font.PLAIN, 10);
    public final List<Foreign> foreign;

    @Expose
    public List<_Foreign> _foreign;
    @Expose
    public final Map<String, Column> attributeMap;
    @Expose
    private String name;

    public long keyCount = 0;
    public long colCount = 0;
    private List<Column> sorted;
    private boolean highlighted;
    private List<Column> keys;
    private List<Column> cols;

    public Table() {
        this("Unnamed");
    }

    public Table(String name) {
        this.name = name;
        this.attributeMap = new HashMap<>();
        this.sorted = new ArrayList<>();
        this.foreign = new ArrayList<>();
    }

    public boolean add(@NotNull Column column) {
        column.setParent(this);
        boolean n = this.attributeMap.put(column.getName(), column) == null;
        revalidate();
        return n;
    }


    public void add(Table other, String name, boolean required, String prefix) {
        this.foreign.add(new Foreign(required, name, other, prefix));
        revalidate();
    }

    public void add(Table other, String name, boolean required) {
        add(other, name, required, "");
    }


    public boolean remove(Column column) {
        if (this.attributeMap.remove(column.getName(), column)) {
            column.setParent(null);
            revalidate();
            return true;
        }
        return false;
    }

    public void revalidate() {
        this.sorted = attributeMap.values().stream().sorted(Comparator.comparingInt(a -> a.getName().length())).collect(Collectors.toList());
        for (int i = 0; i < this.sorted.size(); i++) this.sorted.get(i).setIndex(i);

        foreign.sort(Comparator.comparing(f -> f.table.getName()));
        HashMap<String, Integer> hashMap = new HashMap<>();
        foreign.forEach(f -> {
            if (Objects.equals(f.role, "inherits")) return;
            String prefix = f.table.name.substring(0, 1);
            hashMap.compute(prefix, (s, integer) -> integer == null ? 1 : integer + 1);
            prefix = prefix + hashMap.get(prefix);
            f.prefix = prefix + "_";
        });

        keyCount = getKeys().size();
        colCount = getCols().size();
    }

    @Override
    public void draw(@NotNull DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        g.translate(getX(), getY());

        g.setFont(title);
        g.drawStringCenter(name, (float) (Column.WIDTH / 2), (float) (Column.HEIGHT / 2 + 10));
        g.setFont(normal);

        if (highlighted) g.draw(getShape());
        g.translate(0, Column.HEIGHT);
        for (Column e : sorted) {
            e.draw(g);
            g.translate(Column.WIDTH, 0);
        }
        g.setTransform(transform);

        Vector offset = this.add(sorted.size() * Column.WIDTH, Column.HEIGHT);
        for (Foreign other : foreign) {
            other.table.drawAsForeign(g, other, offset.clone());
            offset.incre(other.table.keyCount * Column.WIDTH, 0);
        }
        if (highlighted) {
            g.setColor(g.context.highlight());
            g.draw(getShapeWorld());
            g.setColor(g.context.foreground());
        }
    }

    @Override
    public void predraw(DiagramGraphics g) {
        Vector offset = this.add(sorted.size() * Column.WIDTH, Column.HEIGHT);
        for (Foreign other : foreign) {
            other.table.predrawAsForeign(g, offset.clone(), other.table);
            offset.incre(other.table.keyCount * Column.WIDTH, 0);
        }
    }

    /**
     * Draw this table's keys onto position specified
     *
     * @param g       graphics
     * @param foreign ref
     * @param origin  to draw
     */
    public void drawAsForeign(@NotNull DiagramGraphics g, Foreign foreign, @NotNull Vector origin) {
        double diff = keyCount * .5 * Column.WIDTH;
        Vector center = origin.add(diff, 7);
        for (Column key : keys) {
            key.drawAsForeign(g, foreign, origin);
            origin.incre(Column.WIDTH, 0);
        }
        if (((RSDiagram) g.getContext()).showBrackets.get()) {
            g.draw(new RangeLine(center.add(-diff, 0), center.add(diff, 0)));
            Font font = g.getFont();
            g.setFont(small);
            g.drawStringCenter(foreign.role, center, g.context.background());
            g.setFont(font);
        }
    }

    public void predrawAsForeign(DiagramGraphics g, Vector origin, Table parent) {
        for (Column key : keys) {
            key.predrawAsForeign(g, origin, parent);
            origin.incre(Column.WIDTH, 0);
        }
    }

    public Stream<Column> selfKeys() {
        return sorted.stream().filter(Column::isKey);
    }

    /**
     * Combines keys and all required foreign keys
     *
     * @return minimal set of keys
     */
    public List<Column> getKeys() {
        return keys = Stream.concat(selfKeys(),
                foreign.stream().filter(f -> f.required).flatMap(e -> e.table.getKeys().stream())).collect(Collectors.toList());
    }

    /**
     * Combine all columns and all foreign keys
     *
     * @return all columns
     */
    public List<Column> getCols() {
        return cols = Stream.concat(sorted.stream(), foreign.stream().flatMap(e -> e.table.getKeys().stream())).collect(Collectors.toList());
    }

    public Vector positionOf(Column column) {
        return this.add(cols.indexOf(column) * Column.WIDTH, Column.HEIGHT);
    }

    public Shape getShapeWorld() {
        AffineTransform tx = new AffineTransform();
        tx.translate(getX(), getY());
        return tx.createTransformedShape(getShape());
    }

    public Rectangle2D getShape() {
        return new Rectangle2D.Double(0, 0, Math.max(colCount, 1) * Column.WIDTH, Column.HEIGHT * 2);
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public String getName() {
        return name;
    }

    public String getEscapedName() {
        String name = getName().replaceAll("[^A-Za-z0-9_]", "_");
        if (Arrays.binarySearch(SQL_KEYWORDS, name.toUpperCase(Locale.ROOT)) >= 0) name = '"' + name + '"';
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void updateParents(Table table) {
        table.attributeMap.values().forEach(a -> a.setParent(table));
    }

    public String getKeysSQL(Foreign ref) {
        return Stream.concat(
                        selfKeys().map(column -> column.getName(ref)),
                        foreign.stream().filter(f -> f.required)
                                .flatMap(t -> t.table.getKeys().stream().map(c -> c.getName(ref == null ? t : ref))))
                .collect(Collectors.joining(", "));
    }

    public String getKeysSQL() {
        return getKeysSQL(null);
    }

    public String toSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(getEscapedName()).append("\n(\n");
        for (Column value : sorted) {
            builder.append('\t').append(value.toSQL()).append(",\n");
        }
        for (Foreign foreign : foreign) {
            for (Column c : foreign.table.getKeys()) {
                builder.append('\t').append(c.toSQL(foreign)).append(",")
                        .append(" # from '").append(foreign.table.getName()).append("'; ")
                        .append(foreign.role)
                        .append("\n");
            }
        }
        builder.append("\tPRIMARY KEY (").append(getKeysSQL()).append(")\n");
        builder.append(");");
        return builder.toString();
    }

    public String toSQLForeign() {
        return foreign.isEmpty() ? "" : "ALTER TABLE " + getEscapedName() + "\n" +
                foreign.stream().map(foreign ->
                        String.format("\tADD FOREIGN KEY (%s) REFERENCES %s(%s)",
                                foreign.table.getKeysSQL(foreign),
                                foreign.table.getEscapedName(),
                                foreign.table.getKeysSQL())
                ).collect(Collectors.joining(",\n")) + ";";
    }

    public static class Foreign {
        public Table table;
        public String role;
        public boolean required;
        public String prefix;

        public Foreign(boolean required, String role, Table table) {
            this(required, role, table, "");
        }

        public Foreign(boolean required, String role, Table table, String prefix) {
            this.required = required;
            this.role = role;
            this.table = table;
            this.prefix = prefix;
        }
    }

    public static class _Foreign {
        @Expose
        public String table;
        @Expose
        public String role;
        @Expose
        public boolean required;
        @Expose
        public String prefix;

        public _Foreign(Foreign foreign) {
            this.table = foreign.table.name;
            this.prefix = foreign.prefix;
            this.required = foreign.required;
            this.role = foreign.role;
        }
    }

}

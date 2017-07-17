package info.pascalkrause.vertx.mongodata.collection;

public class Index {

    private final boolean ascending;
    private final boolean unique;
    private final String name;
    private final String column;

    /**
     * Creates an unique Index with order ascending
     *
     * @param name Name of the index.
     * @param column The column on which the index will be created.
     */
    public Index(String name, String column) {
        this(name, column, false);
    }

    /**
     * Creates an Index with order ascending
     *
     * @param name Name of the index.
     * @param column The column on which the index will be created.
     * @param unique Defines if the index is unique or not.
     */
    public Index(String name, String column, boolean unique) {
        this(name, column, unique, true);
    }

    /**
     * Creates an Index
     *
     * @param name Name of the index.
     * @param column The column on which the index will be created.
     * @param ascending The order of the index. True = ascending, False = descending.
     * @param unique Defines if the index is unique or not.
     */
    public Index(String name, String column, boolean unique, boolean ascending) {
        this.name = name;
        this.column = column;
        this.unique = unique;
        this.ascending = ascending;
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getName() {
        return name;
    }

    public String getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ascending ? 1231 : 1237);
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (unique ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Index && hashCode() == obj.hashCode()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Index [ascending=" + ascending + ", unique=" + unique + ", name=" + name + ", column=" + column + "]";
    }
}

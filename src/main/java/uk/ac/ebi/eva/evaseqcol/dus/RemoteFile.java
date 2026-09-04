package uk.ac.ebi.eva.evaseqcol.dus;

/**
 * Name and size of a file found in a remote HTTPS directory listing.
 */
public class RemoteFile {

    private final String name;

    private final long size;

    public RemoteFile(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

}

package mcech.clusterfuck.types;

public class File implements Comparable<File> {
	public File(String name, long size) { //TODO MD5 instead of String
		this.name = name;
		this.size = size;
	}
	
	@Override
	public int compareTo(File o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof File o) {
			return name.equals(o.name);
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public final String name;
	public final long size;
}

package mcech.clusterfuck;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mcech.clusterfuck.types.File;

public class LocalFiles {
	public LocalFiles(Path directory) {
		directory_ = directory;
	}
	
	public Set<File> list() throws IOException {
		Set<File> result = new HashSet<>();
		for (Path path : Files.list(directory_).toList()) {
			String name = path.getFileName().toString();
			long size = Files.size(path);
			result.add(new File(name, size));
		}
		return result; 
	}
	
	public byte[] read(String file, long pos, int len) throws IOException {
		Path path = directory_.resolve(file);
		
		byte[] result = new byte[len];
		try (RandomAccessFile in = new RandomAccessFile(path.toFile(), "r")) {
			in.seek(pos);
			int n = in.read(result);
			if (n == -1) {
				result = new byte[0];
			}
			else if (n < len) {
				result = Arrays.copyOf(result, n);
			}
			return result;
		}
	}
	
	public void write(String file, long pos, byte[] data) throws IOException {
		Path path = directory_.resolve(file);
		
		try (RandomAccessFile out = new RandomAccessFile(path.toFile(), "rw")) {
			out.seek(pos);
			out.write(data);
		}
	}
	
	public void move(String source, String destination) throws IOException {
		Path src = directory_.resolve(source);
		Path dst = directory_.resolve(destination);
		Files.move(src, dst, ATOMIC_MOVE);
	}
	
	public void delete(String file) throws IOException {
		Path path = directory_.resolve(file);
		Files.deleteIfExists(path);
	}
	
	private final Path directory_;
}

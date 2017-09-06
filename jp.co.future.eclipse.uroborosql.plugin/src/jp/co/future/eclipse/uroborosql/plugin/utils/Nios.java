package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Nios {
	public static class DeleteVisitor extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
			Files.delete(path);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
			if (exception == null) {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			} else {
				throw exception;
			}

		}
	}

	public static void deleteDirectories(Path path) {
		try {
			if (Files.exists(path)) {
				Files.walkFileTree(path, new DeleteVisitor());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void createDirectories(Path path) {
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

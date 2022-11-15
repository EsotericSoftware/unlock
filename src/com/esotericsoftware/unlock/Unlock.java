
package com.esotericsoftware.unlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Unlock {
	private final File handleExe;

	private Unlock (String path, boolean list) throws Exception {
		handleExe = new File(System.getProperty("java.io.tmpdir"), "/.unlock/handle64.exe");
		try (InputStream input = Unlock.class.getResourceAsStream("/handle64.exe")) {
			if (!handleExe.exists() || handleExe.length() != input.available()) {
				handleExe.getParentFile().mkdirs();
				Files.copy(input, handleExe.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}

		var handles = new ArrayList<String>();

		var command = new ArrayList<String>();
		command.add(handleExe.toString());
		command.add("-nobanner");
		command.add("-v");
		command.add(path);
		if (!run(command, handles, false)) {
			if (handles.size() != 1 || !handles.get(0).equals("No matching handles found."))
				throw error("Error getting handles (exit code): " + String.join(" ", command));
			System.out.println("File is not locked.");
			return;
		}
		handles.remove(0);

		command.remove(3);
		command.remove(2);
		command.add("-p");
		for (String entry : handles) {
			String[] columns = entry.split(",", 5);
			if (columns.length != 5) throw error("Error getting handles (invalid response): " + String.join(" ", command));
			if (!columns[4].trim().equals(path)) continue;
			command.add(columns[1]);
			command.add("-c");
			command.add(columns[3]);
			command.add("-y");
			run(command, null, true);
			command.remove(6);
			command.remove(5);
			command.remove(4);
			command.remove(3);
			System.out.println("Unlocked: " + columns[0]);
		}
	}

	private boolean run (ArrayList<String> command, ArrayList<String> output, boolean requireSuccess) throws Exception {
		var builder = new ProcessBuilder();
		builder.command(command);
		builder.redirectErrorStream(true);

		Process process = builder.start();

		if (output != null) {
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				reader.lines().forEach(line -> output.add(line));
			}
		}

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			if (requireSuccess) throw error("Shell exited with code " + exitCode + ": " + String.join(" ", command) //
				+ "\nOutput:\n" + String.join("\n", output));
			return false;
		}
		return true;
	}

	static private Exception error (String message) {
		return new Exception(message);
	}

	static public void main (String[] args) throws Exception {
		String path = null;
		boolean list = false;
		for (String arg : args) {
			if (arg.equals("--list"))
				list = true;
			else
				path = arg;
		}

		if (args.length != (list ? 2 : 1)) {
			System.out.println("Usage: [--list|-l] [path]");
			return;
		}

		File file = new File(path);
		try {
			file = file.getCanonicalFile();
		} catch (IOException ignored) {
			file = file.getCanonicalFile();
		}
		if (!file.exists()) {
			System.out.println("File does not exist.");
			return;
		}

		new Unlock(file.getAbsolutePath(), list);
	}
}

package com.claimlens.service;

import com.claimlens.config.YtDlpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class YtDlpService {

    private static final Logger log = LoggerFactory.getLogger(YtDlpService.class);

    private final YtDlpProperties properties;

    public YtDlpService(YtDlpProperties properties) {
        this.properties = properties;
    }

    public Path download(String url, Path targetDir) {
        String outputTemplate = targetDir.resolve(UUID.randomUUID() + ".%(ext)s").toString();

        List<String> command = List.of(
                properties.getBinaryPath(),
                "--quiet",
                "--no-warnings",
                "-f", "b",
                "--no-playlist",
                "-o", outputTemplate,
                "--print", "after_move:filepath",
                url
        );

        List<String> outputLines;
        int exitCode;
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            outputLines = readLines(process);
            exitCode = process.waitFor();
        } catch (IOException e) {
            throw new IllegalStateException("failed to run yt-dlp for " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while running yt-dlp for " + url, e);
        }

        if (exitCode != 0) {
            throw new IllegalStateException("yt-dlp failed (exit " + exitCode + ") for " + url + ": "
                    + String.join(" | ", outputLines));
        }

        String filePath = lastNonBlank(outputLines);
        if (filePath == null) {
            throw new IllegalStateException("yt-dlp did not report a downloaded file path for " + url);
        }

        log.info("Downloaded via yt-dlp: url={} path={}", url, filePath);
        return Path.of(filePath);
    }

    private List<String> readLines(Process process) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private String lastNonBlank(List<String> lines) {
        for (int i = lines.size() - 1; i >= 0; i--) {
            String candidate = lines.get(i).trim();
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }
}

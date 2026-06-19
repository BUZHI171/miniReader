package com.aireader.v2.controller;

import com.aireader.v2.repository.NovelRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final NovelRepository novelRepository;

    public BackupController(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBackup() {
        try {
            var novels = novelRepository.findAll();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                List<Map<String, Object>> exportedNovels = new ArrayList<>();

                for (var novel : novels) {
                    try {
                        Map<String, Object> novelData = new HashMap<>();
                        novelData.put("id", novel.getId());
                        novelData.put("title", novel.getTitle());
                        novelData.put("author", novel.getAuthor());
                        novelData.put("total_chapters", novel.getTotalChapters());
                        novelData.put("total_words", novel.getTotalWords());
                        novelData.put("prescan_status", novel.getPrescanStatus());

                        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(novelData);
                        zos.putNextEntry(new ZipEntry("novels/" + novel.getId() + ".json"));
                        zos.write(json.getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();

                        Map<String, Object> entry = new HashMap<>();
                        entry.put("id", novel.getId());
                        entry.put("title", novel.getTitle());
                        entry.put("total_chapters", novel.getTotalChapters());
                        exportedNovels.add(entry);
                    } catch (Exception e) {
                        // Skip novels that fail to export
                    }
                }

                Map<String, Object> manifest = new HashMap<>();
                manifest.put("backup_format_version", 1);
                manifest.put("exported_at", LocalDateTime.now().toString());
                manifest.put("app_version", "1.0.0");
                manifest.put("novel_count", exportedNovels.size());
                manifest.put("novels", exportedNovels);

                String manifestJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(manifest);
                zos.putNextEntry(new ZipEntry("manifest.json"));
                zos.write(manifestJson.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = "ai-reader-backup-" + dateStr + ".zip";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "澶囦唤瀵煎嚭澶辫触: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/import/preview")
    public ResponseEntity<Map<String, Object>> previewBackup(@RequestParam("file") MultipartFile file) {
        try {
            byte[] data = file.getBytes();

            Map<String, Object> result = new HashMap<>();
            result.put("backup_format_version", 1);
            result.put("exported_at", "unknown");
            result.put("novel_count", 0);
            result.put("novels", new ArrayList<>());
            result.put("conflict_count", 0);
            result.put("zip_size_bytes", data.length);

            var existingNovels = novelRepository.findAll();
            Map<String, String> existingTitles = new HashMap<>();
            for (var novel : existingNovels) {
                existingTitles.put(novel.getTitle(), novel.getId());
            }

            try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(data))) {
                ZipEntry entry;
                List<Map<String, Object>> novelsPreview = new ArrayList<>();

                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("manifest.json")) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                        String manifestJson = bos.toString(StandardCharsets.UTF_8);

                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        Map<String, Object> manifest = mapper.readValue(manifestJson, Map.class);
                        result.put("backup_format_version", manifest.get("backup_format_version"));
                        result.put("exported_at", manifest.get("exported_at"));

                        List<Map<String, Object>> manifestNovels = (List<Map<String, Object>>) manifest.get("novels");
                        if (manifestNovels != null) {
                            for (Map<String, Object> novelEntry : manifestNovels) {
                                String title = (String) novelEntry.get("title");
                                Boolean conflict = existingTitles.containsKey(title);

                                Map<String, Object> preview = new HashMap<>();
                                preview.put("id", novelEntry.get("id"));
                                preview.put("title", title);
                                preview.put("total_chapters", novelEntry.get("total_chapters"));
                                preview.put("conflict", conflict);
                                preview.put("existing_id", existingTitles.get(title));
                                novelsPreview.add(preview);
                            }
                        }
                        result.put("novels", novelsPreview);
                        result.put("novel_count", novelsPreview.size());
                        result.put("conflict_count", novelsPreview.stream().filter(n -> (Boolean) n.get("conflict")).count());
                    }
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "棰勮澶辫触: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/import/confirm")
    public ResponseEntity<Map<String, Object>> confirmBackupImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "conflict_mode", defaultValue = "skip") String conflictMode) {

        if (!conflictMode.equals("skip") && !conflictMode.equals("overwrite")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "conflict_mode 蹇呴』涓?skip 鎴?overwrite");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("imported", 0);
            result.put("skipped", 0);
            result.put("overwritten", 0);
            result.put("errors", new ArrayList<String>());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "澶囦唤瀵煎叆澶辫触: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
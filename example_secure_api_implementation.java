package com.example.filesecurity;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // Secure base directory
    private static final String BASE_DIRECTORY = "/path/to/secure/directory";

    /**
     * Endpoint to access a file within the base directory.
     * 
     * @param fileName File name provided by the user.
     * @return File content or error.
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<String> getFile(@PathVariable String fileName) {
        try {
            // Create a reference to the file based on the base directory
            File file = new File(BASE_DIRECTORY, fileName);

            // Resolve the canonical path to validate the location
            if (!file.getCanonicalPath().startsWith(new File(BASE_DIRECTORY).getCanonicalPath())) {
                return ResponseEntity.badRequest().body("Access denied: Invalid path.");
            }

            // Check if the file exists and is a file
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            // Read the file content (simplified for example)
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            return ResponseEntity.ok(content);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error accessing the file: " + e.getMessage());
        }
    }
}

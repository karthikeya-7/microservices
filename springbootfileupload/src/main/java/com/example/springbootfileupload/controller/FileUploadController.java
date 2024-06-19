package com.example.springbootfileupload.controller;

import com.example.springbootfileupload.entity.TempTableEntry;
import com.example.springbootfileupload.entity.UploadedFile;
import com.example.springbootfileupload.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/")
    public String index(Model model) {
        List<UploadedFile> files = fileUploadService.getAllFiles();
        model.addAttribute("files", files);
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            UploadedFile uploadedFile = fileUploadService.uploadFile(file, file.getOriginalFilename());
            if (uploadedFile.getStatus().startsWith("Failed")) {
                model.addAttribute("error", uploadedFile.getStatus());
            } else {
                model.addAttribute("uploadFile", uploadedFile);
                model.addAttribute("files", fileUploadService.getAllFiles());
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/view/{id}")
    public String viewFileContent(@PathVariable Long id, Model model) {
        try {
            List<TempTableEntry> tempTableEntries = fileUploadService.getTempTableEntriesByFileId(id);
            model.addAttribute("tempTableEntries", tempTableEntries);
            model.addAttribute("files", fileUploadService.getAllFiles());
            return "view";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error viewing file: " + e.getMessage());
            return "index"; // or handle error appropriately
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadCsvFile(@PathVariable Long id) {
        try {
            FileInputStream fileInputStream = fileUploadService.getConvertedCsvFile(id);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted_file_" + id + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(new InputStreamResource(fileInputStream));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

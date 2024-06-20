package com.example.springbootfileupload.service;

import com.example.springbootfileupload.entity.TempTableEntry;
import com.example.springbootfileupload.entity.UploadedFile;
import com.example.springbootfileupload.repository.TempTableRepository;
import com.example.springbootfileupload.repository.UploadedFileRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileUploadService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private TempTableRepository tempTableRepository;

    private static final String UPLOAD_DIR = "./uploads/";

    public UploadedFile uploadFile(MultipartFile multipartFile, String originalFileName) throws IOException {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOriginalFileName(originalFileName);

        // Convert Excel file to a list of entries
        List<TempTableEntry> tempTableEntries;
        try {
            tempTableEntries = convertExcelToTempTableEntries(multipartFile);
        } catch (IOException e) {
            uploadedFile.setTimestampFileName("");
            uploadedFile.setStatus("Failed: Error processing Excel file.");
            uploadedFileRepository.save(uploadedFile);
            return uploadedFile;
        }

        // Check for duplicates within the extracted content
        boolean hasDuplicates = checkForDuplicatesInContent(tempTableEntries);

        // Generate a timestamped file name and save the file
        String timestampFileName = LocalDateTime.now().toString().replace(":", "-") + ".csv";
        Path path = Paths.get(UPLOAD_DIR + timestampFileName);
        try {
            Files.copy(multipartFile.getInputStream(), path);
            uploadedFile.setTimestampFileName(timestampFileName);

            if (hasDuplicates) {
                uploadedFile.setStatus("File uploaded but contains duplicate entries.");
            } else {
                uploadedFile.setStatus("File uploaded and converted to CSV successfully");

                // Associate each entry with the uploaded file and save them
                tempTableEntries.forEach(entry -> entry.setUploadedFile(uploadedFile));
                tempTableRepository.saveAll(tempTableEntries);
            }
        } catch (IOException e) {
            uploadedFile.setTimestampFileName("");
            uploadedFile.setStatus("Failed: Error saving file.");
            uploadedFileRepository.save(uploadedFile);
            return uploadedFile;
        }

        // Save the uploaded file metadata
        uploadedFileRepository.save(uploadedFile);

        return uploadedFile;
    }

    private List<TempTableEntry> convertExcelToTempTableEntries(MultipartFile multipartFile) throws IOException {
        Workbook workbook = WorkbookFactory.create(multipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<TempTableEntry> entries = new ArrayList<>();

        for (Row row : sheet) {
            TempTableEntry entry = new TempTableEntry();
            entry.setColumnName(getStringValueFromCell(row.getCell(0)));
            entry.setEmailId(getStringValueFromCell(row.getCell(1))); // Example: assuming column 1 is String
            entry.setPhoneNumber(getStringValueFromCell(row.getCell(2))); // Example: assuming column 2 is PhoneNumber
            // Add more fields as needed

            entries.add(entry);
        }

        workbook.close();

        return entries;
    }

    private boolean checkForDuplicatesInContent(List<TempTableEntry> entries) {
        Set<String> uniqueEntries = new HashSet<>();
        for (TempTableEntry entry : entries) {
            String uniqueKey = entry.getColumnName() + "|" + entry.getEmailId() + "|" + entry.getPhoneNumber();
            if (!uniqueEntries.add(uniqueKey)) {
                return true; // Duplicate found
            }
        }
        return false; // No duplicates found
    }

    private String getStringValueFromCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    public List<UploadedFile> getAllFiles() {
        return uploadedFileRepository.findAll();
    }

    public List<TempTableEntry> getTempTableEntriesByFileId(Long fileId) {
        return tempTableRepository.findByUploadedFileId(fileId);
    }

    public UploadedFile getFileById(Long id) {
        Optional<UploadedFile> file = uploadedFileRepository.findById(id);
        if (file.isPresent()) {
            return file.get();
        } else {
            throw new IllegalArgumentException("Invalid file ID");
        }
    }

    public FileInputStream getConvertedCsvFile(Long id) throws IOException {
        UploadedFile uploadedFile = getFileById(id);
        String csvFilePath = UPLOAD_DIR + uploadedFile.getTimestampFileName();
        File file = new File(csvFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("CSV file not found for ID: " + id);
        }
        return new FileInputStream(file);
    }
}

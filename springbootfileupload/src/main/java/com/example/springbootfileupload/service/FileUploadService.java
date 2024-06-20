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
import java.util.List;
import java.util.Optional;

@Service
public class FileUploadService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private TempTableRepository tempTableRepository;

    private static final String UPLOAD_DIR = "./uploads/";

    public UploadedFile uploadFile(MultipartFile multipartFile, String originalFileName) throws IOException {
        boolean isDuplicate = checkForDuplicates(multipartFile);
        if (isDuplicate) {
            UploadedFile failedUpload = new UploadedFile();
            failedUpload.setOriginalFileName(originalFileName);
            failedUpload.setTimestampFileName("");
            failedUpload.setStatus("Failed: Duplicate file found.");
            return failedUpload;
        }

        String timestampFileName = LocalDateTime.now().toString().replace(":", "-") + ".csv";
        Path path = Paths.get(UPLOAD_DIR + timestampFileName);
        Files.copy(multipartFile.getInputStream(), path);

        List<TempTableEntry> tempTableEntries = convertExcelToTempTableEntries(path.toFile());

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOriginalFileName(originalFileName);
        uploadedFile.setTimestampFileName(timestampFileName);
        uploadedFile.setStatus("File uploaded and converted to CSV successfully");
        uploadedFileRepository.save(uploadedFile);

        tempTableEntries.forEach(entry -> entry.setUploadedFile(uploadedFile));
        tempTableRepository.saveAll(tempTableEntries);

        return uploadedFile;
    }

    private boolean checkForDuplicates(MultipartFile file) {
        // Implement duplicate check logic here, e.g., by file hash or name
        return false; // Placeholder; replace with actual logic
    }

    private List<TempTableEntry> convertExcelToTempTableEntries(File excelFile) throws IOException {
        Workbook workbook = WorkbookFactory.create(excelFile);
        Sheet sheet = workbook.getSheetAt(0);

        List<TempTableEntry> entries = new java.util.ArrayList<>();

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

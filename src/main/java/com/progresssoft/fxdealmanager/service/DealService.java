package com.progresssoft.fxdealmanager.service;

import com.progresssoft.fxdealmanager.model.Deal;
import com.progresssoft.fxdealmanager.repository.DealRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;


import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DealService {

    private final DealRepository dealRepository;
    private static final Logger logger = LoggerFactory.getLogger(DealService.class);

    
    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public List<String> importDealsFromExcel(InputStream inputStream) {
        List<String> logMessages = new ArrayList<>();
        logger.info("Started reading Excel file for import.");

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Assuming row 0 is header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String uniqueId = getCellAsString(row.getCell(0));
                    String fromCurrency = getCellAsString(row.getCell(1));
                    String toCurrency = getCellAsString(row.getCell(2));
                    String timestampStr = getCellAsString(row.getCell(3));
                    LocalDateTime dealTimestamp = parseTimestamp(timestampStr);
                    BigDecimal dealAmount = new BigDecimal(getCellAsString(row.getCell(4)));

                    if (uniqueId == null || uniqueId.isBlank()) {
                        logMessages.add(String.format("Row %d Skipped: Unique ID missing", i + 1));
                        logger.warn("Row {} Skipped: Unique ID missing", i + 1);
                        continue;
                    }
                    if (dealAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        logMessages.add(String.format("Row %d Skipped: Amount not positive", i + 1));
                        logger.warn("Row {} Skipped: Amount not positive", i + 1);
                        continue;
                    }
                    if (fromCurrency == null || fromCurrency.length() != 3 || toCurrency == null || toCurrency.length() != 3) {
                        logMessages.add(String.format("Row %d Skipped: Invalid currency code", i + 1));
                        logger.warn("Row {} Skipped: Invalid currency code", i + 1);
                        continue;
                    }

                    if (dealRepository.existsByUniqueId(uniqueId)) {
                        logMessages.add(String.format("Row %d Skipped: Duplicate Unique ID [%s]", i + 1, uniqueId));
                        logger.warn("Row {} Skipped: Duplicate Unique ID [{}]", i + 1, uniqueId);
                        continue;
                    }

                    Deal deal = new Deal(uniqueId, fromCurrency, toCurrency, dealTimestamp, dealAmount);
                    dealRepository.save(deal);
                    logMessages.add(String.format("Row %d: Deal [%s] saved.", i + 1, uniqueId));
                    logger.info("Row {}: Deal [{}] saved.", i + 1, uniqueId);
                } catch (Exception ex) {
                    logMessages.add(String.format("Row %d Error: %s", i + 1, ex.getMessage()));
                    logger.error("Row {} Error: {}", i + 1, ex.getMessage());
                }
            }
        } catch (Exception e) {
            logMessages.add("Failed to process file: " + e.getMessage());
            logger.error("Failed to process file: {}", e.getMessage());
        }

        logger.info("Finished importing Excel file.");
        return logMessages;
    }

    private String getCellAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    private LocalDateTime parseTimestamp(String timestampStr) {
    try {
        return LocalDateTime.parse(timestampStr);
    } catch (Exception e) {
        throw new RuntimeException("Invalid timestamp format: " + timestampStr);
    }
}
}

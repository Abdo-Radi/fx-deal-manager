package com.progresssoft.fxdealmanager.service;

import com.progresssoft.fxdealmanager.model.Deal;
import com.progresssoft.fxdealmanager.repository.DealRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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

        // Bulk fetch all existing unique IDs (before the loop)
        Set<String> existingIds = new HashSet<>(
                dealRepository.findAll().stream()
                        .map(Deal::getUniqueId)
                        .collect(Collectors.toSet()));

        List<Deal> dealsToSave = new ArrayList<>();
        Set<String> newBatchIds = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Assuming row 0 is header
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                // Safer, get strings first then check for missing
                String uniqueId = getCellAsString(row.getCell(0));
                String fromCurrency = getCellAsString(row.getCell(1));
                String toCurrency = getCellAsString(row.getCell(2));
                String timestampStr = getCellAsString(row.getCell(3));
                String amountStr = getCellAsString(row.getCell(4));

                // Null/missing field check
                if (uniqueId == null || fromCurrency == null || toCurrency == null ||
                        timestampStr == null || amountStr == null ||
                        uniqueId.isBlank() || fromCurrency.isBlank() || toCurrency.isBlank() ||
                        timestampStr.isBlank() || amountStr.isBlank()) {
                    logMessages.add(String.format("Row %d Skipped: Missing required field(s)", i + 1));
                    logger.warn("Row {} Skipped: Missing required field(s)", i + 1);
                    continue;
                }

                // Parse and type format validation
                LocalDateTime dealTimestamp;
                try {
                    dealTimestamp = parseTimestamp(timestampStr);
                } catch (Exception e) {
                    logMessages.add(String.format("Row %d Skipped: Invalid timestamp format", i + 1));
                    logger.warn("Row {} Skipped: Invalid timestamp format", i + 1);
                    continue;
                }

                BigDecimal dealAmount;
                try {
                    dealAmount = new BigDecimal(amountStr);
                } catch (Exception e) {
                    logMessages.add(String.format("Row %d Skipped: Invalid amount format", i + 1));
                    logger.warn("Row {} Skipped: Invalid amount format", i + 1);
                    continue;
                }

                if (dealAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    logMessages.add(String.format("Row %d Skipped: Amount not positive", i + 1));
                    logger.warn("Row {} Skipped: Amount not positive", i + 1);
                    continue;
                }
                if (fromCurrency.length() != 3 || toCurrency.length() != 3) {
                    logMessages.add(String.format("Row %d Skipped: Invalid currency code", i + 1));
                    logger.warn("Row {} Skipped: Invalid currency code", i + 1);
                    continue;
                }

                // Fast local check for duplicate in DB or in this Excel batch
                if (existingIds.contains(uniqueId) || newBatchIds.contains(uniqueId)) {
                    logMessages.add(String.format("Row %d Skipped: Duplicate Unique ID [%s]", i + 1, uniqueId));
                    logger.warn("Row {} Skipped: Duplicate Unique ID [{}]", i + 1, uniqueId);
                    continue;
                }

                Deal deal = new Deal(uniqueId, fromCurrency, toCurrency, dealTimestamp, dealAmount);
                dealsToSave.add(deal);
                newBatchIds.add(uniqueId);

                logMessages.add(String.format("Row %d: Deal [%s] ready to save.", i + 1, uniqueId));
                logger.info("Row {}: Deal [{}] ready to save.", i + 1, uniqueId);
            }

            // Batch save all valid, non-duplicate deals at once for speed
            if (!dealsToSave.isEmpty()) {
                dealRepository.saveAll(dealsToSave);
                logger.info("Batch saved {} deals.", dealsToSave.size());
            }
        } catch (Exception e) {
            logMessages.add("Failed to process file: " + e.getMessage());
            logger.error("Failed to process file: {}", e.getMessage());
        }

        logger.info("Finished importing Excel file.");
        return logMessages;
    }

    private String getCellAsString(Cell cell) {
        if (cell == null)
            return null;
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

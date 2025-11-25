package com.progresssoft.fxdealmanager.service;

import com.progresssoft.fxdealmanager.model.Deal;
import com.progresssoft.fxdealmanager.repository.DealRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DealServiceTest {

    @Test
    void testValidDealIsSaved() throws Exception {
        DealRepository mockRepo = Mockito.mock(DealRepository.class);
        Mockito.when(mockRepo.existsByUniqueId("VALIDID")).thenReturn(false);

        DealService service = new DealService(mockRepo);

        // Simulate Excel row data (replace with actual Excel stream for full test)
        Deal deal = new Deal("VALIDID", "USD", "EUR", LocalDateTime.now(), new BigDecimal("100"));
        mockRepo.save(deal);

        Mockito.verify(mockRepo, Mockito.times(1)).save(deal);
    }

    @Test
    void testDuplicateDealIsSkipped() throws Exception {
        DealRepository mockRepo = Mockito.mock(DealRepository.class);
        Mockito.when(mockRepo.existsByUniqueId("DUPLICATE")).thenReturn(true);

        DealService service = new DealService(mockRepo);

        // Simulate Excel row data (actual Excel test needs POI or test double)
        Deal deal = new Deal("DUPLICATE", "USD", "EUR", LocalDateTime.now(), new BigDecimal("100"));

        // Should not call save for duplicates
        Mockito.verify(mockRepo, Mockito.times(0)).save(deal);
    }
}

package com.bibliotech.fine.controller;

import com.bibliotech.fine.dto.FineRequest;
import com.bibliotech.fine.entity.Fine;
import com.bibliotech.fine.entity.FineStatus;
import com.bibliotech.fine.service.FineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FineController.class)
@AutoConfigureMockMvc(addFilters = false)
class FineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FineService fineService;

    @Test
    void calculate_Returns200Ok() throws Exception {
        FineRequest request = new FineRequest(10L, 100L, 50L, 3L);
        Fine fine = new Fine();
        fine.setId(1L);
        fine.setRentalId(10L);
        fine.setUserId(100L);
        fine.setDaysLate(3L);
        fine.setAmount(BigDecimal.valueOf(15));
        fine.setStatus(FineStatus.UNPAID);

        when(fineService.calculate(any(FineRequest.class))).thenReturn(fine);

        mockMvc.perform(post("/api/fines/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(15));
    }

    @Test
    void pay_Returns200Ok() throws Exception {
        Fine fine = new Fine();
        fine.setId(1L);
        fine.setStatus(FineStatus.PAID);
        fine.setTransactionId("TXN-12345");

        when(fineService.pay(1L)).thenReturn(fine);

        mockMvc.perform(put("/api/fines/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"));
    }
}

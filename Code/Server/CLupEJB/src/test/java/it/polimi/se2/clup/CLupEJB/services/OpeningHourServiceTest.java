package it.polimi.se2.clup.CLupEJB.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class OpeningHourServiceTest {

    @InjectMocks
    private OpeningHourService ohService;

    @Mock
    private EntityManager em;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }
}
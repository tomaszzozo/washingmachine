package edu.iis.mto.testreactor.washingmachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock private DirtDetector dirtDetector;
    @Mock private Engine engine;
    @Mock private WaterPump waterPump;
    private WashingMachine washingMachine;

    @BeforeEach
    void setUp() {
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void properBatchWithStaticProgram() {
        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withMaterialType(Material.COTTON)
                .withWeightKg(7d)
                .build();
        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withProgram(Program.LONG)
                .withSpin(true)
                .build();
        LaundryStatus result = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(result, LaundryStatus.builder()
                .withErrorCode(ErrorCode.NO_ERROR)
                .withResult(Result.SUCCESS)
                .withRunnedProgram(Program.LONG)
                .build());
    }

}

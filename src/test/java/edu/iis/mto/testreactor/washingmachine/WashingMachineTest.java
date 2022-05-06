package edu.iis.mto.testreactor.washingmachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static edu.iis.mto.testreactor.washingmachine.ErrorCode.*;
import static edu.iis.mto.testreactor.washingmachine.Result.*;

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
        Material irrelevantMaterial = Material.COTTON;
        double properWeight = 7d;
        LaundryBatch laundryBatch = createBatch(irrelevantMaterial, properWeight);

        Program staticProgram = Program.LONG;
        ProgramConfiguration programConfiguration = createProgram(staticProgram, true);

        LaundryStatus result = washingMachine.start(laundryBatch, programConfiguration);

        assertEquals(result, createStatus(NO_ERROR, SUCCESS, staticProgram));
    }

    private ProgramConfiguration createProgram(Program program, boolean spin) {
       return ProgramConfiguration.builder().withProgram(program).withSpin(spin).build();
    }

    private LaundryBatch createBatch(Material material, double weightKg) {
        return LaundryBatch.builder().withMaterialType(material).withWeightKg(weightKg).build();
    }

    private LaundryStatus createStatus(ErrorCode errorCode, Result result, Program program) {
        return LaundryStatus.builder().withErrorCode(errorCode).withResult(result).withRunnedProgram(program).build();
    }

}

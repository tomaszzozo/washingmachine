package edu.iis.mto.testreactor.washingmachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;

import static edu.iis.mto.testreactor.washingmachine.ErrorCode.*;
import static edu.iis.mto.testreactor.washingmachine.Result.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock private DirtDetector dirtDetector;
    @Mock private Engine engine;
    @Mock private WaterPump waterPump;

    private WashingMachine washingMachine;

    private final Material irrelevantMaterial = Material.COTTON;
    private final double properWeight = 7d;
    private final Program standardProgram = Program.LONG;
    private final ProgramConfiguration standardProgramConfig = createProgram(standardProgram, true);
    private final LaundryBatch standardBatch = createBatch(irrelevantMaterial, properWeight);
    private final LaundryStatus successStatus = createStatus(NO_ERROR, SUCCESS, standardProgram);

    @BeforeEach
    void setUp() {
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void standardUsage() {
        LaundryStatus result = washingMachine.start(standardBatch, standardProgramConfig);
        assertEquals(successStatus, result);
    }


    @Test
    void waterPumpAndEngineCallCheck() throws WaterPumpException, EngineException {
        washingMachine.start(standardBatch, standardProgramConfig);

        InOrder callOrder = inOrder(waterPump, engine);
        callOrder.verify(waterPump).pour(properWeight);
        callOrder.verify(engine).runWashing(standardProgram.getTimeInMinutes());
        callOrder.verify(waterPump).release();
        callOrder.verify(engine).spin();
    }

    @Test
    void tooHeavy() {
        LaundryBatch woolenCorrectBatch = createBatch(Material.WOOL, WashingMachine.MAX_WEIGTH_KG/2 - 0.001);
        LaundryBatch woolenIncorrectBatch = createBatch(Material.WOOL, WashingMachine.MAX_WEIGTH_KG/2);
        LaundryBatch woolenHeavierIncorrectBatch = createBatch(Material.WOOL, WashingMachine.MAX_WEIGTH_KG/2 + 0.001);
        LaundryBatch cottonCorrectBatch = createBatch(Material.COTTON, WashingMachine.MAX_WEIGTH_KG);
        LaundryBatch cottonIncorrectBatch = createBatch(Material.COTTON, WashingMachine.MAX_WEIGTH_KG + 0.001);
        LaundryStatus tooHeavyStatus = createStatus(TOO_HEAVY, FAILURE, null);

        ArrayList<LaundryBatch> incorrectBatches = new ArrayList<>(Arrays.asList(woolenIncorrectBatch,
                woolenHeavierIncorrectBatch, cottonIncorrectBatch));
        ArrayList<LaundryBatch> correctBatches = new ArrayList<>(Arrays.asList(cottonCorrectBatch, woolenCorrectBatch));

        for (LaundryBatch batch : incorrectBatches) {
            LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
            assertEquals(tooHeavyStatus, result);
        }
        for (LaundryBatch batch : correctBatches) {
            LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
            assertEquals(successStatus, result);
        }
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

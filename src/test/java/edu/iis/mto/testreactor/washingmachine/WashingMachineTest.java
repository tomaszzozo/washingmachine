package edu.iis.mto.testreactor.washingmachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static edu.iis.mto.testreactor.washingmachine.ErrorCode.*;
import static edu.iis.mto.testreactor.washingmachine.Result.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock private DirtDetector dirtDetector;
    @Mock private Engine engine;
    @Mock private WaterPump waterPump;

    private WashingMachine washingMachine;

    private final Material irrelevantMaterial = Material.COTTON;
    private final double properWeight = 7d;
    private final Program standardProgram = Program.LONG;
    private final ProgramConfiguration standardProgramConfig = createProgramWithSpin(standardProgram);
    private final LaundryBatch standardBatch = createBatch(irrelevantMaterial, properWeight);
    private final LaundryStatus successStatus = createStatus(NO_ERROR, SUCCESS, standardProgram);
    private final LaundryStatus tooHeavyStatus = createStatus(TOO_HEAVY, FAILURE, null);

    @BeforeEach
    void setUp() {
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void standardUsage() {
        LaundryStatus result = standardWash();
        assertEquals(successStatus, result);
    }


    @Test
    void waterPumpAndEngineCallCheck() throws WaterPumpException, EngineException {
        standardWash();

        InOrder callOrder = inOrder(waterPump, engine);
        callOrder.verify(waterPump).pour(properWeight);
        callOrder.verify(engine).runWashing(standardProgram.getTimeInMinutes());
        callOrder.verify(waterPump).release();
        callOrder.verify(engine).spin();
    }

    @Test
    void tooHeavyWoolenCorrectBatch() {
        LaundryBatch batch = createBatch(Material.WOOL, WashingMachine.MAX_WEIGTH_KG/2-0.001);
        LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
        assertEquals(successStatus, result);
    }

    @Test
    void tooHeavyWoolenIncorrectBatch() {
        LaundryBatch batch = createBatch(Material.WOOL, WashingMachine.MAX_WEIGTH_KG/2);
        LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
        assertEquals(tooHeavyStatus, result);
    }

    @Test
    void tooHeavyWoolenMoreIncorrectBatch() {
        LaundryBatch batch = createBatch(Material.WOOL, WashingMachine.MAX_WEIGTH_KG/2+0.001);
        LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
        assertEquals(tooHeavyStatus, result);
    }

    @Test
    void tooHeavyCottonCorrectBatch() {
        LaundryBatch batch = createBatch(Material.COTTON, WashingMachine.MAX_WEIGTH_KG);
        LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
        assertEquals(successStatus, result);
    }

    @Test
    void tooHeavyCottonIncorrectBatch() {
        LaundryBatch batch = createBatch(Material.COTTON, WashingMachine.MAX_WEIGTH_KG+0.001);
        LaundryStatus result = washingMachine.start(batch, standardProgramConfig);
        assertEquals(tooHeavyStatus, result);
    }

    @Test
    void waterPumpPourFailure() throws WaterPumpException {
        doThrow(new WaterPumpException()).when(waterPump).pour(any(Double.class));
        LaundryStatus result = standardWash();
        LaundryStatus expected = createStatus(WATER_PUMP_FAILURE, FAILURE, standardProgram);
        assertEquals(expected, result);
    }

    @Test
    void waterPumpReleaseFailure() throws WaterPumpException {
        doThrow(new WaterPumpException()).when(waterPump).release();
        LaundryStatus result = standardWash();
        LaundryStatus expected = createStatus(WATER_PUMP_FAILURE, FAILURE, standardProgram);
        assertEquals(expected, result);
    }

    @Test
    void engineRunWashingFailure() throws EngineException {
        doThrow(new EngineException()).when(engine).runWashing(any(Integer.class));
        LaundryStatus result = standardWash();
        LaundryStatus expected = createStatus(ENGINE_FAILURE, FAILURE, standardProgram);
        assertEquals(expected, result);
    }

    @Test
    void engineSpinFailure() throws EngineException {
        doThrow(new EngineException()).when(engine).spin();
        LaundryStatus result = standardWash();
        LaundryStatus expected = createStatus(ENGINE_FAILURE, FAILURE, standardProgram);
        assertEquals(expected, result);
    }

    @Test
    void programDetection() {
        ProgramConfiguration autodetectConfig = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.AUTODETECT)
                .build();

        double averageDirtPercentage = 50d;
        when(dirtDetector.detectDirtDegree(any(LaundryBatch.class))).thenReturn(new Percentage(averageDirtPercentage +0.01));
        LaundryStatus result = washingMachine.start(standardBatch, autodetectConfig);
        LaundryStatus expected = createStatus(NO_ERROR, SUCCESS, Program.LONG);
        assertEquals(expected, result);

        when(dirtDetector.detectDirtDegree(any(LaundryBatch.class))).thenReturn(new Percentage(averageDirtPercentage));
        result = washingMachine.start(standardBatch, autodetectConfig);
        expected = createStatus(NO_ERROR, SUCCESS, Program.MEDIUM);
        assertEquals(expected, result);
    }

    private ProgramConfiguration createProgramWithSpin(Program program) {
       return ProgramConfiguration.builder().withProgram(program).withSpin(true).build();
    }

    private LaundryBatch createBatch(Material material, double weightKg) {
        return LaundryBatch.builder().withMaterialType(material).withWeightKg(weightKg).build();
    }

    private LaundryStatus createStatus(ErrorCode errorCode, Result result, Program program) {
        return LaundryStatus.builder().withErrorCode(errorCode).withResult(result).withRunnedProgram(program).build();
    }

    private LaundryStatus standardWash() {
        return washingMachine.start(standardBatch, standardProgramConfig);
    }
}

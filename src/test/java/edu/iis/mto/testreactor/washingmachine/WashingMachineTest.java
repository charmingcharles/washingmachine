package edu.iis.mto.testreactor.washingmachine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock
    private DirtDetector dirtDetector;
    @Mock
    private Engine engine;
    @Mock
    private WaterPump waterPump;

    private WashingMachine washingMachine;

    private LaundryBatch generateLaundryBatch(int weight, Material material){
        return LaundryBatch.builder()
                .withWeightKg(weight)
                .withMaterialType(material)
                .build();
    }

    private ProgramConfiguration generateProgramConfiguration(boolean spin, Program program){
        return ProgramConfiguration.builder()
                .withSpin(spin)
                .withProgram(program)
                .build();
    }

    private LaundryStatus generateLaundryStatus(ErrorCode errorCode, Result result, Program program){
        return LaundryStatus.builder()
                .withErrorCode(errorCode)
                .withResult(result)
                .withRunnedProgram(program)
                .build();
    }

    @BeforeEach
    void setUp(){
        dirtDetector = Mockito.mock(DirtDetector.class);
        engine = Mockito.mock(Engine.class);
        waterPump = Mockito.mock(WaterPump.class);
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void successfulLongWashingTest() {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.LONG);
        LaundryStatus expectedLaundryStatus = generateLaundryStatus(ErrorCode.NO_ERROR, Result.SUCCESS, Program.LONG);

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }

    @Test
    void noSpinProgramLongBehaviorTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(false, Program.LONG);
        LaundryStatus expectedLaundryStatus = generateLaundryStatus(ErrorCode.NO_ERROR, Result.SUCCESS, Program.LONG);

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.LONG.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(0)).spin();
        assertEquals(Program.LONG.getTimeInMinutes(), programConfiguration.getProgram().getTimeInMinutes());
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }

    @Test
    void spinProgramMediumBehaviorTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.MEDIUM);

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
        assertEquals(Program.MEDIUM.getTimeInMinutes(), programConfiguration.getProgram().getTimeInMinutes());
    }

    @Test
    void spinProgramShortBehaviorTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
        assertEquals(Program.SHORT.getTimeInMinutes(), programConfiguration.getProgram().getTimeInMinutes());
    }

    @Test
    void spinProgramAutodetectBehaviorZeroPercentTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.AUTODETECT);

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(0));

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(dirtDetector, Mockito.times(1)).detectDirtDegree(laundryBatch);
        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramAutodetectBehaviorFiftyPercentTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.AUTODETECT);

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(50));

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(dirtDetector, Mockito.times(1)).detectDirtDegree(laundryBatch);
        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramAutodetectBehaviorSeventyPercentTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(5, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.AUTODETECT);

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(70));

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(dirtDetector, Mockito.times(1)).detectDirtDegree(laundryBatch);
        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.LONG.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramShortZeroWeightBehaviorTwentyPercentTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(0, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(0);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramOverweightBehaviorTwentyPercentTest() {
        LaundryBatch laundryBatch = generateLaundryBatch(10, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);
        LaundryStatus expectedLaundryStatus = generateLaundryStatus(ErrorCode.TOO_HEAVY, Result.FAILURE, null);

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }

    @Test
    void spinProgramShortNegativeWeightBehaviorTwentyPercentTest() throws WaterPumpException, EngineException { //is it ok?
        LaundryBatch laundryBatch = generateLaundryBatch(-4, Material.COTTON);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(-4);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramLongLessThanHalfWeightSpecialMaterialBehaviorTwentyPercentTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(3, Material.JEANS);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(3);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramLongHalfWeightSpecialMaterialBehaviorTwentyPercentTest() {
        LaundryBatch laundryBatch = generateLaundryBatch(4, Material.JEANS);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);
        LaundryStatus expectedLaundryStatus = generateLaundryStatus(ErrorCode.TOO_HEAVY, Result.FAILURE, null);

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }

    @Test
    void correctOrderTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(3, Material.JEANS);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(true, Program.SHORT);

        washingMachine.start(laundryBatch, programConfiguration);

        InOrder order = Mockito.inOrder(waterPump, engine);
        order.verify(waterPump).pour(3);
        order.verify(engine).runWashing(Program.SHORT.getTimeInMinutes());
        order.verify(waterPump).release();
        order.verify(engine).spin();
    }

    @Test
    void autoDetectButNoSpinCorrectOrderTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = generateLaundryBatch(3, Material.JEANS);
        ProgramConfiguration programConfiguration = generateProgramConfiguration(false, Program.AUTODETECT);

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(50));

        washingMachine.start(laundryBatch, programConfiguration);

        InOrder order = Mockito.inOrder(waterPump, engine, dirtDetector);
        order.verify(dirtDetector).detectDirtDegree(laundryBatch);
        order.verify(waterPump).pour(3);
        order.verify(engine).runWashing(Program.MEDIUM.getTimeInMinutes());
        order.verify(waterPump).release();
    }


}
